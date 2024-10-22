package me.caseload.knockbacksync.listener;

import com.github.retrooper.packetevents.util.Vector3d;
import me.caseload.knockbacksync.player.PlatformPlayer;

public abstract class PlayerJumpListener {

    public void onPlayerJump(PlatformPlayer player) {
        Vector3d velocity = player.getVelocity();

        if (velocity.getY() < 0)
            return;

        double jumpVelocity = player.getJumpVelocity();

        if (Double.compare(velocity.getY(), jumpVelocity) == 0) {
            player.sendMessage("You just jumped probably idk maybe not idk");
        }
    }
}
