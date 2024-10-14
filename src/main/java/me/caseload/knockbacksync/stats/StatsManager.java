package me.caseload.knockbacksync.stats;

import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.KnockbackSyncFabric;
import me.caseload.knockbacksync.KnockbackSyncPlugin;

public class StatsManager {

    public static void init() {
        switch (KnockbackSyncBase.INSTANCE.platform) {
            case BUKKIT:
            case FOLIA:
                KnockbackSyncBase.INSTANCE.getScheduler().runTaskAsynchronously(() -> {
                    BuildTypePie.determineBuildType(); // Function to calculate hash
                    MetricsBukkit metrics = new MetricsBukkit(KnockbackSyncPlugin.getPlugin(KnockbackSyncPlugin.class), 23568);
                    metrics.addCustomChart(new PlayerVersionsPie());
                    metrics.addCustomChart(new BuildTypePie());
                });
            case FABRIC:
                KnockbackSyncBase.INSTANCE.getScheduler().runTaskAsynchronously(() -> {
                    BuildTypePie.determineBuildType();
                    MetricsFabric metricsFabric = new MetricsFabric(KnockbackSyncFabric.server, 23568);
                    metricsFabric.addCustomChart(new PlayerVersionsPie());
                    metricsFabric.addCustomChart(new BuildTypePie());
                });
        }
    }
}
