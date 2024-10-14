package me.caseload.knockbacksync.listener.fabric;

import me.caseload.knockbacksync.listener.PlayerJoinQuitListener;
import me.caseload.knockbacksync.player.FabricPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class FabricPlayerJoinQuitListener extends PlayerJoinQuitListener {
    public void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            onPlayerJoin(new FabricPlayer(handler.player));
        });
        ServerPlayConnectionEvents.DISCONNECT.register(((handler, server) -> {
            onPlayerQuit(handler.player.getUUID());
        }));
    }
}
