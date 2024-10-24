package me.caseload.knockbacksync.entity;

import me.caseload.knockbacksync.ConfigWrapper;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.event.ConfigReloadEvent;
import me.caseload.knockbacksync.event.KBSyncEventHandler;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EntityTickManager {
    private static final Map<EntityType<?>, Integer> customTickIntervals = new HashMap<>();

    static {
        updateTickIntervals(KnockbackSyncBase.INSTANCE.getConfigManager().getConfigWrapper());
    }

    @KBSyncEventHandler
    public static void updateTickIntervals(ConfigReloadEvent event) {
        ConfigWrapper configWrapper = event.getConfigManager().getConfigWrapper();
        updateTickIntervals(configWrapper);
    }

    private static void updateTickIntervals(ConfigWrapper configWrapper) {
        customTickIntervals.clear();
        for (String entityKey : configWrapper.getKeys("entity_tick_intervals")) {
            try {
                Optional<EntityType<?>> entityType = EntityType.byString(entityKey.toLowerCase());
                if (entityType.isPresent()) {
                    int interval = configWrapper.getInt("entity_tick_intervals." + entityKey, entityType.get().updateInterval());
                    customTickIntervals.put(entityType.get(), interval);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid entity type in config: " + entityKey);
            }
        }
    }

    public static int getCustomUpdateInterval(EntityType<?> entityType) {
        return customTickIntervals.getOrDefault(entityType, entityType.updateInterval());
    }
}
