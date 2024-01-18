package me.caseload.kbsync.listener;

import me.caseload.kbsync.KbSync;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.w3c.dom.Attr;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

public class PlayerVelocityListener implements Listener {

    private final Map<UUID, Integer> pingMap;

    public PlayerVelocityListener(Map<UUID, Integer> pingMap) {
        this.pingMap = pingMap;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void on(PlayerVelocityEvent event) {
        if (event.isCancelled())
            return;

        Player player = event.getPlayer();
        EntityDamageEvent entityDamageEvent = player.getLastDamageCause();
        if (entityDamageEvent == null)
            return;

        EntityDamageEvent.DamageCause damageCause = entityDamageEvent.getCause();
        if (damageCause != EntityDamageEvent.DamageCause.ENTITY_ATTACK)
            return;

        Entity damager = ((EntityDamageByEntityEvent) entityDamageEvent).getDamager();
        if (!(damager instanceof Player))
            return;

        //player.sendMessage("old y: " + player.getVelocity().getY());

        Vector velocity = player.getVelocity();
        if (player.isOnGround() || !predictiveOnGround(player, velocity.getY()))
            return;

        Vector newVelocity = velocity.clone();
        double yAxis = KbSync.kb.get(player.getUniqueId());
        //player.sendMessage("new y: " + yAxis);
        newVelocity.setY(yAxis);
        player.setVelocity(newVelocity);
    }

    public boolean predictiveOnGround(Player player, double verticalVelocity) {
        if (player.isOnGround())
            return true;

        if (player.isGliding()
                || player.getLocation().getBlock().getType() == Material.WATER
                || player.getLocation().getBlock().getType() == Material.LAVA)
            return false;

        double distanceToGround = calculateDistanceToGround(player);
        double maxHeight = verticalVelocity > 0 ? calculateMaxHeight(verticalVelocity) : 0;

        int ticksUntilFalling = verticalVelocity > 0 ? calculateTimeToReachMaxVerticalVelocity(verticalVelocity) : 0;
        int ticksToReachGround = calculateFallTime(verticalVelocity, maxHeight + distanceToGround);
        int delay = ticksUntilFalling + ticksToReachGround;
        long estimatedPing = pingMap.get(player.getUniqueId()) == null ? player.getPing() : pingMap.get(player.getUniqueId());

        if (delay / 20.0 * 1000 <= estimatedPing && distanceToGround <= 1.3)
            //player.sendMessage("Adjusted your velocity because the time in air was less or equal to your ping. (" + delay / 20.0 * 1000 + "<= " + estimatedPing + ")");
            return true;

        //player.sendMessage("Didn't adjust your velocity because the time in air wasn't less or equal to your ping. (" + delay / 20.0 * 1000 + "> " + estimatedPing + ")");
        return false;
    }

    public static int calculateFallTime(double initialVelocity, double distance) {
        double terminalVelocity = 3.92;
        double gravity = 0.08;
        double multiplier = 0.98;

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

    public static int calculateTimeToReachMaxVerticalVelocity(double targetVerticalVelocity)
    {
        double terminalVelocity = 3.92;
        double gravity = 0.08;
        double multiplier = 0.98;

        double a = -gravity * multiplier;
        double b = gravity + terminalVelocity * multiplier;
        double c = -2 * targetVerticalVelocity;

        double discriminant = b * b - 4 * a * c;
        double positiveRoot = (-b + Math.sqrt(discriminant)) / (2 * a);

        return (int)Math.ceil(positiveRoot * 20);
    }

    public static double calculateMaxHeight(double targetVerticalVelocity)
    {
        double maxHeight = targetVerticalVelocity * 2.484875;
        return maxHeight;
    }

    public double calculateDistanceToGround(Player player) {
        Location playerLocation = player.getLocation();
        World world = player.getWorld();

        Location startLocation = playerLocation.clone();

        Vector direction = new Vector(0, -1, 0);

        double maxDistance = playerLocation.getY();

        Vector hitPosition = world.rayTraceBlocks(startLocation, direction, maxDistance).getHitPosition();
        Block hitBlock = hitPosition.toLocation(world).getBlock();

        while (hitBlock != null && hitBlock.isPassable()) {
            hitPosition = world.rayTraceBlocks(hitBlock.getLocation(), direction, maxDistance).getHitPosition();
            hitBlock = hitPosition.toLocation(world).getBlock();
        }

        double distanceToGround = startLocation.getY() - hitBlock.getLocation().getY();

        return distanceToGround - 1;
    }

/*    public double calculateDistanceToGround(Player player) {
        Location playerLocation = player.getLocation();
        World world = player.getWorld();

        int playerX = playerLocation.getBlockX();
        int playerZ = playerLocation.getBlockZ();
        int groundY = world.getHighestBlockYAt(playerX, playerZ);

        double distanceToGround = playerLocation.getY() - groundY;

        return distanceToGround - 1;
    }*/
}
