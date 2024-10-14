//package me.caseload.knockbacksync.mixin;
//
//import net.minecraft.server.network.EntityTrackerEntry;
//import net.minecraft.world.entity.ai.behavior.EntityTracker;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//@Mixin(EntityTracker.class)
//public class EntityTrackerEntryMixin {
//
//    @Inject(method = "sendVelocityUpdates", at = @At("HEAD"), cancellable = true)
//    private void onSendVelocityUpdates(CallbackInfo ci) {
//        // Your custom logic here
//        System.out.println("EntityTrackerEntry velocity update");
//    }
//}