package me.caseload.knockbacksync.mixin;

import me.caseload.knockbacksync.entity.EntityTickManager;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {

    @Redirect(method = "addEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;updateInterval()I"))
    private int getCustomUpdateInterval(EntityType<?> entityType) {
        return EntityTickManager.getCustomUpdateInterval(entityType);
    }
}