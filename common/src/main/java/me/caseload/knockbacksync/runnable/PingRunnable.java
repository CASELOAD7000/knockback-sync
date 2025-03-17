package me.caseload.knockbacksync.runnable;

import com.github.retrooper.packetevents.protocol.player.User;
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

        for (User user : CombatManager.getPlayers()) {
            PlayerData playerData = PlayerDataManager.getPlayerData(user);
            playerData.sendPing(true);
        }
    }
}
