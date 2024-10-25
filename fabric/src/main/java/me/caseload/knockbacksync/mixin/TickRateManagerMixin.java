package me.caseload.knockbacksync.mixin;

import me.caseload.knockbacksync.callback.TickRateChangeEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.TickRateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TickRateManager.class)
public class TickRateManagerMixin {
    @Shadow
    protected float tickrate;

    @Inject(method = "setTickRate", at = @At("HEAD"))
    private void onSetTickRate(float tickRate, CallbackInfo ci) {
        float oldTickRate = this.tickrate;
        TickRateChangeEvent.EVENT.invoker().onTickRateChange(oldTickRate, tickRate);
    }
}