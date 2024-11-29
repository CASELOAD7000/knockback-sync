package me.caseload.knockbacksync.scheduler;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class FoliaTaskHandle implements AbstractTaskHandle {
    private static final MethodHandle GET_OWNING_PLUGIN;
    private static final MethodHandle CANCEL;

    static {
        try {
            Class<?> scheduledTaskClass = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            GET_OWNING_PLUGIN = lookup.findVirtual(scheduledTaskClass, "getOwningPlugin", MethodType.methodType(Plugin.class));
            CANCEL = lookup.findVirtual(scheduledTaskClass, "cancel", MethodType.methodType(
                    Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask$CancelledState")));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("Failed to initialize FoliaTaskHandle reflection", e);
        }
    }

    private final Object scheduledTask; // Store as Object instead of ScheduledTask
    private boolean cancelled;

    public FoliaTaskHandle(@NotNull Object scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    public Plugin getOwner() {
        try {
            return (Plugin) GET_OWNING_PLUGIN.invoke(scheduledTask);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to invoke getOwningPlugin", e);
        }
    }

    @Override
    public boolean getCancelled() {
        return this.cancelled;
    }

    @Override
    public void cancel() {
        try {
            CANCEL.invoke(scheduledTask);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to invoke cancel", e);
        }
        this.cancelled = true;
    }
}