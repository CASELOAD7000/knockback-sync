package me.caseload.knockbacksync.stats.custom;

import me.caseload.knockbacksync.stats.CustomChart;

public interface Metrics {

    void addCustomChart(CustomChart chart);
    void shutdown();
}
