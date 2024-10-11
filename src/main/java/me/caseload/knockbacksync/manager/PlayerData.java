package me.caseload.knockbacksync.manager;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import lombok.Getter;
import lombok.Setter;
import me.caseload.knockbacksync.KnockbackSync;
import me.caseload.knockbacksync.util.MathUtil;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Getter
public class PlayerData {

    private final Player player;

    public final User user;

    // Please read the GitHub FAQ before adjusting.
    public static long PING_OFFSET;

    @NotNull
    private final Map<Integer, Long> timeline = new HashMap<>();

    @NotNull
    private final Random random = new Random();

    @Nullable
    private BukkitTask combatTask;

    @Nullable @Setter
    private Long ping, previousPing;

    @Nullable @Setter
    private Double verticalVelocity;

    @Nullable @Setter
    private Integer lastDamageTicks;

    @Setter
    private double gravity = 0.08;

    public PlayerData(Player player) {
        this.player = player;
        this.user = PacketEvents.getAPI().getPlayerManager().getUser(player);
        PING_OFFSET = KnockbackSync.getInstance().getConfig().getInt("ping_offset", 25);
    }

    /**
     * Calculates the player's ping with compensation for lag spikes.
     * A hardcoded offset is applied for several reasons,
     * read the GitHub FAQ before adjusting.
     *
     * @return The compensated ping, with a minimum of 1.
     */
    public long getEstimatedPing() {
        long currentPing = (ping != null) ? ping : player.getPing();
        long lastPing = (previousPing != null) ? previousPing : player.getPing();
        long ping = (currentPing - lastPing > KnockbackSync.getInstance().getConfigManager().getSpikeThreshold()) ? lastPing : currentPing;

        return Math.max(1, ping - PING_OFFSET);
    }

    public void sendPing() {
        int packetId = random.nextInt(1, 10000);

        timeline.put(packetId, System.currentTimeMillis());

        WrapperPlayServerPing packet = new WrapperPlayServerPing(packetId);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
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
        Material material = player.getLocation().getBlock().getType();
        if (player.isGliding() || material == Material.WATER || material == Material.LAVA
                || material == Material.COBWEB || material == Material.SCAFFOLDING)
            return false;

        if (ping == null || ping < PING_OFFSET)
            return false;

        double gDist = getDistanceToGround();
        if (gDist <= 0)
            return false; // prevent player from taking adjusted knockback when on ground serverside

        int tMax = verticalVelocity > 0 ? MathUtil.calculateTimeToMaxVelocity(verticalVelocity, gravity) : 0;
        double mH = verticalVelocity > 0 ? MathUtil.calculateDistanceTraveled(verticalVelocity, tMax, gravity) : 0;
        int tFall = MathUtil.calculateFallTime(verticalVelocity, mH + gDist, gravity);

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

        World world = player.getWorld();

        for (Location corner : getBBCorners()) {
            RayTraceResult result = world.rayTraceBlocks(corner, new Vector(0, -1, 0), 5, FluidCollisionMode.NEVER, true);
            if (result == null || result.getHitBlock() == null)
                continue;

            collisionDist = Math.min(collisionDist, corner.getY() - result.getHitBlock().getY());
        }

        return collisionDist - 1;
    }

    /**
     * Gets the corners of the Player's bounding box.
     *
     * @return An array of locations representing the corners of the bounding box.
     */
    public Location @NotNull [] getBBCorners() {
        BoundingBox boundingBox = player.getBoundingBox();
        Location location = player.getLocation();
        World world = location.getWorld();

        double adjustment = 0.01; // To ensure the bounding box isn't clipping inside a wall

        return new Location[] {
                new Location(world, boundingBox.getMinX() + adjustment, location.getY(), boundingBox.getMinZ() + adjustment),
                new Location(world, boundingBox.getMinX() + adjustment, location.getY(), boundingBox.getMaxZ() - adjustment),
                new Location(world, boundingBox.getMaxX() - adjustment, location.getY(), boundingBox.getMinZ() + adjustment),
                new Location(world, boundingBox.getMaxX() - adjustment, location.getY(), boundingBox.getMaxZ() - adjustment)
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
            double knockbackResistance = player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).getValue();
            double resistanceFactor = 0.04000000119 * knockbackResistance * 10;
            yAxis -= resistanceFactor;
        }

        // vertical velocity is always 0.4 when you have knockback level higher than 0
        if (attacker.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.KNOCKBACK) > 0)
            yAxis = 0.4;

        return yAxis;
    }

    // might need soon
    public double calculateJumpVelocity() {
        double jumpVelocity = 0.42;

        PotionEffect jumpEffect = player.getPotionEffect(PotionEffectType.JUMP);
        if (jumpEffect != null) {
            int amplifier = jumpEffect.getAmplifier();
            jumpVelocity += (amplifier + 1) * 0.1F;
        }

        return jumpVelocity;
    }

    public boolean isInCombat() {
        return combatTask != null;
    }

    public void updateCombat() {
        if (isInCombat())
            combatTask.cancel();

        combatTask = newCombatTask();
        CombatManager.addPlayer(player.getUniqueId());
    }

    public void quitCombat(boolean cancelTask) {
        if (cancelTask)
            combatTask.cancel();

        combatTask = null;
        CombatManager.removePlayer(player.getUniqueId());
        timeline.clear(); // failsafe for packet loss idk
    }

    @NotNull
    private BukkitTask newCombatTask() {
        return Bukkit.getScheduler().runTaskLaterAsynchronously(KnockbackSync.getInstance(),
                () -> quitCombat(false), KnockbackSync.getInstance().getConfigManager().getCombatTimer());
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