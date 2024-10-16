package me.caseload.knockbacksync.scheduler;

public interface SchedulerAdapter {
    AbstractTaskHandle runTask(Runnable task);

    AbstractTaskHandle runTaskAsynchronously(Runnable task);

    AbstractTaskHandle runTaskLater(Runnable task, long delayTicks);

    AbstractTaskHandle runTaskTimer(Runnable task, long delayTicks, long periodTicks);

    AbstractTaskHandle runTaskLaterAsynchronously(Runnable task, long delay);

    AbstractTaskHandle runTaskTimerAsynchronously(Runnable task, long delay, long period);

    void shutdown();
}