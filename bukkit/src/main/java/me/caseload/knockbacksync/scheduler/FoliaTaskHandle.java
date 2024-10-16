package me.caseload.knockbacksync.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class FoliaTaskHandle implements AbstractTaskHandle {
    private final ScheduledTask scheduledTask;
    private boolean cancelled;

    public FoliaTaskHandle(@NotNull ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    public Plugin getOwner() {
        return this.scheduledTask.getOwningPlugin();
    }

    @Override
    public boolean getCancelled() {
        return this.cancelled;
    }

    @Override
    public void cancel() {
        this.scheduledTask.cancel();
        this.cancelled = true;
    }
}
