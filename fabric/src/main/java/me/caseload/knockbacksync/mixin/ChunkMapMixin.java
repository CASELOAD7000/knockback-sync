package me.caseload.knockbacksync.mixin;

import me.caseload.knockbacksync.entity.EntityTickManager;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerChunkLoadingManager.class)
public abstract class ChunkMapMixin {

    @Redirect(method = "loadEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityType;getTrackTickInterval()I"))
    private int getCustomUpdateInterval(EntityType<?> entityType) {
        return EntityTickManager.getCustomUpdateInterval(entityType);
    }
}