package me.caseload.knockbacksync.scheduler;

import io.github.retrooper.packetevents.util.folia.TaskWrapper;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
public class FoliaTaskHandle implements AbstractTaskHandle {

    private final TaskWrapper scheduledTask; // Store as Object instead of ScheduledTask
    private boolean cancelled;

    public FoliaTaskHandle(@NotNull TaskWrapper scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    public Plugin getOwner() {
        return scheduledTask.getOwner();
    }

    @Override
    public boolean getCancelled() {
        return this.cancelled;
    }

    @Override
    public void cancel() {
        scheduledTask.cancel();
        this.cancelled = true;
    }
}