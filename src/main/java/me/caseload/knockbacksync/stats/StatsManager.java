package me.caseload.knockbacksync.stats;

import me.caseload.knockbacksync.KnockbackSync;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;

public class StatsManager {

    public static Metrics metrics;

    public static void init() {
        Bukkit.getScheduler().runTaskAsynchronously(KnockbackSync.getInstance(), () -> {
            BuildTypePie.determineBuildType(); // Function to calculate hash
            metrics = new Metrics(KnockbackSync.INSTANCE, 23568);
            metrics.addCustomChart(new PlayerVersionsPie());
            metrics.addCustomChart(new BuildTypePie());
        });
    }
}
