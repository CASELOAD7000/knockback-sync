package me.caseload.knockbacksync.listener;

import com.github.retrooper.packetevents.util.Vector3d;
import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.PlatformPlayer;
import me.caseload.knockbacksync.player.PlayerData;

public abstract class PlayerKnockbackListener {

    public void onPlayerVelocity(PlatformPlayer victim, Vector3d velocity) {
        if (!Base.INSTANCE.getConfigManager().isToggled())
            return;

        PlayerData victimPlayerData = PlayerDataManager.getPlayerData(victim.getUUID());
        if (victimPlayerData == null)
            return;

/*        Integer damageTicks = playerData.getLastDamageTicks();
        if (damageTicks != null && damageTicks > 8)
            return;*/

        Double verticalVelocity = victimPlayerData.getVerticalVelocity();
        if(verticalVelocity == null) return;

        if (victimPlayerData.isOnGroundClientSide(velocity.getY())) {
            // Since we're already changing types do we need to use withY to get a new object
            // Or can we just go velocity.y = verticalVelocity ?
            Vector3d adjustedVelocity = velocity.withY(verticalVelocity);
            victim.setVelocity(adjustedVelocity); // Use PlatformPlayer's setVelocity
        } else if (!victim.isOnGround()) {
            if(!victimPlayerData.isOffGroundSyncEnabled()) return;
            Vector3d adjustedVelocity = velocity.withY(victimPlayerData.getCompensatedOffGroundVelocity());
            victim.setVelocity(adjustedVelocity); // Use PlatformPlayer's setVelocity
        }
    }
}