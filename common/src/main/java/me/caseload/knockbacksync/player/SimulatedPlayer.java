package me.caseload.knockbacksync.player;

import com.github.retrooper.packetevents.protocol.potion.PotionEffect;
import com.github.retrooper.packetevents.protocol.world.BoundingBox;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import lombok.Getter;
import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.util.data.Vec2d;

import java.util.function.Consumer;

public final class SimulatedPlayer {

    private final PlayerData playerData;
    private final PlatformPlayer platformPlayer;
    private Vec2d position;
    @Getter private double yCoordinate;
    @Getter private double verticalVelocity;
    @Getter private BoundingBox boundingBox;
    @Getter private boolean isOnGround;

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
            double shiftY = newY - yCoordinate;
            boundingBox.shift(0, shiftY, 0);
            yCoordinate = newY;
            verticalVelocity = platformPlayer.getVelocity().getY();
            return;
        }

        verticalVelocity += playerData.getGravityAttribute();
        verticalVelocity = Math.min(verticalVelocity, 3.92);
        verticalVelocity *= 0.98;

        double newY = yCoordinate -= verticalVelocity;
        double shiftY = newY - yCoordinate;
        boundingBox.shift(0, shiftY, 0);
        yCoordinate = newY;
    }
}
