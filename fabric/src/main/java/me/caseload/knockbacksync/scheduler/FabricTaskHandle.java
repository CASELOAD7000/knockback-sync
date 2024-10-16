package me.caseload.knockbacksync.scheduler;

public class FabricTaskHandle implements AbstractTaskHandle {
    private Runnable cancellationTask;
    private boolean cancelled = false;

    public FabricTaskHandle(Runnable cancellationTask) {
        this.cancellationTask = cancellationTask;
    }

    @Override
    public boolean getCancelled() {
        return this.cancelled;
    }

    @Override
    public void cancel() {
        this.cancellationTask.run();
        this.cancelled = true;
    }
}
