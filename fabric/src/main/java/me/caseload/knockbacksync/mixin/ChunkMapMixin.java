package me.caseload.knockbacksync.mixin;

import com.google.common.collect.Maps;
import me.caseload.knockbacksync.ConfigWrapper;
import me.caseload.knockbacksync.entity.EntityTickManager;
import me.caseload.knockbacksync.event.ConfigReloadEvent;
import me.caseload.knockbacksync.event.KBSyncEventHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.Optional;

@Mixin(ChunkMap.class)
public abstract class ChunkMapMixin {

    @Redirect(method = "addEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;updateInterval()I"))
    private int getCustomUpdateInterval(EntityType<?> entityType) {
        return EntityTickManager.getCustomUpdateInterval(entityType);
    }
}