package me.caseload.knockbacksync.listener;

import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.PlatformPlayer;
import me.caseload.knockbacksync.player.PlayerData;

import java.util.concurrent.CompletableFuture;

public abstract class PlayerKnockbackListener {

    public void onPlayerVelocity(PlatformPlayer victim, Vector3d velocity) {
        if (!Base.INSTANCE.getConfigManager().isToggled())
            return;

        PlayerData victimPlayerData = PlayerDataManager.getPlayerData(victim.getUUID());
        if (victimPlayerData == null)
            return;

        if (victimPlayerData.getNotNullPing() < PlayerData.PING_OFFSET)
            return;

        victimPlayerData.getDistanceToGround().asFuture()
            .thenCompose(distanceToGround -> {
                if (distanceToGround <= 0)
                    return CompletableFuture.completedFuture(null);

                return victim.getWorld().getBlockStateAt(victim.getLocation()).asFuture()
                        .thenAccept(blockState -> {
                            if (victim.isGliding() ||
                                    blockState.getType() == StateTypes.WATER ||
                                    blockState.getType() == StateTypes.LAVA ||
                                    blockState.getType() == StateTypes.COBWEB ||
                                    blockState.getType() == StateTypes.SCAFFOLDING)
                                return;

                            Vector3d adjustedVelocity;
                            if (victimPlayerData.isOnGroundClientSide(velocity.getY(), distanceToGround)) {
                                Integer damageTicks = victimPlayerData.getLastDamageTicks();
                                if (damageTicks != null && damageTicks > 8)
                                    return;

                                adjustedVelocity = velocity.withY(victimPlayerData.getVerticalVelocity());
                            }
                            else if (victimPlayerData.isOffGroundSyncEnabled())
                                adjustedVelocity = velocity.withY(victimPlayerData.getCompensatedOffGroundVelocity());
                            else
                                return;

                            victim.setVelocity(adjustedVelocity);
                        });
            });
    }
}