package me.caseload.knockbacksync.mixin;

import me.caseload.knockbacksync.callback.PlayerVelocityEvent;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public class ServerEntityMixin {

    @Shadow
    @Final
    private Entity entity;

    // Velocity event
    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;velocityModified:Z", ordinal = 1), cancellable = true)
    private void onSendChanges(CallbackInfo ci) {
        if (this.entity.velocityModified && this.entity instanceof ServerPlayerEntity player) {
            Vec3d velocity = player.getVelocity();

            ActionResult result = PlayerVelocityEvent.EVENT.invoker().onVelocityChange(player, velocity);

            if (result == ActionResult.FAIL) {
                this.entity.velocityModified = false;
                ci.cancel();
            } else if (result == ActionResult.SUCCESS) {
                // Currently unnecessary since we do this in the handler, will move later
                player.setVelocity(velocity);
            }
        }
    }

    // Custom tick intervals
//    @Shadow
//    private int updateInterval;

//    @Accessor("entity")
//    Entity getEntity() {
//        return null;
//    }

//    @Invoker("sendChanges")
//    void invokeSendChanges() {
//
//    }

//    @Override
//    public void setUpdateInterval(int interval) {
//        this.updateInterval = interval;
//    }
}