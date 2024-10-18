package me.caseload.knockbacksync.scheduler;

import me.caseload.knockbacksync.KBSyncFabricLoaderMod;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.*;

public class FabricSchedulerAdapter implements SchedulerAdapter {
    //    private final MinecraftServer server;
    private final Map<ScheduledTask, Runnable> taskMap = new HashMap<>();
    private final Map<Thread, Runnable> asyncTasks = new HashMap<>();

    public FabricSchedulerAdapter() {
        ServerTickEvents.END_SERVER_TICK.register(this::handleTasks);
    }

    private void handleTasks(MinecraftServer server) {
        Iterator<ScheduledTask> iterator = taskMap.keySet().iterator();
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
        ScheduledTask scheduledTask = new ScheduledTask(task, KBSyncFabricLoaderMod.getServer().getTickCount(), 0, false);
        Runnable cancellationTask = () -> taskMap.remove(scheduledTask);
        taskMap.put(scheduledTask, cancellationTask);
        return new FabricTaskHandle(cancellationTask);
    }

    @Override
    public AbstractTaskHandle runTaskAsynchronously(Runnable task) {
        Thread thread = new Thread(task);
        Runnable cancellationTask = () -> {
            thread.interrupt();
            asyncTasks.remove(thread);
        };
        asyncTasks.put(thread, cancellationTask);
        thread.start();
        return new FabricTaskHandle(cancellationTask);
    }

    @Override
    public AbstractTaskHandle runTaskLater(Runnable task, long delayTicks) {
        ScheduledTask scheduledTask = new ScheduledTask(task, KBSyncFabricLoaderMod.getServer().getTickCount() + delayTicks, 0, false);
        Runnable cancellationTask = () -> taskMap.remove(scheduledTask);
        taskMap.put(scheduledTask, cancellationTask);
        return new FabricTaskHandle(cancellationTask);
    }

    @Override
    public AbstractTaskHandle runTaskTimer(Runnable task, long delayTicks, long periodTicks) {
        ScheduledTask scheduledTask = new ScheduledTask(task, KBSyncFabricLoaderMod.getServer().getTickCount() + delayTicks, periodTicks, true);
        Runnable cancellationTask = () -> taskMap.remove(scheduledTask);
        taskMap.put(scheduledTask, cancellationTask);
        return new FabricTaskHandle(cancellationTask);
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
        Runnable cancellationTask = () -> {
            thread.interrupt();
            asyncTasks.remove(thread);
        };
        asyncTasks.put(thread, cancellationTask);
        thread.start();
        return new FabricTaskHandle(cancellationTask);
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
        Runnable cancellationTask = () -> {
            thread.interrupt();
            asyncTasks.remove(thread);
        };
        asyncTasks.put(thread, cancellationTask);
        thread.start();
        return new FabricTaskHandle(cancellationTask);
    }

    @Override
    public void shutdown() {
        // Create a new list to store the tasks that need to be executed
        List<Runnable> tasksToExecute = new ArrayList<>();

        // Add the tasks from the taskMap to the tasksToExecute list
        tasksToExecute.addAll(taskMap.values());

        // Add the tasks from the asyncTasks to the tasksToExecute list
        tasksToExecute.addAll(asyncTasks.values());

        // Clear the taskMap and asyncTasks to avoid further modifications
        taskMap.clear();
        asyncTasks.clear();

        // Execute the tasks in the tasksToExecute list
        for (Runnable task : tasksToExecute) {
            task.run();
        }
    }

    private static class ScheduledTask {
        final Runnable task;
        final long period;
        final boolean isPeriodic;
        long nextRunTick;

        ScheduledTask(Runnable task, long nextRunTick, long period, boolean isPeriodic) {
            this.task = task;
            this.nextRunTick = nextRunTick;
            this.period = period;
            this.isPeriodic = isPeriodic;
        }
    }
}