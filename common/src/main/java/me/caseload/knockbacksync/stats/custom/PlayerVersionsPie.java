package me.caseload.knockbacksync.stats.custom;

import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.PlatformPlayer;
import me.caseload.knockbacksync.player.PlayerData;
import me.caseload.knockbacksync.stats.AdvancedPie;

import java.util.HashMap;
import java.util.Map;

public class PlayerVersionsPie extends AdvancedPie {

    // Gets the client versions of players online
    public PlayerVersionsPie() {
        super("player_version", () -> {
            Map<String, Integer> valueMap = new HashMap<>();
            for (PlatformPlayer player : KnockbackSyncBase.INSTANCE.platformServer.getOnlinePlayers()) {
                PlayerData playerData = PlayerDataManager.getPlayerData(player.getUUID());
                valueMap.put(playerData.getClientVersion().toString(), valueMap.getOrDefault(playerData.getClientVersion().toString(), 0) + 1);
            }
            return valueMap;
        });
    }
}
