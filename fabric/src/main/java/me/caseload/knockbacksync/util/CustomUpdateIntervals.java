package me.caseload.knockbacksync.util;

import me.caseload.knockbacksync.ConfigWrapper;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.mixin.UpdateIntervalAccessor;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;

public class CustomUpdateIntervals {
    private static final Map<String, Integer> entityIntervals = new HashMap<>();

    public static void setInterval(String entityType, int interval) {
        entityIntervals.put(entityType, interval);
    }

    public static int getInterval(String entityType, int defaultInterval) {
        return entityIntervals.getOrDefault(entityType, defaultInterval);
    }

    public static void clear() {
        entityIntervals.clear();
    }

    public static void updateIntervals(ServerLevel level, Map<String, Integer> entityIntervals) {
        for (Entity entity : level.getAllEntities()) {
            String entityType = entity.getType().builtInRegistryHolder().getRegisteredName();
            if (entityIntervals.containsKey(entityType)) {
                ChunkMap.TrackedEntity serverEntity = ((ServerChunkCache) entity.level().getChunkSource()).chunkMap.entityMap.get(entity.getId());
                if (serverEntity != null) {
                    ((UpdateIntervalAccessor) serverEntity).setUpdateInterval(entityIntervals.get(entityType));
                }
            }
        }
    }

    public static Map<String, Integer> loadEntityIntervalsFromConfig() {
        Map<String, Integer> intervals = new HashMap<>();
        ConfigWrapper config = KnockbackSyncBase.INSTANCE.getConfigManager().getConfigWrapper();

        String basePath = "entity_update_intervals";
        if (config.contains(basePath)) {
            for (String entityType : config.getKeys(basePath)) {
                int interval = config.getInt(basePath + "." + entityType, -1);
                if (interval > 0) {
                    intervals.put(entityType, interval);
                }
            }
        }

        return intervals;
    }
}
