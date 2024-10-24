package me.caseload.knockbacksync.event;

public interface EventBus {
    void registerListeners(Object listener);
    void unregisterListeners(Object listener);
    void post(Event event);
}