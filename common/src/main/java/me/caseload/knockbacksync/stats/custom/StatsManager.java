package me.caseload.knockbacksync.stats.custom;

import lombok.Getter;

@Getter
public abstract class StatsManager {

    Metrics metrics;

    public void init() {
        throw new IllegalStateException("Empty stats class!");
    }
}
