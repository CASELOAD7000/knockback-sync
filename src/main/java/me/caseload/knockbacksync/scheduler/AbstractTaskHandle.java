package me.caseload.knockbacksync.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class AbstractTaskHandle {
    private BukkitTask bukkitTask;
    private ScheduledTask scheduledTask;
    private Runnable cancellationTask;
    @Getter
    private boolean cancelled = false;

    public AbstractTaskHandle(Runnable cancellationTask) {
        this.cancellationTask = cancellationTask;
    }

    public AbstractTaskHandle(@NotNull BukkitTask bukkitTask) {
        this.bukkitTask = bukkitTask;
    }

    public AbstractTaskHandle(@NotNull ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    // Should never be called on Fabric
    public Plugin getOwner() {
        return this.bukkitTask != null ? this.bukkitTask.getOwner() : this.scheduledTask.getOwningPlugin();
    }

    public void cancel() {
        if (this.bukkitTask != null) {
            this.bukkitTask.cancel();
        } else if (this.cancellationTask != null) {
            this.cancellationTask.run();
        } else {
            this.scheduledTask.cancel();
        }
        cancelled = true;
    }
}