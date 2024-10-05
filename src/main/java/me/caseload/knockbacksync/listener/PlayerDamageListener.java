package me.caseload.knockbacksync.listener;

import me.caseload.knockbacksync.KnockbackSync;
import me.caseload.knockbacksync.manager.PlayerData;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerDamageListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!KnockbackSync.getInstance().isToggled())
            return;

        if (!(event.getEntity() instanceof Player victim) || !(event.getDamager() instanceof Player attacker))
            return;

        PlayerData playerData = PlayerDataManager.getPlayerData(victim.getUniqueId());
        playerData.setVerticalVelocity(playerData.calculateVerticalVelocity(attacker)); // do not move this calculation
        playerData.updateCombat();

        if (!KnockbackSync.getInstance().isRunnableEnabled())
            playerData.sendPing();
    }
}