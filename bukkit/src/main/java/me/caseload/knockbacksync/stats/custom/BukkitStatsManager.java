package me.caseload.knockbacksync.stats.custom;

import me.caseload.knockbacksync.Base;

public class BukkitStatsManager extends StatsManager {

    public BukkitStatsManager() {
        super(new MetricsBukkit(23568));
    }
}
