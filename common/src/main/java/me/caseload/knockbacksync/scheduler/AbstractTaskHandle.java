package me.caseload.knockbacksync.scheduler;

public interface AbstractTaskHandle {

    boolean getCancelled();

    void cancel();
}