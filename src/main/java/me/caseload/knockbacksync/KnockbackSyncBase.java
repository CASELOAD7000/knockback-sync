package me.caseload.knockbacksync;

import com.github.retrooper.packetevents.PacketEvents;
import lombok.Getter;
import me.caseload.knockbacksync.manager.ConfigManager;
import me.caseload.knockbacksync.permission.PermissionChecker;
import me.caseload.knockbacksync.scheduler.SchedulerAdapter;
import me.caseload.knockbacksync.stats.StatsManager;
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

// Base class
public abstract class KnockbackSyncBase {
    public static Logger LOGGER;
    public static KnockbackSyncBase INSTANCE;

    public SchedulerAdapter scheduler = null;
    public Platform platform;

    @Getter
    protected ConfigManager configManager;

    protected KnockbackSyncBase() {
        this.platform = getPlatform();
        INSTANCE = this;
        configManager = new ConfigManager();
    }

    private Platform getPlatform() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return Platform.FOLIA; // Paper (Folia) detected
        } catch (ClassNotFoundException ignored1) {}

        try {
            Class.forName("org.bukkit.Bukkit");
            return Platform.BUKKIT; // Bukkit (Spigot/Paper without Folia) detected
        } catch (ClassNotFoundException ignored2) {}

        try {
            Class.forName("net.fabricmc.loader.api.FabricLoader");
            return Platform.FABRIC; // Fabric detected
        } catch (ClassNotFoundException ignored3) {}

        throw new IllegalStateException("Unknown platform!");
    }

    public abstract Logger getLogger();
    public abstract File getDataFolder();
    public abstract InputStream getResource(String filename);

    public abstract void load();

    public void enable() {
        saveDefaultConfig();
        LOGGER = getLogger();
        initializeScheduler();
        initializePacketEvents();
        configManager.loadConfig(false);
        registerPlatformListeners();
        registerCommonListeners();
        registerCommands();
        StatsManager.init();
        checkForUpdates();
    }

    public abstract void initializeScheduler();

    public void initializePacketEvents() {
        PacketEvents.getAPI().getSettings()
                .checkForUpdates(false)
                .debug(false);

        PacketEvents.getAPI().init();
    }

    protected void registerCommonListeners() {
//        PacketEvents.getAPI().getEventManager().registerListeners(
//                new AttributeChangeListener(),
//                new PingReceiveListener()
//        );
    }

    protected abstract void registerPlatformListeners();
    protected abstract void registerCommands();
    protected abstract String getVersion();

    protected void checkForUpdates() {
        getLogger().info("Checking for updates...");

        scheduler.runTaskAsynchronously(() -> {
            try {
                GitHub github = GitHub.connectAnonymously();
                String latestVersion = github.getRepository("CASELOAD7000/knockback-sync")
                        .getLatestRelease()
                        .getTagName();

                String currentVersion = getVersion();
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

    public abstract void saveDefaultConfig();

    public abstract PermissionChecker getPermissionChecker();

    public SchedulerAdapter getScheduler() {
        return scheduler;
    }
}


