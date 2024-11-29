package me.caseload.knockbacksync.scheduler;

import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import io.github.retrooper.packetevents.util.folia.GlobalRegionScheduler;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class FoliaSchedulerAdapter implements SchedulerAdapter {
    private final Plugin plugin;

    public FoliaSchedulerAdapter(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public AbstractTaskHandle runTask(Runnable task) {
        return new FoliaTaskHandle(FoliaScheduler.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run()));
    }

    @Override
    public AbstractTaskHandle runTaskAsynchronously(Runnable task) {
        return new FoliaTaskHandle(FoliaScheduler.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run()));
    }

    @Override
    public AbstractTaskHandle runTaskLater(Runnable task, long delayTicks) {
        return new FoliaTaskHandle(FoliaScheduler.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delayTicks));
    }

    @Override
    public AbstractTaskHandle runTaskTimer(Runnable task, long delayTicks, long periodTicks) {
        return new FoliaTaskHandle(FoliaScheduler.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), delayTicks, periodTicks));
    }

    @Override
    public AbstractTaskHandle runTaskLaterAsynchronously(Runnable task, long delay) {
        return new FoliaTaskHandle(FoliaScheduler.getAsyncScheduler().runDelayed(plugin, scheduledTask -> task.run(), delay * 50, TimeUnit.MILLISECONDS));
    }

    @Override
    public AbstractTaskHandle runTaskTimerAsynchronously(Runnable task, long delay, long period) {
        return new FoliaTaskHandle(FoliaScheduler.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), delay, period));
    }

    // Folia takes care of this
    @Override
    public void shutdown() {

    }
}