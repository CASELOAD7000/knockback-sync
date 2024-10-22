package me.caseload.knockbacksync.player;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.player.PlayerManager;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.ConnectionState;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.BoundingBox;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerKeepAlive;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import lombok.Getter;
import lombok.Setter;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.manager.CombatManager;
import me.caseload.knockbacksync.scheduler.AbstractTaskHandle;
import me.caseload.knockbacksync.util.MathUtil;
import me.caseload.knockbacksync.util.data.Pair;
import me.caseload.knockbacksync.world.PlatformWorld;
import me.caseload.knockbacksync.world.raytrace.FluidHandling;
import me.caseload.knockbacksync.world.raytrace.RayTraceResult;
import org.incendo.cloud.CloudCapability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
public class PlayerData {

    // Please read the GitHub FAQ before adjusting.
    public static final long PING_OFFSET = 25;
    public static float TICK_RATE = 20.0F;
    private static Field playerField;
    public final List<Pair<Integer, Long>> transactionsSent = new LinkedList<>();
    public final List<Pair<Long, Long>> keepaliveMap = new LinkedList<>();

    private static final short MAIN_THREAD_TRANSACTION_ID = -1;
    private static final short NETTY_THREAD_TRANSACTION_ID = -2;

    static {
        try {
            switch (KnockbackSyncBase.INSTANCE.platform) {
                case BUKKIT:
                case FOLIA:
                    Class<?> bukkitPlayerClass = Class.forName("me.caseload.knockbacksync.player.BukkitPlayer");
                    playerField = bukkitPlayerClass.getDeclaredField("bukkitPlayer");
                    break;
                case FABRIC:
                    Class<?> fabricPlayerClass = Class.forName("me.caseload.knockbacksync.player.FabricPlayer");
                    playerField = fabricPlayerClass.getDeclaredField("fabricPlayer");
                    break;
                default:
                    throw new IllegalStateException("Unexpected platform: " + KnockbackSyncBase.INSTANCE.platform);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        playerField.setAccessible(true); // May not be needed since it's already public
    }

    public final User user;
    private final PlatformPlayer platformPlayer;
    private final UUID uuid;
    @NotNull private final Random random = new Random();
    public long lastKeepAliveID = 0;
    @Getter private final JitterCalculator jitterCalculator = new JitterCalculator();
    @Setter private double jitter;
    @Nullable private AbstractTaskHandle combatTask;
    @Nullable @Setter private Double ping, previousPing;
    @Nullable @Setter private Double verticalVelocity;
    @Nullable @Setter private Integer lastDamageTicks;
    @Setter private double gravityAttribute = 0.08;
    @Setter private double knockbackResistanceAttribute = 0.0;

    public PlayerData(PlatformPlayer platformPlayer) {
        this.uuid = platformPlayer.getUUID();
        this.platformPlayer = platformPlayer;

        User tempUser = null;

        PlayerManager playerManager = PacketEvents.getAPI().getPlayerManager();
        Object player = null;
        try {
            player = playerField.get(platformPlayer);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        if (player != null) {
            tempUser = playerManager.getUser(player);
        }

        this.user = tempUser;
    }

    /**
     * Calculates the player's ping with compensation for lag spikes.
     * A hardcoded offset is applied for several reasons,
     * read the GitHub FAQ before adjusting.
     *
     * @return The compensated ping, with a minimum of 1.
     */
    public double getEstimatedPing() {
        double currentPing = (ping != null) ? ping : platformPlayer.getPing();
        double lastPing = (previousPing != null) ? previousPing : platformPlayer.getPing();
        double ping = (currentPing - lastPing > KnockbackSyncBase.INSTANCE.getConfigManager().getSpikeThreshold()) ? lastPing : currentPing;

        return Math.max(1, ping - PING_OFFSET);
    }

//    public boolean isKeepAliveIDOurs(long id) {
//        return id == lastKeepAliveID;
//    }

    public void sendPing(boolean async) {
        if (user == null || user.getEncoderState() != ConnectionState.PLAY) return;

       String pingStrategy = KnockbackSyncBase.INSTANCE.getConfigManager().getConfigWrapper().getString("ping_strategy", "KEEPALIVE");
       switch (pingStrategy) {
           case "KEEPALIVE":
               long keepAliveID = async ? NETTY_THREAD_TRANSACTION_ID : MAIN_THREAD_TRANSACTION_ID;
               if (async) {
                   ChannelHelper.runInEventLoop(user.getChannel(), () -> {
                       // We call sendPacket instead of writePacket because it flushes immediately
                       // Making our time measurement more accurate since we don't call, System.nanoTime(), wait until flush
                       // And then actually send packet
                       user.sendPacket(new WrapperPlayServerKeepAlive(keepAliveID));
                   });
               } else {
                   user.sendPacket(new WrapperPlayServerKeepAlive(keepAliveID));
               }
               break;
           case "PING":
           case "TRANSACTION":
               PacketWrapper<?> packet;
               short pingTransactionID = async ? NETTY_THREAD_TRANSACTION_ID : MAIN_THREAD_TRANSACTION_ID;
               if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_17)) {
                   packet = new WrapperPlayServerPing(pingTransactionID);
               } else {
                   packet = new WrapperPlayServerWindowConfirmation((byte) 0, pingTransactionID, false);
               }

               if (async) {
                   ChannelHelper.runInEventLoop(user.getChannel(), () -> {
                       user.writePacket(packet);
                   });
               } else {
                   user.writePacket(packet);
               }
               break;
       }
    }

    /**
     * Determines if the Player is on the ground clientside, but not serverside
     * <p>
     * Returns <code>ping ≥ (tMax + tFall)</code> and <code>gDist ≤ 1.3</code>
     * <p>
     * Where:
     * <ul>
     *   <li><code>ping</code>: Estimated latency</li>
     *   <li><code>tMax</code>: Time to reach maximum upward velocity</li>
     *   <li><code>tFall</code>: Time to fall to the ground</li>
     *   <li><code>gDist</code>: Distance to the ground</li>
     * </ul>
     *
     * @param verticalVelocity The Player's current vertical velocity.
     * @return <code>true</code> if the Player is on the ground; <code>false</code> otherwise.
     */
    public boolean isOnGround(double verticalVelocity) {
        WrappedBlockState blockState = platformPlayer.getWorld().getBlockStateAt(platformPlayer.getLocation().getPosition());

        if (platformPlayer.isGliding() ||
                blockState.getType() == StateTypes.WATER ||
                blockState.getType() == StateTypes.LAVA ||
                blockState.getType() == StateTypes.COBWEB ||
                blockState.getType() == StateTypes.SCAFFOLDING) {
            return false;
        }

        if (ping == null || ping < PING_OFFSET)
            return false;

        double gDist = getDistanceToGround();
        if (gDist <= 0)
            return false; // prevent player from taking adjusted knockback when on ground serverside

        int tMax = verticalVelocity > 0 ? MathUtil.calculateTimeToMaxVelocity(verticalVelocity, gravityAttribute) : 0;
        double mH = verticalVelocity > 0 ? MathUtil.calculateDistanceTraveled(verticalVelocity, tMax, gravityAttribute) : 0;
        int tFall = MathUtil.calculateFallTime(verticalVelocity, mH + gDist, gravityAttribute);

        if (tFall == -1)
            return false; // reached the max tick limit, not safe to predict

        return getEstimatedPing() >= tMax + tFall / TICK_RATE * 1000 && gDist <= 1.3;
    }

    /**
     * Ray traces from each corner of the player's bounding box to the ground,
     * returning the smallest distance, with a maximum limit of 5 blocks.
     *
     * @return The distance to the ground in blocks
     */
    public double getDistanceToGround() {
        double collisionDist = 5;

        PlatformWorld world = platformPlayer.getWorld();

        for (Location corner : getBBCorners()) {
            RayTraceResult result = world.rayTraceBlocks(corner.getPosition(), new Vector3d(0, -1, 0), 5, FluidHandling.NONE, true);

            if (result == null || result.getHitPosition() == null)
                continue;

            collisionDist = Math.min(collisionDist, corner.getY() - result.getHitPosition().getY());
        }

        return collisionDist - 1;
    }

    /**
     * Gets the corners of the Player's bounding box.
     *
     * @return An array of locations representing the corners of the bounding box.
     */
    private Location[] getBBCorners() {
        BoundingBox boundingBox = platformPlayer.getBoundingBox();
        Location location = platformPlayer.getLocation();

        double adjustment = 0.01; // To ensure the bounding box isn't clipping inside a wall

        return new Location[] {
                new Location(boundingBox.getMinX() + adjustment, location.getY(), boundingBox.getMinZ() + adjustment, 0, 0),
                new Location(boundingBox.getMinX() + adjustment, location.getY(), boundingBox.getMaxZ() - adjustment, 0, 0),
                new Location(boundingBox.getMaxX() - adjustment, location.getY(), boundingBox.getMinZ() + adjustment,0 , 0),
                new Location(boundingBox.getMaxX() - adjustment, location.getY(), boundingBox.getMaxZ() - adjustment,0, 0)
        };
    }

    /**
     * Calculates the positive vertical velocity.
     * This is used to switch falling knockback to rising knockback.
     *
     * @param attacker The player who is attacking.
     * @return The calculated positive vertical velocity, consistent with vanilla behavior.
     */
    public double calculateVerticalVelocity(PlatformPlayer attacker) {
        double yAxis = attacker.getAttackCooldown() > 0.848 ? 0.4 : 0.36080000519752503;

        if (!attacker.isSprinting()) {
            yAxis = 0.36080000519752503;
//            double knockbackResistance = knockbackResistanceAttribute;
            double resistanceFactor = 0.04000000119 * knockbackResistanceAttribute * 10;
            yAxis -= resistanceFactor;
        }

        // vertical velocity is always 0.4 when you have knockback level higher than 0
        if (attacker.getMainHandKnockbackLevel() > 0)
            yAxis = 0.4;

        return yAxis;
    }

    // might need soon
//    public double calculateJumpVelocity() {
//        double jumpVelocity = 0.42;
//
//        PotionEffect jumpEffect = player.getPotionEffect(PotionEffectType.JUMP);
//        if (jumpEffect != null) {
//            int amplifier = jumpEffect.getAmplifier();
//            jumpVelocity += (amplifier + 1) * 0.1F;
//        }
//
//        return jumpVelocity;
//    }

    public boolean isInCombat() {
        return combatTask != null;
    }

    public void updateCombat() {
        if (isInCombat())
            combatTask.cancel();

        combatTask = newCombatTask();
        CombatManager.addPlayer(uuid);
    }

    public void quitCombat(boolean cancelTask) {
        if (cancelTask)
            combatTask.cancel();

        combatTask = null;
        CombatManager.removePlayer(uuid);
    }

    @NotNull
    private AbstractTaskHandle newCombatTask() {
        return KnockbackSyncBase.INSTANCE.getScheduler().runTaskLaterAsynchronously(
                () -> quitCombat(false), KnockbackSyncBase.INSTANCE.getConfigManager().getCombatTimer());
    }

    public ClientVersion getClientVersion() {
        if (user == null)
            return ClientVersion.UNKNOWN;
        ClientVersion ver = user.getClientVersion();
        if (ver == null) {
            // If temporarily null, assume server version...
            return ClientVersion.getById(PacketEvents.getAPI().getServerManager().getVersion().getProtocolVersion());
        }
        return ver;
    }

    public long getKeepAliveSendTime() {
        return lastKeepAliveID;
    }

    public boolean didWeSendThatPacket(long receivedId) {
        return receivedId == NETTY_THREAD_TRANSACTION_ID || receivedId == MAIN_THREAD_TRANSACTION_ID;
    }
}