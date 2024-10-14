package me.caseload.knockbacksync.stats;

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
import me.caseload.knockbacksync.ConfigWrapper;
import me.caseload.knockbacksync.KnockbackSyncBase;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

public class MetricsFabric implements Metrics {

    private final MinecraftServer server;

    private final MetricsBase metricsBase;

    /**
     * Creates a new Metrics instance.
     *
     * @param server instance of MinecraftServer.
     * @param serviceId The id of the service. It can be found at <a
     *     href="https://bstats.org/what-is-my-plugin-id">What is my plugin id?</a>
     */
    public MetricsFabric(MinecraftServer server, int serviceId) {
        this.server = server;
        // Get the config file
        File bStatsFolder = new File(FabricLoader.getInstance().getConfigDir().toString(), "bStats");
        File configFile = new File(bStatsFolder, "config.yml");
        Map<String, Object> configMap = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try (InputStream stream = new FileInputStream(configFile)) {
            configMap = mapper.readValue(stream, Map.class);
        } catch (IOException e) {
            // Ignore, use defaults
        }

        ConfigWrapper config = new ConfigWrapper(configMap);

        if (!config.getBoolean("exists", false)) { // Check if the file has been created before
            config.set("enabled", true);
            config.set("serverUuid", UUID.randomUUID().toString());
            config.set("logFailedRequests", false);
            config.set("logSentData", false);
            config.set("logResponseStatusText", false);
            config.set("exists", true); // Mark the file as created

            try (OutputStream stream = new FileOutputStream(configFile)) {
                mapper.writeValue(stream, configMap);
            } catch (IOException e) {
                // Ignore
            }
        }

        // Load the data
        boolean enabled = config.getBoolean("enabled", true);
        String serverUUID = config.getString("serverUuid", UUID.randomUUID().toString()); // Provide default if not found
        boolean logErrors = config.getBoolean("logFailedRequests", false);
        boolean logSentData = config.getBoolean("logSentData", false);
        boolean logResponseStatusText = config.getBoolean("logResponseStatusText", false);
        metricsBase =
                new // See https://github.com/Bastian/bstats-metrics/pull/126
                        // See https://github.com/Bastian/bstats-metrics/pull/126
                        // See https://github.com/Bastian/bstats-metrics/pull/126
                        // See https://github.com/Bastian/bstats-metrics/pull/126
                        // See https://github.com/Bastian/bstats-metrics/pull/126
                        // See https://github.com/Bastian/bstats-metrics/pull/126
                        // See https://github.com/Bastian/bstats-metrics/pull/126
                        MetricsBase(
                        "bukkit",
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
        builder.appendField("onlineMode", server.usesAuthentication() ? 0 : 1);
        builder.appendField("bukkitVersion", "Fabric " + FabricLoader.getInstance().getModContainer("fabricloader").get().getMetadata().getVersion().getFriendlyString() + " (MC: " + server.getServerVersion() + ")");
        builder.appendField("bukkitName", "fabric");
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
        if (server != null && server.isRunning()) {
            return server.getPlayerCount();
        } else {
            return 0;
        }
    }
}
