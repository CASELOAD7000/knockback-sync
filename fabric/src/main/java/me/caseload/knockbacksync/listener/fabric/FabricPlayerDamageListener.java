package me.caseload.knockbacksync.listener.fabric;


import me.caseload.knockbacksync.listener.PlayerDamageListener;
import me.caseload.knockbacksync.player.FabricPlayer;
import me.caseload.knockbacksync.player.PlatformPlayer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class FabricPlayerDamageListener extends PlayerDamageListener {

    public void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, source) -> {
            onPlayerDamage((ServerPlayer) player, entity);
            return InteractionResult.PASS;
        });

        // Sending ping packets on tick if runnable is disabled
//        ServerTickEvents.END_SERVER_TICK.register(server -> {
//            if (!KnockbackSyncBase.INSTANCE.getConfigManager().isRunnableEnabled()) {
//                for (ServerPlayerEntity player : server.getPlayerList()) {
//                    PlayerData playerData = PlayerDataManager.getPlayerData(player);
//                    if (playerData != null) {
//                        playerData.sendPing();
//                    }
//                }
//            }
//        });
    }

    private void onPlayerDamage(ServerPlayer attacker, Entity victimEntity) {
        if (victimEntity instanceof Player victim) {
            PlatformPlayer platformAttacker = new FabricPlayer(attacker);
            PlatformPlayer platformVictim = new FabricPlayer((ServerPlayer) victim);
            super.onPlayerDamage(platformVictim, platformAttacker); // Call the shared method
        }
    }
}