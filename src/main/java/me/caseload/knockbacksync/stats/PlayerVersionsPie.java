package me.caseload.knockbacksync.stats;

import me.caseload.knockbacksync.manager.PlayerData;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import org.bstats.charts.AdvancedPie;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerVersionsPie extends AdvancedPie {

    // Gets the client versions of players online
    public PlayerVersionsPie() {
        super("player_version", () -> {
            Map<String, Integer> valueMap = new HashMap<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                PlayerData playerData = PlayerDataManager.getPlayerData(player.getUniqueId());
                valueMap.put(playerData.getClientVersion().toString(), valueMap.getOrDefault(playerData.getClientVersion().toString(), 0) + 1);
            }
            return valueMap;
        });
    }
}
