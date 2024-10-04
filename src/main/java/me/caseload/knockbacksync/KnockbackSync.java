package me.caseload.knockbacksync;

import com.github.retrooper.packetevents.PacketEvents;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import lombok.Setter;
import me.caseload.knockbacksync.command.MainCommand;
import me.caseload.knockbacksync.listener.*;
import me.caseload.knockbacksync.runnable.PingRunnable;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.kohsuke.github.GitHub;

@Setter
@Getter
public final class KnockbackSync extends JavaPlugin {

    private boolean toggled;
    private boolean runnable;
    private boolean updateAvailable;
    private boolean notifyUpdate;

    private long runnableInterval;
    private long combatTimer;
    private long spikeThreshold;

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
                new PlayerJoinQuitListener()
        );

        PacketEvents.getAPI().getEventManager().registerListener(
                new PingReceiveListener()
        );

        PacketEvents.getAPI().init();

        toggled = getConfig().getBoolean("enabled", true);
        runnable = getConfig().getBoolean("runnable.enabled", true);
        runnableInterval = getConfig().getLong("runnable.interval", 5L);
        combatTimer = getConfig().getLong("runnable.timer", 30L);
        spikeThreshold = getConfig().getLong("spike_threshold", 20L);
        notifyUpdate = getConfig().getBoolean("notify_updates", true);

        if (runnable)
            new PingRunnable().runTaskTimerAsynchronously(this, 0L, runnableInterval);
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
        PacketEvents.getAPI().terminate();
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