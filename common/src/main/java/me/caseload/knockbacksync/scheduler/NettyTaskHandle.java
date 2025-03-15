package me.caseload.knockbacksync.scheduler;

import io.netty.util.concurrent.ScheduledFuture;

public class NettyTaskHandle implements AbstractTaskHandle {

    private final ScheduledFuture scheduledFuture;

    public NettyTaskHandle(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    @Override
    public boolean getCancelled() {
        return scheduledFuture.isCancelled();
    }

    @Override
    public void cancel() {
        scheduledFuture.cancel(true);
    }
}
