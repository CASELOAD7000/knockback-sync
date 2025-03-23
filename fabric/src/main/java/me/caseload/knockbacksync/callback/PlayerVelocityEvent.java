package me.caseload.knockbacksync.callback;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;

public interface PlayerVelocityEvent {
    Event<PlayerVelocityEvent> EVENT = EventFactory.createArrayBacked(PlayerVelocityEvent.class,
            (listeners) -> (player, velocity) -> {
                for (PlayerVelocityEvent listener : listeners) {
                    ActionResult result = listener.onVelocityChange(player, velocity);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    ActionResult onVelocityChange(ServerPlayerEntity player, Vec3d velocity);
}