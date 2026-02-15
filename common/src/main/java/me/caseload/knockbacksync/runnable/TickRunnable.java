package me.caseload.knockbacksync.runnable;

import com.github.retrooper.packetevents.protocol.player.User;
import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.PlatformPlayer;
import me.caseload.knockbacksync.player.PlayerData;
import me.caseload.knockbacksync.player.SimulatedPlayer;

public class TickRunnable implements Runnable {

    @Override
    public void run() {
        if (!Base.INSTANCE.getConfigManager().isToggled())
            return;

        for (PlatformPlayer player : Base.INSTANCE.getPlatformServer().getOnlinePlayers()) {
            User user = player.getUser();
            if (user == null)
                continue;

            PlayerData playerData = PlayerDataManager.getPlayerData(user);
            if (playerData == null)
                continue;

            SimulatedPlayer simulatedPlayer = playerData.getSimulatedPlayer();
            if (simulatedPlayer == null)
                continue;

            simulatedPlayer.tick();
        }
    }
}