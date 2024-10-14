package me.caseload.knockbacksync.stats.custom;

import me.caseload.knockbacksync.KnockbackSyncBase;

public class StatsManager {

    public static void init() {
        switch (KnockbackSyncBase.INSTANCE.platform) {
            case BUKKIT:
            case FOLIA:
                KnockbackSyncBase.INSTANCE.getScheduler().runTaskAsynchronously(() -> {
                    BuildTypePie.determineBuildType(); // Function to calculate hash
                    MetricsBukkit metrics = new MetricsBukkit(23568);
                    metrics.addCustomChart(new PlayerVersionsPie());
                    metrics.addCustomChart(new BuildTypePie());
                });
                break;
            case FABRIC:
                KnockbackSyncBase.INSTANCE.getScheduler().runTaskAsynchronously(() -> {
                    BuildTypePie.determineBuildType();
                    MetricsFabric metricsFabric = new MetricsFabric(23568);
                    metricsFabric.addCustomChart(new PlayerVersionsPie());
                    metricsFabric.addCustomChart(new BuildTypePie());
                });
                break;
        }
    }
}
