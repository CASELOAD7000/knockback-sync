package me.caseload.knockbacksync.stats.custom;

import me.caseload.knockbacksync.Base;

public class FabricStatsManager extends StatsManager {
    @Override
    public void init() {
        Base.INSTANCE.getScheduler().runTaskAsynchronously(() -> {
            BuildTypePie.determineBuildType(); // Function to calculate hash
            MetricsFabric metrics = new MetricsFabric(23568);
            metrics.addCustomChart(new PlayerVersionsPie());
            metrics.addCustomChart(new BuildTypePie());
            super.metrics = metrics;
        });
    }
}
