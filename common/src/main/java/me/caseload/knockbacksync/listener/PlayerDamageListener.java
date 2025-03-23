package me.caseload.knockbacksync.listener;

import com.github.retrooper.packetevents.protocol.player.User;
import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.PlatformPlayer;
import me.caseload.knockbacksync.player.PlayerData;

public abstract class PlayerDamageListener {
    public void onPlayerDamage(PlatformPlayer victim, PlatformPlayer attacker) {
        if (!Base.INSTANCE.getConfigManager().isToggled())
            return;

        User user = victim.getUser();
        if (user == null) return; // Prevent errors with fake players

        PlayerData playerData = PlayerDataManager.getPlayerData(user);
        if (playerData == null)
            return;

        playerData.setVerticalVelocity(playerData.calculateVerticalVelocity(attacker)); // do not move this calculation
        playerData.setLastDamageTicks(victim.getNoDamageTicks());
        playerData.updateCombat();

        if (!Base.INSTANCE.getConfigManager().isRunnableEnabled())
            playerData.sendPing(true);
    }
}
