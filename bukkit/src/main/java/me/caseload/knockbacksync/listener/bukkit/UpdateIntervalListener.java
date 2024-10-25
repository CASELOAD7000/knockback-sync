package me.caseload.knockbacksync.listener.bukkit;

import me.caseload.knockbacksync.Base;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class UpdateIntervalListener implements Listener {
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            // Schedule the modification to happen after the entity is fully spawned
            Base.INSTANCE.getScheduler().runTaskLater(() -> {
                modifyUpdateInterval(entity);
            }, 1L);
        }
    }

    private void modifyUpdateInterval(Entity entity) {
        try {
            Object nmsEntity = getHandle(entity);
            // Get the tracker field
            Field trackerField = nmsEntity.getClass().getDeclaredField("tracker");
            trackerField.setAccessible(true);
            Object tracker = trackerField.get(nmsEntity);

            // Get the updateInterval field
            Field updateIntervalField = tracker.getClass().getDeclaredField("updateInterval");
            updateIntervalField.setAccessible(true);

            // Modify the update interval
            updateIntervalField.set(tracker, 1);
        } catch (Exception e) {
            Base.LOGGER.warning("Failed to modify update interval: " + e.getMessage());
        }
    }

    private Object getHandle(Entity entity) throws Exception {
        Method getHandle = entity.getClass().getMethod("getHandle");
        return getHandle.invoke(entity);
    }
}
