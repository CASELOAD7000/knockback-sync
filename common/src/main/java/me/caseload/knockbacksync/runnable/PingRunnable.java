package me.caseload.knockbacksync.runnable;

import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.manager.CombatManager;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.PlayerData;

import java.util.UUID;

public class PingRunnable implements Runnable {

    @Override
    public void run() {
        if (!Base.INSTANCE.getConfigManager().isToggled())
            return;

        for (UUID uuid : CombatManager.getPlayers()) {
            PlayerData playerData = PlayerDataManager.getPlayerData(uuid);
            playerData.sendPing(true);
        }
    }
}
