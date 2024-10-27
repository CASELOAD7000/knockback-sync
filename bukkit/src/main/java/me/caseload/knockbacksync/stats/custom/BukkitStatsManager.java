package me.caseload.knockbacksync.stats.custom;

import me.caseload.knockbacksync.Base;
import org.bukkit.plugin.Plugin;

public class BukkitStatsManager extends StatsManager {

    public BukkitStatsManager(Plugin plugin) {
        super(new MetricsBukkit(plugin, 23568));
    }
}
