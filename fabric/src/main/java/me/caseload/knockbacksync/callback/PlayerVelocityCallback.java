package me.caseload.knockbacksync.callback;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.Vec3;

public interface PlayerVelocityCallback {
    Event<PlayerVelocityCallback> EVENT = EventFactory.createArrayBacked(PlayerVelocityCallback.class,
            (listeners) -> (player, velocity) -> {
                for (PlayerVelocityCallback listener : listeners) {
                    InteractionResult result = listener.onVelocityChange(player, velocity);

                    if (result != InteractionResult.PASS) {
                        return result;
                    }
                }

                return InteractionResult.PASS;
            });

    InteractionResult onVelocityChange(ServerPlayer player, Vec3 velocity);
}