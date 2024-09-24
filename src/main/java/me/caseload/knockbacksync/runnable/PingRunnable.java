package me.caseload.knockbacksync.runnable;

import me.caseload.knockbacksync.KnockbackSync;
import me.caseload.knockbacksync.manager.CombatManager;
import me.caseload.knockbacksync.manager.PlayerData;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class PingRunnable extends BukkitRunnable {

    @Override
    public void run() {
        if (!KnockbackSync.getInstance().isToggled())
            return;

        for (UUID uuid : CombatManager.getPlayers()) {
            PlayerData playerData = PlayerDataManager.getPlayerData(uuid);
            playerData.sendPing();
        }
    }
}
