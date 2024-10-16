package me.caseload.knockbacksync.mixin;

import me.caseload.knockbacksync.callback.PlayerVelocityEvent;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundSetEntityMotionPacket;<init>(Lnet/minecraft/world/entity/Entity;)V"), cancellable = true)
    private void onAttack(Entity target, CallbackInfo ci) {
        if (target instanceof ServerPlayer serverPlayer && target.hurtMarked) {
            Vec3 velocity = target.getDeltaMovement();

            InteractionResult result = PlayerVelocityEvent.EVENT.invoker().onVelocityChange(serverPlayer, velocity);

            if (result == InteractionResult.FAIL) {
                target.hurtMarked = false;
                ci.cancel(); // Prevent sending the velocity packet
            } else if (result == InteractionResult.SUCCESS) {
                // Currently unnecessary since we do this in the handler, will move later
                target.setDeltaMovement(velocity);
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(target));
                target.hurtMarked = false;
                ci.cancel(); // Prevent sending the original velocity packet
            }
        }
    }
}
