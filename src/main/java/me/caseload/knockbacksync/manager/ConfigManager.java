package me.caseload.knockbacksync.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import lombok.Setter;
import me.caseload.knockbacksync.KnockbackSync;
import me.caseload.knockbacksync.Platform;
import me.caseload.knockbacksync.runnable.PingRunnable;
import me.caseload.knockbacksync.scheduler.AbstractTaskHandle;

import java.io.File;
import java.io.IOException;
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

    public void loadConfig(boolean reloadConfig) {
        KnockbackSync instance = KnockbackSync.getInstance();
        File configFile = new File(instance.getDataFolder(), "config.yml");

        if (reloadConfig || !configFile.exists()) {
            instance.saveDefaultConfig();
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Map<String, Object> config;
        try {
            config = mapper.readValue(configFile, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        toggled = (boolean) config.getOrDefault("enabled", true);

        // Checks to see if the runnable was enabled...
        // and if we now want to disable it
        boolean newRunnableEnabled = (boolean) config.getOrDefault("runnable.enabled", true);
        if (runnableEnabled && newRunnableEnabled && pingTask != null) // null check for first startup
            pingTask.cancel();

        runnableEnabled = newRunnableEnabled;

        if (runnableEnabled) {
            long initialDelay = 0L;
            long pingTaskRunnableInterval = runnableInterval;
            // Folia does not allow 0 ticks of wait time
            if (KnockbackSync.INSTANCE.platform == Platform.FOLIA) {
                initialDelay = 1L;
                pingTaskRunnableInterval = Math.max(pingTaskRunnableInterval, 1L);
            }
            pingTask = KnockbackSync.INSTANCE.getScheduler().runTaskTimerAsynchronously(new PingRunnable(), initialDelay, pingTaskRunnableInterval);
        }

        notifyUpdate = (boolean) config.getOrDefault("notify_updates", true);
        runnableInterval = (long) config.getOrDefault("runnable.interval", 5L);
        combatTimer = (long) config.getOrDefault("runnable.timer", 30L);
        spikeThreshold = (long) config.getOrDefault("spike_threshold", 20L);
        enableMessage = (String) config.getOrDefault("enable_message", "&aSuccessfully enabled KnockbackSync.");
        disableMessage = (String) config.getOrDefault("disable_message", "&cSuccessfully disabled KnockbackSync.");
        playerEnableMessage = (String) config.getOrDefault("player_enable_message", "&aSuccessfully enabled KnockbackSync for %player%.");
        playerDisableMessage = (String) config.getOrDefault("player_disable_message", "&cSuccessfully disabled KnockbackSync for %player%.");
        playerIneligibleMessage = (String) config.getOrDefault("player_ineligible_message", "&c%player% is ineligible for KnockbackSync. If you believe this is an error, please open an issue on the github page.");
        reloadMessage = (String) config.getOrDefault("reload_message", "&aSuccessfully reloaded KnockbackSync.");
        PlayerData.PING_OFFSET = (int) config.getOrDefault("ping_offset", 25);
    }
}