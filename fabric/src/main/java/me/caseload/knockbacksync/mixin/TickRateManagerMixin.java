package me.caseload.knockbacksync.mixin;

import me.caseload.knockbacksync.callback.TickRateChangeEvent;
import net.minecraft.world.tick.TickManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TickManager.class)
public class TickRateManagerMixin {
    @Shadow
    protected float tickRate;

    @Inject(method = "setTickRate", at = @At("HEAD"))
    private void onSetTickRate(float tickRate, CallbackInfo ci) {
        float oldTickRate = this.tickRate;
        TickRateChangeEvent.EVENT.invoker().onTickRateChange(oldTickRate, tickRate);
    }
}