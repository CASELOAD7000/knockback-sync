package me.caseload.knockbacksync.stats.custom;

import me.caseload.knockbacksync.KnockbackSyncBase;

public class BukkitStatsManager extends StatsManager {
    @Override
    public void init() {
        KnockbackSyncBase.INSTANCE.getScheduler().runTaskAsynchronously(() -> {
            BuildTypePie.determineBuildType(); // Function to calculate hash
            MetricsBukkit metrics = new MetricsBukkit(23568);
            metrics.addCustomChart(new PlayerVersionsPie());
            metrics.addCustomChart(new BuildTypePie());
        });
    }
}
