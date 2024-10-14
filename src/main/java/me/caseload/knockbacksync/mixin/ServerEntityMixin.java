package me.caseload.knockbacksync.mixin;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//@Mixin(ServerEntity.class)
//public class ServerEntityMixin {
//
//    @Inject(method = "sendChanges", at = @At("HEAD"), cancellable = true)
//    private void onSendChanges(CallbackInfo ci) {
//        // Your custom logic here
//        System.out.println("ServerEntity sendChanges called");
//
//        // Example: Modify the entity's velocity
//        Entity entity = ((ServerEntity) (Object) this).getEntity();
//        Vec3 velocity = entity.getDeltaMovement();
//
//        // Print the current velocity
//        System.out.println("Current velocity: " + velocity);
//
//        // Modify the velocity if needed
//        // entity.setDeltaMovement(new Vec3(x, y, z));
//    }
//}
