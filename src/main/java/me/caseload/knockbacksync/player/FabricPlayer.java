package me.caseload.knockbacksync.player;

import com.github.retrooper.packetevents.util.Vector3d;
import me.caseload.knockbacksync.world.FabricWorld;
import me.caseload.knockbacksync.world.PlatformWorld;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

public class FabricPlayer implements PlatformPlayer {
    private final ServerPlayer player;

    public FabricPlayer(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public UUID getUUID() {
        return player.getUUID();
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
        return player.getXRot();
    }

    @Override
    public float getYaw() {
        return player.getYRot();
    }

    @Override
    public boolean isOnGround() {
        return player.onGround();
    }

    @Override
    public int getPing() {
        return player.connection.latency();
    }

    @Override
    public boolean isGliding() {
        return player.isFallFlying();
    }

    @Override
    public PlatformWorld getWorld() {
        return new FabricWorld(player.level());
    }

    @Override
    public Vector3d getLocation() {
        Vec3 pos = player.position();
        return new Vector3d(pos.x, pos.y, pos.z);
    }

    // Implement other methods
}
