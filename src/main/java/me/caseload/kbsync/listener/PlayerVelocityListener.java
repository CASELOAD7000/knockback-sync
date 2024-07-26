package me.caseload.kbsync.listener;

import me.caseload.kbsync.KbSync;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;

public class PlayerVelocityListener implements Listener {

    private final Map<UUID, Integer> pingMap;

    public PlayerVelocityListener(Map<UUID, Integer> pingMap) {
        this.pingMap = pingMap;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        EntityDamageEvent entityDamageEvent = player.getLastDamageCause();

        if (entityDamageEvent == null || entityDamageEvent.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK
                || !(entityDamageEvent instanceof EntityDamageEvent)) {
            return;
        }

        Vector velocity = player.getVelocity();
        if (isPlayerOnGround(player) || !isPredictiveOnGround(player, velocity.getY())) return;

        // Process velocity adjustment asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(KbSync.getInstance(), () -> {
            Vector newVelocity = velocity.clone();
            double yAxis = KbSync.kb.getOrDefault(player.getUniqueId(), velocity.getY());
            newVelocity.setY(yAxis);
            player.setVelocity(newVelocity);
        });
    }

    public boolean isPredictiveOnGround(Player player, double verticalVelocity) {
        if (isPlayerOnGround(player)) return true;

        if (player.isGliding() || player.getLocation().getBlock().getType() == Material.WATER
                || player.getLocation().getBlock().getType() == Material.LAVA) return false;

        double distanceToGround = calculateDistanceToGround(player);
        double maxHeight = verticalVelocity > 0 ? calculateMaxHeight(verticalVelocity) : 0;

        int ticksUntilFalling = verticalVelocity > 0 ? calculateTimeToReachMaxVerticalVelocity(verticalVelocity) : 0;
        int ticksToReachGround = calculateFallTime(verticalVelocity, maxHeight + distanceToGround);
        int delay = ticksUntilFalling + ticksToReachGround;
        long estimatedPing = pingMap.getOrDefault(player.getUniqueId(), player.getPing());

        return delay / 20.0 * 1000 <= estimatedPing && distanceToGround <= 1.3;
    }

    public boolean isPlayerOnGround(Player player) {
        Block blockBelow = player.getLocation().clone().subtract(0, 0.1, 0).getBlock();
        return blockBelow.getType().isSolid();
    }

    public static int calculateFallTime(double initialVelocity, double distance) {
        final double terminalVelocity = 3.92;
        final double gravity = 0.08;
        final double multiplier = 0.98;

        double velocity = Math.abs(initialVelocity);
        int ticks = 0;

        while (distance > 0) {
            velocity += gravity;
            velocity = Math.min(velocity, terminalVelocity);
            velocity *= multiplier;
            distance -= velocity;
            ticks++;
        }

        return ticks;
    }

    public static int calculateTimeToReachMaxVerticalVelocity(double targetVerticalVelocity) {
        final double terminalVelocity = 3.92;
        final double gravity = 0.08;
        final double multiplier = 0.98;

        double a = -gravity * multiplier;
        double b = gravity + terminalVelocity * multiplier;
        double c = -2 * targetVerticalVelocity;

        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) return 0;  // Ensure non-negative discriminant
        double positiveRoot = (-b + Math.sqrt(discriminant)) / (2 * a);

        return (int) Math.ceil(positiveRoot * 20);
    }

    public static double calculateMaxHeight(double targetVerticalVelocity) {
        return targetVerticalVelocity * 2.484875;
    }

    public double calculateDistanceToGround(Player player) {
        World world = player.getWorld();
        double maxDistance = player.getLocation().getY();

        // Using ray tracing to find the distance to the ground
        Block hitBlock = world.rayTraceBlocks(player.getLocation().clone(), new Vector(0, -1, 0), maxDistance).getHitBlock();

        while (hitBlock != null && hitBlock.isPassable()) {
            hitBlock = world.rayTraceBlocks(hitBlock.getLocation().clone(), new Vector(0, -1, 0), maxDistance).getHitBlock();
        }

        if (hitBlock == null) return 0;
        double distanceToGround = player.getLocation().getY() - hitBlock.getLocation().getY();
        return distanceToGround - 1;
    }
}
