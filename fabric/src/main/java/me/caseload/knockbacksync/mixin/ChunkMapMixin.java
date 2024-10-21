package me.caseload.knockbacksync.mixin;

import com.google.common.collect.Maps;
import me.caseload.knockbacksync.ConfigWrapper;
import me.caseload.knockbacksync.event.ConfigReloadEvent;
import me.caseload.knockbacksync.event.KBSyncEventHandler;
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

//    @Shadow public Map<Integer, ChunkMap.TrackedEntity> entityMap;

    // Store custom tick intervals loaded from config
    private static final Map<EntityType<?>, Integer> customTickIntervals = Maps.newHashMap();

    // Method to update customTickIntervals from your config
    @KBSyncEventHandler
    public void updateTickIntervals(ConfigReloadEvent configReloadEvent) {
        ConfigWrapper configWrapper = configReloadEvent.getConfigManager().getConfigWrapper();
        customTickIntervals.clear();
        for (String entityKey : configWrapper.getKeys("entity_tick_intervals")) {
            try {
                Optional<EntityType<?>> entityType = EntityType.byString(entityKey.toUpperCase());
                if (entityType.isPresent()) {
                    int interval = configWrapper.getInt("entity_tick_intervals." + entityKey, entityType.get().updateInterval());
                    customTickIntervals.put(entityType.get(), interval);
                }
            } catch (IllegalArgumentException e) {
                // Handle invalid entity type in config (e.g., log a warning)
                System.err.println("Invalid entity type in config: " + entityKey);
            }
        }
    }

    @Redirect(method = "addEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;updateInterval()I"))
    private int getCustomUpdateInterval(EntityType<?> entityType) {
        // Check if we have a custom interval for this entity type
        return customTickIntervals.getOrDefault(entityType, entityType.updateInterval());
    }
}