package me.caseload.knockbacksync.listener.fabric;


import me.caseload.knockbacksync.listener.PlayerDamageListener;
import me.caseload.knockbacksync.player.FabricPlayer;
import me.caseload.knockbacksync.player.PlatformPlayer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

public class FabricPlayerDamageListener extends PlayerDamageListener {

    public void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, source) -> {
            onPlayerDamage((ServerPlayerEntity) player, entity);
            return ActionResult.PASS;
        });
    }

    private void onPlayerDamage(ServerPlayerEntity attacker, Entity victimEntity) {
        if (victimEntity instanceof PlayerEntity victim) {
            PlatformPlayer platformAttacker = new FabricPlayer(attacker);
            PlatformPlayer platformVictim = new FabricPlayer((ServerPlayerEntity) victim);
            super.onPlayerDamage(platformVictim, platformAttacker); // Call the shared method
        }
    }
}