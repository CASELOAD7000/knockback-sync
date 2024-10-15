package me.caseload.knockbacksync.stats.custom;

@FunctionalInterface
public interface PluginJarHashProvider {
    String getPluginJarHash() throws Exception;
}