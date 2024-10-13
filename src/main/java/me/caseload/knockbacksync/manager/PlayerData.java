package me.caseload.knockbacksync.manager;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import lombok.Getter;
import lombok.Setter;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.player.BukkitPlayer;
import me.caseload.knockbacksync.player.PlatformPlayer;
import me.caseload.knockbacksync.world.PlatformWorld;
import me.caseload.knockbacksync.scheduler.AbstractTaskHandle;
import me.caseload.knockbacksync.util.MathUtil;
import me.caseload.knockbacksync.world.raytrace.FluidHandling;
import me.caseload.knockbacksync.world.raytrace.RayTraceResult;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Getter
public class PlayerData {

//    private final Player player;
    private final PlatformPlayer platformPlayer;
    private final UUID uuid;

    public final User user;

    // Please read the GitHub FAQ before adjusting.
    public static long PING_OFFSET;

    @NotNull
    private final Map<Integer, Long> timeline = new HashMap<>();

    @NotNull
    private final Random random = new Random();

    @Nullable
    private AbstractTaskHandle combatTask;

    @Nullable @Setter
    private Long ping, previousPing;

    @Nullable @Setter
    private Double verticalVelocity;

    @Nullable @Setter
    private Integer lastDamageTicks;

    @Setter
    private double gravityAttribute = 0.08;

    @Setter
    private double knockbackResistanceAttribute = 0.0;

    public PlayerData(Player player) {
        this.uuid = player.getUniqueId();
        this.user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        this.platformPlayer = new BukkitPlayer(player);
//        this.player = player;
//        this.serverPlayer = ((CraftPlayer) player).getHandle();
        PING_OFFSET = KnockbackSyncBase.INSTANCE.getConfigManager().getConfig().getInt("ping_offset", 25);
    }

    /**
     * Calculates the player's ping with compensation for lag spikes.
     * A hardcoded offset is applied for several reasons,
     * read the GitHub FAQ before adjusting.
     *
     * @return The compensated ping, with a minimum of 1.
     */
    public long getEstimatedPing() {
        long currentPing = (ping != null) ? ping : platformPlayer.getPing();
        long lastPing = (previousPing != null) ? previousPing : platformPlayer.getPing();
        long ping = (currentPing - lastPing > KnockbackSyncBase.INSTANCE.getConfigManager().getSpikeThreshold()) ? lastPing : currentPing;

        return Math.max(1, ping - PING_OFFSET);
    }

    public void sendPing() {
        int packetId = random.nextInt(1, 10000);

        timeline.put(packetId, System.currentTimeMillis());

        WrapperPlayServerPing packet = new WrapperPlayServerPing(packetId);
        user.sendPacket(packet);
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
        WrappedBlockState blockState = platformPlayer.getWorld().getBlockStateAt(platformPlayer.getLocation());

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

        return getEstimatedPing() >= tMax + tFall / 20.0 * 1000 && gDist <= 1.3;
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

        for (Vector3d corner : getBBCorners()) {
            RayTraceResult result = world.rayTraceBlocks(corner, new Vector3d(0, -1, 0), 5, FluidHandling.NONE, true);

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
    private Vector3d[] getBBCorners() {
        Vector3d playerPos = platformPlayer.getLocation();
        double width = 0.6;  // typical player width
//        double height = 1.8;  // typical player height
        double adjustment = 0.01;

        return new Vector3d[] {
                new Vector3d(playerPos.getX() - width/2 + adjustment, playerPos.getY(), playerPos.getZ() - width/2 + adjustment),
                new Vector3d(playerPos.getX() - width/2 + adjustment, playerPos.getY(), playerPos.getZ() + width/2 - adjustment),
                new Vector3d(playerPos.getX() + width/2 - adjustment, playerPos.getY(), playerPos.getZ() - width/2 + adjustment),
                new Vector3d(playerPos.getX() + width/2 - adjustment, playerPos.getY(), playerPos.getZ() + width/2 - adjustment)
        };
    }

    /**
     * Calculates the positive vertical velocity.
     * This is used to switch falling knockback to rising knockback.
     *
     * @param attacker The player who is attacking.
     * @return The calculated positive vertical velocity, consistent with vanilla behavior.
     */
    public double calculateVerticalVelocity(Player attacker) {
        double yAxis = attacker.getAttackCooldown() > 0.848 ? 0.4 : 0.36080000519752503;

        if (!attacker.isSprinting()) {
            yAxis = 0.36080000519752503;
//            double knockbackResistance = knockbackResistanceAttribute;
            double resistanceFactor = 0.04000000119 * knockbackResistanceAttribute * 10;
            yAxis -= resistanceFactor;
        }

        // vertical velocity is always 0.4 when you have knockback level higher than 0
        if (attacker.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.KNOCKBACK) > 0)
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
        timeline.clear(); // failsafe for packet loss idk
    }

    @NotNull
    private AbstractTaskHandle newCombatTask() {
        return KnockbackSyncBase.INSTANCE.getScheduler().runTaskLaterAsynchronously(
                () -> quitCombat(false), KnockbackSyncBase.INSTANCE.getConfigManager().getCombatTimer());
    }

    public ClientVersion getClientVersion() {
        ClientVersion ver = user.getClientVersion();
        if (ver == null) {
            // If temporarily null, assume server version...
            return ClientVersion.getById(PacketEvents.getAPI().getServerManager().getVersion().getProtocolVersion());
        }
        return ver;
    }
}