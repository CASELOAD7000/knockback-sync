package me.caseload.knockbacksync.player;

import com.github.retrooper.packetevents.protocol.world.BoundingBox;
import com.github.retrooper.packetevents.util.Vector3d;
import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.util.data.Vec2d;
import me.caseload.knockbacksync.world.PlatformWorld;
import me.caseload.knockbacksync.world.raytrace.FluidHandling;
import me.caseload.knockbacksync.world.raytrace.RayTraceResult;

public class SimulatedPlayer {

    private final PlayerData playerData;
    private final PlatformPlayer platformPlayer;
    private Vec2d position;
    private double yCoordinate;
    private double verticalVelocity;
    private BoundingBox boundingBox;
    private boolean isOnGround;

    public SimulatedPlayer(PlayerData playerData) {
        this.playerData = playerData;
        this.platformPlayer = playerData.getPlatformPlayer();
        this.position = new Vec2d(platformPlayer.getX(), platformPlayer.getZ());
        this.yCoordinate = platformPlayer.getY();
        this.verticalVelocity = platformPlayer.getVelocity().y;
        this.boundingBox = platformPlayer.getBoundingBox();
        this.isOnGround = platformPlayer.isOnGround(); // not safe but its only for a single tick
    }

    public void tick() {
        Vector3d location = playerData.getPlatformPlayer().getLocation();
        Vec2d newPosition = new Vec2d(location.x, location.z);

        if (!position.equals(newPosition)) {
            double dx = newPosition.x - position.x;
            double dz = newPosition.z - position.z;
            boundingBox.shift(dx, 0, dz);
            position = newPosition;
        }

        if (platformPlayer.isFlying() || platformPlayer.isGliding()) {
            double newY = platformPlayer.getY();
            double dy = newY - yCoordinate;
            boundingBox.shift(0, dy, 0);
            yCoordinate = newY;
            return;
        }

        Base.LOGGER.info(String.valueOf(yCoordinate));
    }

    private double getVerticalVelocity() {
        return verticalVelocity;
    }

    private double getYCoordinate() {
        return yCoordinate;
    }
}
