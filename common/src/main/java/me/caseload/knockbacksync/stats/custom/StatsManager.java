package me.caseload.knockbacksync.stats.custom;

import me.caseload.knockbacksync.KnockbackSyncBase;

public abstract class StatsManager {

    Metrics metrics;

    public Metrics getMetrics() {
        return metrics;
    }

    public void init() {
        throw new IllegalStateException("Empty stats class!");
    }
}
