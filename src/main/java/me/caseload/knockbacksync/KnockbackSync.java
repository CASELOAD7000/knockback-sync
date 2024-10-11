package me.caseload.knockbacksync;

import com.github.retrooper.packetevents.PacketEvents;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import me.caseload.knockbacksync.command.MainCommand;
import me.caseload.knockbacksync.listener.*;
import me.caseload.knockbacksync.manager.ConfigManager;
import me.caseload.knockbacksync.stats.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.kohsuke.github.GitHub;

import java.util.logging.Logger;

public final class KnockbackSync extends JavaPlugin {

    public static Logger LOGGER;
    public static JavaPlugin INSTANCE;

    @Getter
    private final ConfigManager configManager = new ConfigManager();

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        LOGGER = getLogger();
        INSTANCE = this;
//        checkForUpdates();
        saveDefaultConfig();
        configManager.loadConfig(false);

        CommandAPI.onEnable();
        new MainCommand().register();

        registerListeners(
                new PlayerDamageListener(),
                new PlayerKnockbackListener(),
                new PlayerJoinQuitListener()
        );

        PacketEvents.getAPI().getEventManager().registerListeners(
                new AttributeChangeListener(),
                new PingReceiveListener()
        );

        PacketEvents.getAPI().getSettings()
                        .checkForUpdates(false)
                        .debug(false);
        PacketEvents.getAPI().load();
        PacketEvents.getAPI().init();

        StatsManager.init();
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
                    LOGGER.warning("A new update is available for download at: https://github.com/CASELOAD7000/knockback-sync/releases/latest");
                } else {
                    LOGGER.info("You are running the latest release.");
                }

                configManager.setUpdateAvailable(updateAvailable);
            } catch (Exception e) {
                LOGGER.severe("Failed to check for updates: " + e.getMessage());
            }
        });
    }
}