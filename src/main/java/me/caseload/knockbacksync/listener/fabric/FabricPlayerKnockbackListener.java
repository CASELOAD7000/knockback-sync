package me.caseload.knockbacksync.listener.fabric;

import me.caseload.knockbacksync.listener.PlayerKnockbackListener;
import me.caseload.knockbacksync.player.FabricPlayer;
import org.bukkit.util.Vector;

public class FabricPlayerKnockbackListener extends PlayerKnockbackListener {

    public void register() {
//        PlayerVelocityUpdateCallback.EVENT.register((player, velocity) -> {
//            onPlayerVelocity(new FabricPlayer(player), new Vector(velocity.x, velocity.y, velocity.z));
//            return velocity; // Return the original velocity for Fabric (no modification here)
//        });
    }
}