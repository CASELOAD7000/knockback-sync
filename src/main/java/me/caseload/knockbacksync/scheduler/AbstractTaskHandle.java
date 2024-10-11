package me.caseload.knockbacksync.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class AbstractTaskHandle {
    private BukkitTask bukkitTask;
    private ScheduledTask scheduledTask;
    private Runnable cancellationTask;

    public AbstractTaskHandle(Runnable cancellationTask) {
        this.cancellationTask = cancellationTask;
    }

    public AbstractTaskHandle(@NotNull BukkitTask bukkitTask) {
        this.bukkitTask = bukkitTask;
    }

    public AbstractTaskHandle(@NotNull ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    public Plugin getOwner() {
        return this.bukkitTask != null ? this.bukkitTask.getOwner() : this.scheduledTask.getOwningPlugin();
    }

    public boolean isCancelled() {
        return this.bukkitTask != null ? this.bukkitTask.isCancelled() : this.scheduledTask.isCancelled();
    }

    public void cancel() {
        if (this.bukkitTask != null) {
            this.bukkitTask.cancel();
        } else if (this.cancellationTask != null) {
            this.cancellationTask.run();
        } else {
            this.scheduledTask.cancel();
        }

    }
}