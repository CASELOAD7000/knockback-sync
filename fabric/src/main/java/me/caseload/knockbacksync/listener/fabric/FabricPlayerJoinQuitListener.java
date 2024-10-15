package me.caseload.knockbacksync.listener.fabric;

import me.caseload.knockbacksync.listener.PlayerJoinQuitListener;
import me.caseload.knockbacksync.manager.PlayerData;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class FabricPlayerJoinQuitListener extends PlayerJoinQuitListener {
    public void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            onPlayerJoin(new PlayerData(handler.player.getUUID()));
        });
        ServerPlayConnectionEvents.DISCONNECT.register(((handler, server) -> {
            onPlayerQuit(handler.player.getUUID());
        }));
    }
}
