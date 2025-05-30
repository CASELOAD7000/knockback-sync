package me.caseload.knockbacksync.player;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.BoundingBox;
import com.github.retrooper.packetevents.util.Vector3d;
import me.caseload.knockbacksync.world.PlatformWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    void sendMessage(@NotNull String s);

    double getAttackCooldown();

    boolean isSprinting();

    int getMainHandKnockbackLevel();

    @Nullable Integer getNoDamageTicks();

    void setVelocity(Vector3d adjustedVelocity);

    Vector3d getVelocity();

    double getJumpPower();

    BoundingBox getBoundingBox();

    /**
     * If a player disconnects while we are running the constructor of a PlatformPlayer
     * PacketEvents.getAPI().getPlayerManager().getUser(bukkitPlayer) may return null
     * This will make the internal field null and thus this function may return null.
     */
    @Nullable User getUser();

    void setClientBrand(String brand);

    String getClientBrand();
    // Add more methods as needed
}
