package me.caseload.knockbacksync.listener;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
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

        if (victimPlayerData.getNotNullPing() < PlayerData.PING_OFFSET)
            return;

        double distanceToGround = victimPlayerData.getDistanceToGround();
        if (distanceToGround <= 0)
            return; // minecraft already does the work for us

        WrappedBlockState blockState = victim.getWorld().getBlockStateAt(victim.getLocation());
        if (victim.isGliding() ||
                blockState.getType() == StateTypes.WATER ||
                blockState.getType() == StateTypes.LAVA ||
                blockState.getType() == StateTypes.COBWEB ||
                blockState.getType() == StateTypes.SCAFFOLDING)
            return;

        Vector3d adjustedVelocity;
        if (victimPlayerData.isOnGroundClientSide(velocity.getY(), distanceToGround))
            adjustedVelocity = velocity.withY(victimPlayerData.getVerticalVelocity()); // Should be impossible to produce a NPE in this context
            // Todo FIX
            // This can be spoofed! Clients can just say they're onGround to take less KB!
        else if (victimPlayerData.isOffGroundSyncEnabled())
            adjustedVelocity = velocity.withY(victimPlayerData.getCompensatedOffGroundVelocity());
        else
            return;

        victim.setVelocity(adjustedVelocity);
    }
}