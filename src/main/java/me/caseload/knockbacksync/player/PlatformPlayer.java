package me.caseload.knockbacksync.player;

import com.github.retrooper.packetevents.util.Vector3d;
import me.caseload.knockbacksync.world.PlatformWorld;

import java.util.UUID;

public interface PlatformPlayer {
    UUID getUUID();
    String getName();
    double getX();
    double getY();
    double getZ();
    float getPitch();
    float getYaw();
    boolean isOnGround();
    int getPing();

    boolean isGliding();

    PlatformWorld getWorld();

    Vector3d getLocation();
    // Add more methods as needed
}
