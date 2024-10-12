package me.caseload.knockbacksync.scheduler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.caseload.knockbacksync.KnockbacksyncFabric;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

public class FabricSchedulerAdapter implements SchedulerAdapter {
    private final MinecraftServer server;
    private final List<ScheduledTask> taskList;

    public FabricSchedulerAdapter() {
        this.server = KnockbacksyncFabric.server;
        this.taskList = new ArrayList<>();
        ServerTickEvents.END_SERVER_TICK.register(this::handleTasks);
    }

    private void handleTasks(MinecraftServer server) {
        Iterator<ScheduledTask> iterator = taskList.iterator();
        while (iterator.hasNext()) {
            ScheduledTask task = iterator.next();
            if (server.getTickCount() >= task.nextRunTick) {
                task.task.run();
                if (task.isPeriodic) {
                    task.nextRunTick = server.getTickCount() + task.period;
                } else {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public AbstractTaskHandle runTask(Runnable task) {
        ScheduledTask scheduledTask = new ScheduledTask(task, server.getTickCount(), 0, false);
        taskList.add(scheduledTask);
        return new AbstractTaskHandle(() -> taskList.remove(scheduledTask));
    }

    @Override
    public AbstractTaskHandle runTaskAsynchronously(Runnable task) {
        Thread thread = new Thread(task);
        thread.start();
        return new AbstractTaskHandle(() -> thread.interrupt());
    }

    @Override
    public AbstractTaskHandle runTaskLater(Runnable task, long delayTicks) {
        ScheduledTask scheduledTask = new ScheduledTask(task, server.getTickCount() + delayTicks, 0, false);
        taskList.add(scheduledTask);
        return new AbstractTaskHandle(() -> taskList.remove(scheduledTask));
    }

    @Override
    public AbstractTaskHandle runTaskTimer(Runnable task, long delayTicks, long periodTicks) {
        ScheduledTask scheduledTask = new ScheduledTask(task, server.getTickCount() + delayTicks, periodTicks, true);
        taskList.add(scheduledTask);
        return new AbstractTaskHandle(() -> taskList.remove(scheduledTask));
    }

    @Override
    public AbstractTaskHandle runTaskLaterAsynchronously(Runnable task, long delay) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(delay * 50); // Convert ticks to milliseconds
                task.run();
            } catch (InterruptedException e) {
                // Handle interruption
            }
        });
        thread.start();
        return new AbstractTaskHandle(() -> thread.interrupt());
    }

    @Override
    public AbstractTaskHandle runTaskTimerAsynchronously(Runnable task, long delay, long period) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(delay * 50); // Convert ticks to milliseconds
                while (!Thread.currentThread().isInterrupted()) {
                    task.run();
                    Thread.sleep(period * 50); // Convert ticks to milliseconds
                }
            } catch (InterruptedException e) {
                // Handle interruption
            }
        });
        thread.start();
        return new AbstractTaskHandle(() -> thread.interrupt());
    }

    private static class ScheduledTask {
        final Runnable task;
        long nextRunTick;
        final long period;
        final boolean isPeriodic;

        ScheduledTask(Runnable task, long nextRunTick, long period, boolean isPeriodic) {
            this.task = task;
            this.nextRunTick = nextRunTick;
            this.period = period;
            this.isPeriodic = isPeriodic;
        }
    }
}