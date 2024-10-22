package me.caseload.knockbacksync;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.caseload.knockbacksync.listener.bukkit.BukkitPlayerDamageListener;
import me.caseload.knockbacksync.listener.bukkit.BukkitPlayerJoinQuitListener;
import me.caseload.knockbacksync.listener.bukkit.BukkitPlayerJumpListener;
import me.caseload.knockbacksync.listener.bukkit.BukkitPlayerKnockbackListener;
import me.caseload.knockbacksync.manager.ConfigManager;
import me.caseload.knockbacksync.permission.PermissionChecker;
import me.caseload.knockbacksync.permission.PluginPermissionChecker;
import me.caseload.knockbacksync.scheduler.BukkitSchedulerAdapter;
import me.caseload.knockbacksync.scheduler.FoliaSchedulerAdapter;
import me.caseload.knockbacksync.sender.BukkitPlayerSelectorParser;
import me.caseload.knockbacksync.sender.BukkitSenderFactory;
import me.caseload.knockbacksync.sender.Sender;
import me.caseload.knockbacksync.stats.custom.BukkitStatsManager;
import me.caseload.knockbacksync.stats.custom.PluginJarHashProvider;
import me.caseload.knockbacksync.world.BukkitServer;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

public class KBSyncBukkitBase extends KnockbackSyncBase {

    private final JavaPlugin plugin;
    private final BukkitSenderFactory bukkitSenderFactory = new BukkitSenderFactory(this);
    private final PluginPermissionChecker permissionChecker = new PluginPermissionChecker();

    public KBSyncBukkitBase(JavaPlugin plugin) {
        this.plugin = plugin;
        super.configManager = new ConfigManager();
        super.playerSelectorParser = new BukkitPlayerSelectorParser<>();
        super.statsManager = new BukkitStatsManager();
        super.platformServer = new BukkitServer();
        super.pluginJarHashProvider = new PluginJarHashProvider(this.getClass().getProtectionDomain().getCodeSource().getLocation());
    }

    @Override
    public Logger getLogger() {
        return this.plugin.getLogger();
    }

    @Override
    public File getDataFolder() {
        return this.plugin.getDataFolder();
    }

    @Override
    public InputStream getResource(String filename) {
        return this.plugin.getResource(filename);
    }

    @Override
    public void load() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this.plugin));
        PacketEvents.getAPI().load();
    }

    @Override
    public void enable() {
        super.enable();
        initializeScheduler();
        configManager.loadConfig(false);
        statsManager.init();
        checkForUpdates();
    }

    @Override
    public void initializeScheduler() {
        switch (platform) {
            case BUKKIT:
                super.scheduler = new BukkitSchedulerAdapter(this.plugin);
                break;
            case FOLIA:
                super.scheduler = new FoliaSchedulerAdapter(this.plugin);
                break;
        }
    }

    @Override
    protected void registerPlatformListeners() {
        registerPluginListeners(
                new BukkitPlayerDamageListener(),
                new BukkitPlayerKnockbackListener(),
                new BukkitPlayerJoinQuitListener(),
                new BukkitPlayerJumpListener()
        );
    }

    @Override
    protected void registerCommands() {
        super.commandManager = new LegacyPaperCommandManager<>(
                this.plugin,
                ExecutionCoordinator.simpleCoordinator(),
                bukkitSenderFactory
        );
        if (commandManager instanceof LegacyPaperCommandManager) {
            LegacyPaperCommandManager<Sender> legacyPaperCommandManager = (LegacyPaperCommandManager<Sender>) commandManager;
            if (legacyPaperCommandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
                legacyPaperCommandManager.registerBrigadier();
            } else if (commandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
                legacyPaperCommandManager.registerAsynchronousCompletions();
            }
        }
        super.registerCommands();
    }

    @Override
    protected String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    @Override
    public void saveDefaultConfig() {
        this.plugin.saveDefaultConfig();
    }

    @Override
    public PermissionChecker getPermissionChecker() {
        return permissionChecker;
    }

    private void registerPluginListeners(Listener... listeners) {
        PluginManager pluginManager = this.plugin.getServer().getPluginManager();
        for (Listener listener : listeners)
            pluginManager.registerEvents(listener, this.plugin);
    }

    public BukkitSenderFactory getSenderFactory() {
        return this.bukkitSenderFactory;
    }
}