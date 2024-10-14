package me.caseload.knockbacksync.listener;

import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.manager.PlayerData;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.PlatformPlayer;

public abstract class PlayerDamageListener {
    public void onPlayerDamage(PlatformPlayer victim, PlatformPlayer attacker) {
        if (!KnockbackSyncBase.INSTANCE.getConfigManager().isToggled())
            return;

        PlayerData playerData = PlayerDataManager.getPlayerData(victim.getUUID());
        if (playerData == null)
            return;

        playerData.setVerticalVelocity(playerData.calculateVerticalVelocity(attacker)); // do not move this calculation
        playerData.setLastDamageTicks(victim.getNoDamageTicks());
        playerData.updateCombat();

        if (!KnockbackSyncBase.INSTANCE.getConfigManager().isRunnableEnabled())
            playerData.sendPing();
    }
}
