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
        return new BukkitTaskHandle(scheduler.runTask(plugin, task));
    }

    @Override
    public AbstractTaskHandle runTaskAsynchronously(Runnable task) {
        return new BukkitTaskHandle(scheduler.runTaskAsynchronously(plugin, task));
    }

    @Override
    public AbstractTaskHandle runTaskLater(Runnable task, long delayTicks) {
        return new BukkitTaskHandle(scheduler.runTaskLater(plugin, task, delayTicks));
    }

    @Override
    public AbstractTaskHandle runTaskTimer(Runnable task, long delayTicks, long periodTicks) {
        return new BukkitTaskHandle(scheduler.runTaskTimer(plugin, task, delayTicks, periodTicks));
    }

    @Override
    public AbstractTaskHandle runTaskLaterAsynchronously(Runnable task, long delay) {
        return new BukkitTaskHandle(scheduler.runTaskLaterAsynchronously(plugin, task, delay));
    }

    @Override
    public AbstractTaskHandle runTaskTimerAsynchronously(Runnable task, long delay, long period) {
        return new BukkitTaskHandle(scheduler.runTaskTimerAsynchronously(plugin, task, delay, period));
    }

    // Bukkit should take care of this for us automatically
    @Override
    public void shutdown() {

    }
}