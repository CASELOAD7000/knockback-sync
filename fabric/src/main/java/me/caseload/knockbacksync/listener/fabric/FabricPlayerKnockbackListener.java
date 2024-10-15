package me.caseload.knockbacksync.listener.fabric;

import com.github.retrooper.packetevents.util.Vector3d;
import me.caseload.knockbacksync.callback.PlayerVelocityCallback;
import me.caseload.knockbacksync.listener.PlayerKnockbackListener;
import me.caseload.knockbacksync.player.FabricPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class FabricPlayerKnockbackListener extends PlayerKnockbackListener {

    public void register() {
        PlayerVelocityCallback.EVENT.register((player, velocity) -> {
            DamageSource lastDamageSource = player.getLastDamageSource();
            if (lastDamageSource == null)
                return InteractionResult.PASS;

            // Check if the damage is from a player attack
            if (!lastDamageSource.is(DamageTypes.PLAYER_ATTACK))
                return InteractionResult.PASS;

            onPlayerVelocity(new FabricPlayer(player), new Vector3d(velocity.x, velocity.y, velocity.z));
            // This SHOULD mean we return original velocity for Fabric (no modification here)
            // But since we set it ourselves due to following bukkit's design patterns and doing victim
            // We return a pass so velocity isn't set twice
            return InteractionResult.PASS;
        });
    }
}