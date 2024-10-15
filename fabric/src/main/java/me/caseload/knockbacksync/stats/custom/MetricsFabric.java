package me.caseload.knockbacksync.stats.custom;

/*
 * This Metrics class was auto-generated and can be copied into your project if you are
 * not using a build tool like Gradle or Maven for dependency management.
 *
 * IMPORTANT: You are not allowed to modify this class, except changing the package.
 *
 * Disallowed modifications include but are not limited to:
 *  - Remove the option for users to opt-out
 *  - Change the frequency for data submission
 *  - Obfuscate the code (every obfuscator should allow you to make an exception for specific files)
 *  - Reformat the code (if you use a linter, add an exception)
 *
 * Violations will result in a ban of your plugin and account from bStats.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.KnockbackSyncFabric;
import me.caseload.knockbacksync.stats.CustomChart;
import me.caseload.knockbacksync.stats.JsonObjectBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import java.io.*;
import java.util.UUID;
import java.util.logging.Level;

public class MetricsFabric implements Metrics {

    private final MetricsBase metricsBase;

    private static class Config {
        public boolean enabled = true;
        public String serverUuid;
        public boolean logFailedRequests = false;
        public boolean logSentData = false;
        public boolean logResponseStatusText = false;
    }

    /**
     * Creates a new Metrics instance.
     *
     * @param serviceId The id of the service. It can be found at <a
     *     href="https://bstats.org/what-is-my-plugin-id">What is my plugin id?</a>
     */
    public MetricsFabric(int serviceId) {
        // Get the config file
        File bStatsFolder = new File(FabricLoader.getInstance().getConfigDir().toString(), "bStats");
        File configFile = new File(bStatsFolder, "config.yml");
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Config config;

        try {
            if (!configFile.exists()) {
                bStatsFolder.mkdirs(); // Ensure the directory exists

                config = new Config();
                config.serverUuid = UUID.randomUUID().toString();

                // Add header as a comment
//                mapper.writeValue(configFile,
//                        "# bStats (https://bStats.org) collects some basic information for plugin authors, like how\n"
//                                + "# many people use their plugin and their total player count. It's recommended to keep bStats\n"
//                                + "# enabled, but if you're not comfortable with this, you can turn this setting off. There is no\n"
//                                + "# performance penalty associated with having metrics enabled, and data sent to bStats is fully\n"
//                                + "# anonymous.\n");
                mapper.writeValue(configFile, config); // Write the config object as YAML
            } else {
                config = mapper.readValue(configFile, Config.class);
            }
        } catch (IOException ignored) {
            // Handle the exception appropriately (e.g., log an error)
            config = new Config(); // Fallback to default values
            config.serverUuid = UUID.randomUUID().toString();
        }

        // Load the data
        boolean enabled = config.enabled;
        String serverUUID = config.serverUuid;
        boolean logErrors = config.logFailedRequests;
        boolean logSentData = config.logSentData;
        boolean logResponseStatusText = config.logResponseStatusText;

        metricsBase =
                new // See https://github.com/Bastian/bstats-metrics/pull/126
                        // See https://github.com/Bastian/bstats-metrics/pull/126
                        // See https://github.com/Bastian/bstats-metrics/pull/126
                        // See https://github.com/Bastian/bstats-metrics/pull/126
                        // See https://github.com/Bastian/bstats-metrics/pull/126
                        // See https://github.com/Bastian/bstats-metrics/pull/126
                        // See https://github.com/Bastian/bstats-metrics/pull/126
                        MetricsBase(
                        "fabric",
                        serverUUID,
                        serviceId,
                        enabled,
                        this::appendPlatformData,
                        this::appendServiceData,
                        submitDataTask -> KnockbackSyncBase.INSTANCE.getScheduler().runTask(submitDataTask),
                        () -> true,
                        (message, error) -> KnockbackSyncBase.INSTANCE.getLogger().log(Level.WARNING, message, error),
                        (message) -> KnockbackSyncBase.INSTANCE.getLogger().log(Level.INFO, message),
                        logErrors,
                        logSentData,
                        logResponseStatusText,
                        false);
    }

    /** Shuts down the underlying scheduler service. */
    public void shutdown() {
        metricsBase.shutdown();
    }

    /**
     * Adds a custom chart.
     *
     * @param chart The chart to add.
     */
    public void addCustomChart(CustomChart chart) {
        metricsBase.addCustomChart(chart);
    }

    private void appendPlatformData(JsonObjectBuilder builder) {
        builder.appendField("playerAmount", getPlayerAmount());
        builder.appendField("onlineMode", KnockbackSyncFabric.getServer().usesAuthentication() ? 0 : 1);
        builder.appendField("bukkitVersion", "Fabric " + FabricLoader.getInstance().getModContainer("fabricloader").get().getMetadata().getVersion().getFriendlyString() + " (MC: " + KnockbackSyncFabric.getServer().getServerVersion() + ")");
        builder.appendField("bukkitName", "Fabric");
        builder.appendField("javaVersion", System.getProperty("java.version"));
        builder.appendField("osName", System.getProperty("os.name"));
        builder.appendField("osArch", System.getProperty("os.arch"));
        builder.appendField("osVersion", System.getProperty("os.version"));
        builder.appendField("coreCount", Runtime.getRuntime().availableProcessors());
    }

    private void appendServiceData(JsonObjectBuilder builder) {
        builder.appendField("pluginVersion", FabricLoader.getInstance().getModContainer("knockbacksync").get().getMetadata().getVersion().getFriendlyString());
    }

    private int getPlayerAmount() {
        if (KnockbackSyncFabric.getServer().isRunning()) {
            return KnockbackSyncFabric.getServer().getPlayerCount();
        } else {
            return 0;
        }
    }
}
