package me.caseload.knockbacksync;

import com.github.retrooper.packetevents.PacketEvents;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import lombok.Setter;
import me.caseload.knockbacksync.command.MainCommand;
import me.caseload.knockbacksync.config.KnockbackSyncConfigReloadEvent;
import me.caseload.knockbacksync.listener.*;
import me.caseload.knockbacksync.runnable.PingRunnable;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.kohsuke.github.GitHub;

@Setter
@Getter
public final class KnockbackSync extends JavaPlugin implements Listener {

    private boolean toggled;
    private boolean runnableEnabled;
    private boolean updateAvailable;
    private boolean notifyUpdate;

    private long runnableInterval;
    private long combatTimer;
    private long spikeThreshold;

    private BukkitTask pingTask;

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        checkForUpdates();
        saveDefaultConfig();

        CommandAPI.onEnable();
        new MainCommand().register();

        registerListeners(
                new PlayerDamageListener(),
                new PlayerKnockbackListener(),
                new PlayerJoinQuitListener(),
                this
        );

        PacketEvents.getAPI().getEventManager().registerListeners(
                new AttributeChangeListener(),
                new PingReceiveListener()
        );

        PacketEvents.getAPI().init();

        loadConfig();
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
        PacketEvents.getAPI().terminate();
    }

    @EventHandler
    public void onConfigReload(KnockbackSyncConfigReloadEvent event) {
        loadConfig();
    }

    private void loadConfig() {
        toggled = getConfig().getBoolean("enabled", true);
        // Checks to see if the runnable was enabled...
        // and if we now want to disable it
        boolean newRunnableEnabled = getConfig().getBoolean("runnable.enabled", true);
        if (runnableEnabled && newRunnableEnabled && pingTask != null) { // null check for first startup
            pingTask.cancel();
        }
        runnableEnabled = newRunnableEnabled;
        runnableInterval = getConfig().getLong("runnable.interval", 5L);
        combatTimer = getConfig().getLong("runnable.timer", 30L);
        spikeThreshold = getConfig().getLong("spike_threshold", 20L);
        notifyUpdate = getConfig().getBoolean("notify_updates", true);

        if (runnableEnabled)
            pingTask = new PingRunnable().runTaskTimerAsynchronously(this, 0L, runnableInterval);
    }

    public static KnockbackSync getInstance() {
        return getPlugin(KnockbackSync.class);
    }

    private void registerListeners(Listener... listeners) {
        PluginManager pluginManager = getServer().getPluginManager();
        for (Listener listener : listeners)
            pluginManager.registerEvents(listener, this);
    }

    private void checkForUpdates() {
        getLogger().info("Checking for updates...");

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try {
                GitHub github = GitHub.connectAnonymously();
                String latestVersion = github.getRepository("CASELOAD7000/knockback-sync")
                        .getLatestRelease()
                        .getTagName();

                String currentVersion = getDescription().getVersion();
                boolean updateAvailable = !currentVersion.equalsIgnoreCase(latestVersion);

                if (updateAvailable) {
                    getLogger().warning("A new update is available for download at: https://github.com/CASELOAD7000/knockback-sync/releases/latest");
                } else {
                    getLogger().info("You are running the latest release.");
                }

                setUpdateAvailable(updateAvailable);
            } catch (Exception e) {
                getLogger().severe("Failed to check for updates: " + e.getMessage());
            }
        });
    }
}