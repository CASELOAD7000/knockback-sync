package me.caseload.knockbacksync.mixin;

import me.caseload.knockbacksync.callback.PlayerVelocityEvent;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {

    @Shadow @Final private Entity entity;

    // We inject exactly where 'this.entity.knockedBack = false' is about to happen.
    @Inject(method = "tick",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/entity/Entity;knockedBack:Z",
                    opcode = Opcodes.PUTFIELD),
            cancellable = true)
    private void onKnockbackSync(CallbackInfo ci) {
        // This check ensures we are in the "if (entity.knockedBack)" block
        // and operating on a Player.
        if (this.entity instanceof ServerPlayerEntity serverPlayer) {

            // 1. Capture the velocity CURRENTLY on the player
            Vec3d currentVelocity = serverPlayer.getVelocity();

            // 2. Fire the event
            ActionResult result = PlayerVelocityEvent.EVENT.invoker().onVelocityChange(serverPlayer, currentVelocity);

            if (result == ActionResult.FAIL) {
                // CASE: CANCELLED
                // We want to STOP the packet from sending.
                // We also want to keep 'knockedBack' as true (technically),
                // effectively acting like the block never happened.
                ci.cancel();
            }
            else if (result == ActionResult.SUCCESS) {
                // CASE: MODIFIED
                // The event handler has already updated the player's velocity:
                // player.setVelocity(newVel);

                // We do NOT cancel. We let Vanilla proceed.
                // Vanilla will:
                // 1. Set knockedBack = false;
                // 2. Create a NEW packet reading the current velocity (which is now your custom velocity).
                // 3. Send it.
            }
        }
    }
}