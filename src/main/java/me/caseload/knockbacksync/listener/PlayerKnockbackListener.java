package me.caseload.knockbacksync.listener;

import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.manager.PlayerData;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.PlatformPlayer;
import org.bukkit.util.Vector;

public abstract class PlayerKnockbackListener {

    public void onPlayerVelocity(PlatformPlayer victim, Vector velocity) {
        if (!KnockbackSyncBase.INSTANCE.getConfigManager().isToggled())
            return;

        PlayerData playerData = PlayerDataManager.getPlayerData(victim.getUUID());
        if (playerData == null)
            return;

        Integer damageTicks = playerData.getLastDamageTicks();
        if (damageTicks != null && damageTicks > 8)
            return;

        Double verticalVelocity = playerData.getVerticalVelocity();
        if (verticalVelocity == null || !playerData.isOnGround(velocity.getY()))
            return;

        Vector adjustedVelocity = velocity.clone().setY(verticalVelocity);
        victim.setVelocity(adjustedVelocity); // Use PlatformPlayer's setVelocity
    }
}