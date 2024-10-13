package me.caseload.knockbacksync.player;

import com.github.retrooper.packetevents.util.Vector3d;
import me.caseload.knockbacksync.world.PlatformWorld;
import me.caseload.knockbacksync.world.SpigotWorld;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BukkitPlayer implements PlatformPlayer {
    private final Player player;

    public BukkitPlayer(Player player) {
        this.player = player;
    }

    @Override
    public UUID getUUID() {
        return player.getUniqueId();
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public double getX() {
        return player.getX();
    }

    @Override
    public double getY() {
        return player.getY();
    }

    @Override
    public double getZ() {
        return player.getZ();
    }

    @Override
    public float getPitch() {
        return player.getPitch();
    }

    @Override
    public float getYaw() {
        return player.getYaw();
    }

    @Override
    public boolean isOnGround() {
        return player.isOnGround();
    }

    @Override
    public int getPing() {
        return player.getPing();
    }

    @Override
    public boolean isGliding() {
        return player.isGliding();
    }

    @Override
    public PlatformWorld getWorld() {
        return new SpigotWorld(player.getWorld());
    }

    @Override
    public Vector3d getLocation() {
        Location location = player.getLocation();
        return new Vector3d(location.getX(), location.getY(), location.getZ());
    }
}