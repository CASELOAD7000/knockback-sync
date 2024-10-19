package me.caseload.knockbacksync.scheduler;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class BukkitTaskHandle implements AbstractTaskHandle {
    private final BukkitTask bukkitTask;
    private boolean cancelled;

    public BukkitTaskHandle(@NotNull BukkitTask bukkitTask) {
        this.bukkitTask = bukkitTask;
    }

    public Plugin getOwner() {
        return this.bukkitTask.getOwner();
    }

    @Override
    public boolean getCancelled() {
        return this.cancelled;
    }

    @Override
    public void cancel() {
        this.bukkitTask.cancel();
        this.cancelled = true;
    }
}
