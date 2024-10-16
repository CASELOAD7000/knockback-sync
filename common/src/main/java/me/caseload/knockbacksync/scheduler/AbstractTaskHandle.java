package me.caseload.knockbacksync.scheduler;

public interface AbstractTaskHandle {

    public boolean getCancelled();

    public void cancel();
}