package me.caseload.knockbacksync.mixin;

import me.caseload.knockbacksync.callback.PlayerVelocityCallback;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntity.class)
public class ServerEntityMixin {


    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "sendChanges", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/Entity;hurtMarked:Z", ordinal = 1))
    private void onSendChanges(CallbackInfo ci) {
        if (this.entity.hurtMarked && this.entity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) this.entity;
            Vec3 velocity = player.getDeltaMovement();

            InteractionResult result = PlayerVelocityCallback.EVENT.invoker().onVelocityChange(player, velocity);

            if (result == InteractionResult.FAIL) {
                this.entity.hurtMarked = false;
                ci.cancel();
            } else if (result == InteractionResult.SUCCESS) {
                // Currently unnecessary since we do this in the handler, will move later
                player.setDeltaMovement(velocity);
            }
        }
    }
}