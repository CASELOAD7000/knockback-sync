package me.caseload.knockbacksync.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import lombok.Setter;
import me.caseload.knockbacksync.ConfigWrapper;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.Platform;
import me.caseload.knockbacksync.player.PlayerData;
import me.caseload.knockbacksync.runnable.PingRunnable;
import me.caseload.knockbacksync.scheduler.AbstractTaskHandle;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ConfigManager {

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
    private String reloadMessage;

    private AbstractTaskHandle pingTask;

    private Map<String, Object> config;
    private File configFile;
    private ObjectMapper mapper;
    private ConfigWrapper configWrapper; // Cache the ConfigWrapper instance

    public ConfigManager() {
        mapper = new ObjectMapper(new YAMLFactory());
        KnockbackSyncBase instance = KnockbackSyncBase.INSTANCE;
        configFile = new File(instance.getDataFolder(), "config.yml");
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
                KnockbackSyncBase.INSTANCE.saveDefaultConfig();
            }
            config = mapper.readValue(configFile, Map.class);
            configWrapper = new ConfigWrapper(config);
        } catch (IOException e) {
            e.printStackTrace();
            config = new HashMap<>();
            configWrapper = new ConfigWrapper(config);
        }
    }

    public void saveConfig() {
        try {
            mapper.writeValue(configFile, config);
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

        if (runnableEnabled) {
            long initialDelay = 0L;
            long pingTaskRunnableInterval = runnableInterval;
            // Folia does not allow 0 ticks of wait time
            if (KnockbackSyncBase.INSTANCE.platform == Platform.FOLIA) {
                initialDelay = 1L;
                pingTaskRunnableInterval = Math.max(pingTaskRunnableInterval, 1L);
            }
            pingTask = KnockbackSyncBase.INSTANCE.getScheduler().runTaskTimerAsynchronously(new PingRunnable(), initialDelay, pingTaskRunnableInterval);
        }

        notifyUpdate = config.getBoolean("notify_updates", true);
        runnableInterval = config.getLong("runnable.interval", 5L);
        combatTimer = config.getLong("runnable.timer", 30L);
        spikeThreshold = config.getLong("spike_threshold", 20L);
        enableMessage = config.getString("enable_message", "&aSuccessfully enabled KnockbackSync.");
        disableMessage = config.getString("disable_message", "&cSuccessfully disabled KnockbackSync.");
        playerEnableMessage = config.getString("player_enable_message", "&aSuccessfully enabled KnockbackSync for %player%.");
        playerDisableMessage = config.getString("player_disable_message", "&cSuccessfully disabled KnockbackSync for %player%.");
        playerIneligibleMessage = config.getString("player_ineligible_message", "&c%player% is ineligible for KnockbackSync. If you believe this is an error, please open an issue on the github page.");
        reloadMessage = config.getString("reload_message", "&aSuccessfully reloaded KnockbackSync.");
        PlayerData.PING_OFFSET = config.getInt("ping_offset", 25);
    }
}