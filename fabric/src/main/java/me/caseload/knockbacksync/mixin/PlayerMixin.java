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

    @Inject(method = "knockbackTarget",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"),
            cancellable = true)
    private void onKnockback(Entity target, float strength, Vec3d playerTargetVelocity, CallbackInfo ci) {
        if (target instanceof ServerPlayerEntity serverPlayer && target.knockedBack) {

            // 1. Get the "Proposed" Velocity (The knockback calculated lines above)
            Vec3d proposedVelocity = target.getVelocity();

            // 2. Fire the Event
            ActionResult result = PlayerVelocityEvent.EVENT.invoker().onVelocityChange(serverPlayer, proposedVelocity);

            if (result == ActionResult.FAIL) {
                // --- REPLICATING PAPER: CANCELLED ---
                // In Paper: if (event.isCancelled()) { cancelled = true; }
                // In Paper: if (!cancelled) { sendPacket; cleanup; }

                // So, if cancelled, we stop EVERYTHING. We do not send packet, we do not reset velocity.
                ci.cancel();
                return;
            }

            if (result == ActionResult.SUCCESS) {
                // --- REPLICATING PAPER: MODIFIED ---
                // In Paper: if (!velocity.equals(event.getVelocity())) { player.setVelocity(...) }
                // In Paper: if (!cancelled) { sendPacket; setDeltaMovement(currentMovement); }

                // 1. The event listener should have already set the new velocity on the entity.
                // 2. We must send a NEW packet (because the one on the stack is old).
                serverPlayer.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(target));

                // 3. Replicate the Cleanup: Paper resets velocity to 'currentMovement' (playerTargetVelocity)
                // This prevents the server from thinking the player is flying, maintaining Client Authority.
                target.knockedBack = false;
                target.setVelocity(playerTargetVelocity);

                // 4. Cancel the original logic so Vanilla doesn't send the old packet or do double cleanup.
                ci.cancel();
            }

            // CASE: PASS
            // Vanilla behaves normally (Sends packet, does cleanup).
        }
    }
}