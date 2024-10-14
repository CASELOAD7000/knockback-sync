package me.caseload.knockbacksync.listener;

import com.github.retrooper.packetevents.util.Vector3d;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.manager.PlayerData;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.PlatformPlayer;

public abstract class PlayerKnockbackListener {

    public void onPlayerVelocity(PlatformPlayer victim, Vector3d velocity) {
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

        // Since we're already changing types do we need to use withY to get a new object
        // Or can we just go velcoity.y = verticalVelocity ?
        Vector3d adjustedVelocity = velocity.withY(verticalVelocity);
        victim.setVelocity(adjustedVelocity); // Use PlatformPlayer's setVelocity
    }
}