package me.caseload.knockbacksync.player;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.util.Vector3d;
import me.caseload.knockbacksync.world.PlatformWorld;
import me.caseload.knockbacksync.world.SpigotWorld;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class BukkitPlayer implements PlatformPlayer {
    public final Player bukkitPlayer;

    // Reflection variables
    private static Class<?> craftPlayerClass;
    private static Method getHandleMethod;
    private static Method getAttackStrengthScaleMethod;

    // 1.12.2 support
    static {
        try {
            Object server = Bukkit.getServer().getClass().getDeclaredMethod("getServer").invoke(Bukkit.getServer());
            String nmsPackage = server.getClass().getPackage().getName();
            String bukkitPackage = Bukkit.getServer().getClass().getPackage().getName();
            // Step 1: Load the CraftPlayer class
            craftPlayerClass = Class.forName(bukkitPackage + ".entity.CraftPlayer");
            // Step 2: Get the getHandle method
            getHandleMethod = craftPlayerClass.getMethod("getHandle");
            // Step 3: Get the getAttackStrengthScale method from the EntityPlayer class
            Class<?> entityPlayerClass = Class.forName(nmsPackage + ".EntityPlayer");
            String getAttackStrengthScaleMethodName = "";
            if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_13)) {
                getAttackStrengthScaleMethodName = "n";
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_14)) {
                getAttackStrengthScaleMethodName = "r";
            } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThan(ServerVersion.V_1_15)) {
                getAttackStrengthScaleMethodName = "s";
            } else { // > 1.14.4
                getAttackStrengthScaleMethodName = "getAttackStrengthScale";
            }
            getAttackStrengthScaleMethod = entityPlayerClass.getMethod(getAttackStrengthScaleMethodName, float.class); // getAttackStrengthScale(float)
            getAttackStrengthScaleMethod.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public BukkitPlayer(Player player) {
        this.bukkitPlayer = player;
    }

    @Override
    public UUID getUUID() {
        return bukkitPlayer.getUniqueId();
    }

    @Override
    public String getName() {
        return bukkitPlayer.getName();
    }

    @Override
    public double getX() {
        return bukkitPlayer.getLocation().getX();
    }

    @Override
    public double getY() {
        return bukkitPlayer.getLocation().getY();
    }

    @Override
    public double getZ() {
        return bukkitPlayer.getLocation().getZ();
    }

    @Override
    public float getPitch() {
        return bukkitPlayer.getLocation().getPitch();
    }

    @Override
    public float getYaw() {
        return bukkitPlayer.getLocation().getYaw();
    }

    @Override
    public boolean isOnGround() {
    /* Inconsistent with Entity.isOnGround()
    /  Checks to see if this player is currently standing on a block.
    /  This information may not be reliable, as it is a state provided by the client, and may therefore not be accurate.
    /  It can also easily be spoofed. We may want to cast to LivingEntity and call isOnGround() instead
    */
        return bukkitPlayer.isOnGround();
    }


    @Override
    public int getPing() {
        return PacketEvents.getAPI().getPlayerManager().getPing(bukkitPlayer);
    }

    @Override
    public boolean isGliding() {
        return bukkitPlayer.isGliding();
    }

    @Override
    public PlatformWorld getWorld() {
        return new SpigotWorld(bukkitPlayer.getWorld());
    }

    @Override
    public Vector3d getLocation() {
        Location location = bukkitPlayer.getLocation();
        return new Vector3d(location.getX(), location.getY(), location.getZ());
    }

    @Override
    public void sendMessage(@NotNull String s) {
        bukkitPlayer.sendMessage(s);
    }

    @Override
    public double getAttackCooldown() {
        try {
            // Step 1: Get the CraftPlayer instance
//            Object craftPlayer = craftPlayerClass.cast(bukkitPlayer);
            // Step 2: Get the handle (NMS EntityPlayer)
            Object entityPlayer = getHandleMethod.invoke(bukkitPlayer);
            // Step 3: Invoke the getAttackStrengthScale method
            return (float) getAttackStrengthScaleMethod.invoke(entityPlayer, 0.5f);
        } catch (Exception e) {
            throw new IllegalStateException("This plugin will not work. NMS mapping for getAttackCooldown() failed!");
        }
    }

    @Override
    public boolean isSprinting() {
        return bukkitPlayer.isSprinting();
    }

    @Override
    public int getMainHandKnockbackLevel() {
        return bukkitPlayer.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.KNOCKBACK);
    }

    @Override
    public @Nullable Integer getNoDamageTicks() {
        return bukkitPlayer.getNoDamageTicks();
    }

    @Override
    public void setVelocity(Vector3d adjustedVelocity) {
        bukkitPlayer.setVelocity(new Vector(adjustedVelocity.x, adjustedVelocity.y, adjustedVelocity.z));
    }
}