package me.caseload.knockbacksync.listener;

import me.caseload.knockbacksync.KnockbackSync;
import me.caseload.knockbacksync.manager.PlayerData;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerJoinQuitListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerDataManager.addPlayerData(player.getUniqueId(), new PlayerData(player));

        if (KnockbackSync.getInstance().isUpdateAvailable() && player.hasPermission("knockbacksync.update"))
            player.sendMessage(ChatColor.translateAlternateColorCodes(
                    '&',
                    "&6An updated version of &eKnockbackSync &6is now available for download at: &bhttps://github.com/CASELOAD7000/knockback-sync/releases/latest"
            ));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        PlayerData playerData = PlayerDataManager.getPlayerData(uuid);

        if (playerData.isInCombat())
            playerData.quitCombat(true);

        PlayerDataManager.removePlayerData(uuid);
    }
}
