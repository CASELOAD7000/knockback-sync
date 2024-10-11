package me.caseload.knockbacksync.manager;

import lombok.Getter;
import lombok.Setter;
import me.caseload.knockbacksync.KnockbackSync;
import me.caseload.knockbacksync.runnable.PingRunnable;
import me.caseload.knockbacksync.scheduler.AbstractTaskHandle;
import org.bukkit.scheduler.BukkitTask;

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

        if (reloadConfig)
            instance.reloadConfig();

        toggled = instance.getConfig().getBoolean("enabled", true);

        // Checks to see if the runnable was enabled...
        // and if we now want to disable it
        boolean newRunnableEnabled = instance.getConfig().getBoolean("runnable.enabled", true);
        if (runnableEnabled && newRunnableEnabled && pingTask != null) // null check for first startup
            pingTask.cancel();

        runnableEnabled = newRunnableEnabled;

        if (runnableEnabled)
            pingTask = KnockbackSync.INSTANCE.getScheduler().runTaskTimerAsynchronously(new PingRunnable(), 0L, runnableInterval);

        notifyUpdate = instance.getConfig().getBoolean("notify_updates", true);
        runnableInterval = instance.getConfig().getLong("runnable.interval", 5L);
        combatTimer = instance.getConfig().getLong("runnable.timer", 30L);
        spikeThreshold = instance.getConfig().getLong("spike_threshold", 20L);
        enableMessage = instance.getConfig().getString("enable_message", "&aSuccessfully enabled KnockbackSync.");
        disableMessage = instance.getConfig().getString("disable_message", "&cSuccessfully disabled KnockbackSync.");
        playerEnableMessage = instance.getConfig().getString("player_enable_message", "&aSuccessfully enabled KnockbackSync for %player%.");
        playerDisableMessage = instance.getConfig().getString("player_disable_message", "&cSuccessfully disabled KnockbackSync for %player%.");
        playerIneligibleMessage = instance.getConfig().getString("player_ineligible_message", "&c%player% is ineligible for KnockbackSync. If you believe this is an error, please open an issue on the github page.");
        reloadMessage = instance.getConfig().getString("reload_message", "&aSuccessfully reloaded KnockbackSync.");
        PlayerData.PING_OFFSET = KnockbackSync.getInstance().getConfig().getInt("ping_offset", 25);
    }
}
