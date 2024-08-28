package me.caseload.knockbacksync.util;

import me.caseload.knockbacksync.manager.PingManager;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class PlayerUtil {

    public static boolean predictiveOnGround(Player player, double verticalVelocity) {
        Material material = player.getLocation().getBlock().getType();
        if (player.isGliding() || material == Material.WATER || material == Material.LAVA || material == Material.COBWEB)
            return false;

        double distanceToGround = getDistanceToGround(player);
        if (distanceToGround <= 0)
            return true;

        double maxHeight = verticalVelocity > 0 ? MathUtil.getMaxHeight(verticalVelocity) : 0;
        double ticksUntilMaxHeight = verticalVelocity > 0 ? MathUtil.getTimeToMaxUpwardSpeed(verticalVelocity) : 0;

        int ticksToGround = MathUtil.getFallTime(verticalVelocity, maxHeight + distanceToGround);
        double totalDelayTicks = ticksUntilMaxHeight + ticksToGround;

        long estimatedPing = PingManager.getPingMap().getOrDefault(player.getUniqueId(), (long) player.getPing());

        return totalDelayTicks / 20.0 * 1000 <= estimatedPing && distanceToGround <= 1.3;
    }

    public static double getModifiedYAxis(Player victim, Player attacker) {
        double yAxis = attacker.getAttackCooldown() > 0.9 ? 0.4 : 0.36080000519752503;

        if (!attacker.isSprinting()) {
            yAxis = 0.36080000519752503;
            double knockbackResistance = victim.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).getValue();
            double resistanceFactor = 0.04000000119 * knockbackResistance * 10;
            yAxis -= resistanceFactor;
        }

        return yAxis;
    }

    public static double getDistanceToGround(Player player) {
        Location location = player.getLocation();
        World world = location.getWorld();
        BoundingBox boundingBox = player.getBoundingBox();

        Location[] corners = {
                new Location(world, boundingBox.getMinX(), location.getY(), boundingBox.getMinZ()),
                new Location(world, boundingBox.getMinX(), location.getY(), boundingBox.getMaxZ()),
                new Location(world, boundingBox.getMaxX(), location.getY(), boundingBox.getMinZ()),
                new Location(world, boundingBox.getMaxX(), location.getY(), boundingBox.getMaxZ())
        };

        double collisionDist = 50;

        for (Location corner : corners) {
            RayTraceResult result = world.rayTraceBlocks(corner, new Vector(0, -1, 0), 50, FluidCollisionMode.NEVER, true);
            if (result == null || result.getHitBlock() == null)
                continue;

            collisionDist = Math.min(collisionDist, corner.getY() - result.getHitBlock().getY());
        }

        return collisionDist - 1;
    }
}