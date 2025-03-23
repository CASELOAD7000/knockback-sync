package me.caseload.knockbacksync.listener.fabric;

import com.github.retrooper.packetevents.util.Vector3d;
import me.caseload.knockbacksync.callback.PlayerVelocityEvent;
import me.caseload.knockbacksync.listener.PlayerKnockbackListener;
import me.caseload.knockbacksync.player.FabricPlayer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.util.ActionResult;

public class FabricPlayerKnockbackListener extends PlayerKnockbackListener {

    public void register() {
        PlayerVelocityEvent.EVENT.register((player, velocity) -> {
            DamageSource lastDamageSource = player.getRecentDamageSource();
            if (lastDamageSource == null)
                return ActionResult.PASS;

            // Check if the damage is from a player attack
            if (!lastDamageSource.isOf(DamageTypes.PLAYER_ATTACK))
                return ActionResult.PASS;

            onPlayerVelocity(new FabricPlayer(player), new Vector3d(velocity.x, velocity.y, velocity.z));
            // This SHOULD mean we return original velocity for Fabric (no modification here)
            // But since we set it ourselves due to following bukkit's design patterns and doing victim
            // We return a pass so velocity isn't set twice
            return ActionResult.PASS;
        });
    }
}