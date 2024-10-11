package me.caseload.knockbacksync.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class BukkitSchedulerAdapter implements SchedulerAdapter {
    private final Plugin plugin;
    private final BukkitScheduler scheduler;

    public BukkitSchedulerAdapter(Plugin plugin) {
        this.plugin = plugin;
        this.scheduler = Bukkit.getScheduler();
    }

    @Override
    public AbstractTaskHandle runTask(Runnable task) {
        return new AbstractTaskHandle(scheduler.runTask(plugin, task));
    }

    @Override
    public AbstractTaskHandle runTaskAsynchronously(Runnable task) {
        return new AbstractTaskHandle(scheduler.runTaskAsynchronously(plugin, task));
    }

    @Override
    public AbstractTaskHandle runTaskLater(Runnable task, long delayTicks) {
        return new AbstractTaskHandle(scheduler.runTaskLater(plugin, task, delayTicks));
    }

    @Override
    public AbstractTaskHandle runTaskTimer(Runnable task, long delayTicks, long periodTicks) {
        return new AbstractTaskHandle(scheduler.runTaskTimer(plugin, task, delayTicks, periodTicks));
    }

    @Override
    public AbstractTaskHandle runTaskLaterAsynchronously(Runnable task, long delay) {
        return new AbstractTaskHandle(scheduler.runTaskLaterAsynchronously(plugin, task, delay));
    }

    @Override
    public AbstractTaskHandle runTaskTimerAsynchronously(Runnable task, long delay, long period) {
        return new AbstractTaskHandle(scheduler.runTaskTimerAsynchronously(plugin, task, delay, period));
    }
}