package me.caseload.knockbacksync.stats.custom;

import me.caseload.knockbacksync.Base;

public class FabricStatsManager extends StatsManager {
    public FabricStatsManager() {
        super(new MetricsFabric(23568));
    }
}
