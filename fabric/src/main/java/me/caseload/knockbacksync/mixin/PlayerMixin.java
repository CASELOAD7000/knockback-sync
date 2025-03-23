package me.caseload.knockbacksync.mixin;

import me.caseload.knockbacksync.callback.PlayerVelocityEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerMixin {
    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/EntityVelocityUpdateS2CPacket;<init>(Lnet/minecraft/entity/Entity;)V"), cancellable = true)
    private void onAttack(Entity target, CallbackInfo ci) {
        if (target instanceof ServerPlayerEntity serverPlayer && target.velocityModified) {
            Vec3d velocity = target.getVelocity();

            ActionResult result = PlayerVelocityEvent.EVENT.invoker().onVelocityChange(serverPlayer, velocity);

            if (result == ActionResult.FAIL) {
                target.velocityModified = false;
                ci.cancel(); // Prevent sending the velocity packet
            } else if (result == ActionResult.SUCCESS) {
                // Currently unnecessary since we do this in the handler, will move later
                target.setVelocity(velocity);
                serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(target));
                target.velocityModified = false;
                ci.cancel(); // Prevent sending the original velocity packet
            }
        }
    }
}
