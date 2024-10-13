package me.caseload.knockbacksync.player;

import com.github.retrooper.packetevents.util.Vector3d;
import me.caseload.knockbacksync.world.FabricWorld;
import me.caseload.knockbacksync.world.PlatformWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class FabricPlayer implements PlatformPlayer {
    private final ServerPlayerEntity player;

    public FabricPlayer(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public UUID getUUID() {
        return player.getUuid();
    }

    @Override
    public String getName() {
        return player.getName().toString();
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
        return player.networkHandler.getLatency();
    }

    @Override
    public boolean isGliding() {
        return player.isFallFlying();
    }

    @Override
    public PlatformWorld getWorld() {
        return new FabricWorld(player.getWorld());
    }

    @Override
    public Vector3d getLocation() {
        Vec3d pos = player.getPos();
        return new Vector3d(pos.x, pos.y, pos.z);
    }

    // Implement other methods
}
