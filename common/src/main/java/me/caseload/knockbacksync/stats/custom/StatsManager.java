package me.caseload.knockbacksync.stats.custom;

public abstract class StatsManager {

    Metrics metrics;

    public Metrics getMetrics() {
        return metrics;
    }

    public void init() {
        throw new IllegalStateException("Empty stats class!");
    }
}
