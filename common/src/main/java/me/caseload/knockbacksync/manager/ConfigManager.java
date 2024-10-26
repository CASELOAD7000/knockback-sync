package me.caseload.knockbacksync.manager;

import lombok.Getter;
import lombok.Setter;
import me.caseload.knockbacksync.ConfigWrapper;
import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.Platform;
import me.caseload.knockbacksync.config.YamlConfiguration;
import me.caseload.knockbacksync.runnable.PingRunnable;
import me.caseload.knockbacksync.scheduler.AbstractTaskHandle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ConfigManager {

    public static final long CONFIG_VERSION = 4;

    private boolean toggled;
    private boolean runnableEnabled;
    private boolean updateAvailable;
    private boolean notifyUpdate;

    private long runnableInterval;
    private long combatTimer;
    private long spikeThreshold;

    private String enableMessage;
    private String disableMessage;
    private String playerEnableMessage;
    private String playerDisableMessage;
    private String playerIneligibleMessage;

    private AbstractTaskHandle pingTask;

    private Map<String, Object> config;
    private File configFile;
    private ConfigWrapper configWrapper; // Cache the ConfigWrapper instance
    private YamlConfiguration yamlConfig;

    public ConfigManager() {
        Base instance = Base.INSTANCE;
        configFile = new File(instance.getDataFolder(), "config.yml");
        yamlConfig = new YamlConfiguration(configFile);
    }

    public ConfigWrapper getConfigWrapper() {
        if (configWrapper == null) {
            reloadConfig();
        }
        return configWrapper;
    }

    public void reloadConfig() {
        try {
            if (!configFile.exists()) {
                Base.INSTANCE.saveDefaultConfig();
            } else {
                updateConfig();
            }
            yamlConfig.load();
            config = yamlConfig.getData();
            configWrapper = new ConfigWrapper(config);
        } catch (IOException e) {
            e.printStackTrace();
            config = new HashMap<>();
            configWrapper = new ConfigWrapper(config);
        }
    }

    public void saveConfig() {
        try {
            yamlConfig.setData(config);
            yamlConfig.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadConfig(boolean reloadConfig) {
        if (reloadConfig || config == null) {
            reloadConfig();
        }

        ConfigWrapper config = getConfigWrapper(); // Use cached ConfigWrapper

        toggled = config.getBoolean("enabled", true);

        // Checks to see if the runnable was enabled...
        // and if we now want to disable it
        boolean newRunnableEnabled = config.getBoolean("runnable.enabled", true);
        if (runnableEnabled && newRunnableEnabled && pingTask != null) { // null check for first startup
            pingTask.cancel();
        }

        runnableEnabled = newRunnableEnabled;
        runnableInterval = config.getLong("runnable.interval", 5L);

        if (runnableEnabled) {
            long initialDelay = 0L;
            long pingTaskRunnableInterval = runnableInterval;
            // Folia does not allow 0 ticks of wait time
            if (Base.INSTANCE.platform == Platform.FOLIA) {
                initialDelay = 1L;
                pingTaskRunnableInterval = Math.max(pingTaskRunnableInterval, 1L);
            }
            pingTask = Base.INSTANCE.getScheduler().runTaskTimerAsynchronously(new PingRunnable(), initialDelay, pingTaskRunnableInterval);
        }

        notifyUpdate = config.getBoolean("notify_updates", true);
        combatTimer = config.getLong("runnable.timer", 30L);
        spikeThreshold = config.getLong("spike_threshold", 20L);
        enableMessage = config.getString("enable_message", "&aSuccessfully enabled KnockbackSync.");
        disableMessage = config.getString("disable_message", "&cSuccessfully disabled KnockbackSync.");
        playerEnableMessage = config.getString("player_enable_message", "&aSuccessfully enabled KnockbackSync for %player%.");
        playerDisableMessage = config.getString("player_disable_message", "&cSuccessfully disabled KnockbackSync for %player%.");
        playerIneligibleMessage = config.getString("player_ineligible_message", "&c%player% is ineligible for KnockbackSync. If you believe this is in error, please contact your server administrators.");
    }

    public void updateConfig() {
        ConfigWrapper oldConfig = getConfigWrapper();
        long oldConfigVersion = oldConfig.getLong("config_version", 0);

        if (oldConfigVersion < CONFIG_VERSION) {
            // Backup old config with comments
            File backupFile = new File(configFile.getParentFile(), "config-version-" + oldConfigVersion + ".yml");
            try {
                Files.copy(configFile.toPath(), backupFile.toPath());
                Base.INSTANCE.getLogger().info("Backed up old config to " + backupFile.getName());
            } catch (IOException e) {
                Base.INSTANCE.getLogger().warning("Failed to backup old config: " + e.getMessage());
            }

            // Store old values
            Map<String, Object> oldValues = new HashMap<>(config);

            // Create new config with default values
            Base.INSTANCE.saveDefaultConfig();
            reloadConfig();

            // Transfer existing settings
            ConfigWrapper newConfig = getConfigWrapper();
            transferAllSettings(oldConfig, newConfig);

            // Set new config version
            newConfig.set("config_version", CONFIG_VERSION);

            // Save updated config
            saveConfig();

            Base.INSTANCE.getLogger().info("Config updated to version " + CONFIG_VERSION);
        }
    }

    private void transferAllSettings(ConfigWrapper oldConfig, ConfigWrapper newConfig) {
        transferSettingsRecursive(oldConfig, newConfig, "");
    }

    private void transferSettingsRecursive(ConfigWrapper oldConfig, ConfigWrapper newConfig, String currentPath) {
        for (String key : oldConfig.getKeys(currentPath)) {
            String fullPath = currentPath.isEmpty() ? key : currentPath + "." + key;
            if (newConfig.contains(fullPath)) {
                Object value = oldConfig.get(fullPath);
                if (value instanceof Map) {
                    transferSettingsRecursive(oldConfig, newConfig, fullPath);
                } else {
                    newConfig.set(fullPath, value);
                }
            }
        }
    }
}