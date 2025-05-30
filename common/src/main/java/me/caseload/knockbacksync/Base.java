package me.caseload.knockbacksync;

import com.github.retrooper.packetevents.PacketEvents;
import lombok.Getter;
import me.caseload.knockbacksync.command.MainCommand;
import me.caseload.knockbacksync.command.generic.AbstractPlayerSelectorParser;
import me.caseload.knockbacksync.command.generic.BuilderCommand;
import me.caseload.knockbacksync.command.subcommand.PingCommand;
import me.caseload.knockbacksync.command.subcommand.ReloadCommand;
import me.caseload.knockbacksync.command.subcommand.StatusCommand;
import me.caseload.knockbacksync.command.subcommand.ToggleCommand;
import me.caseload.knockbacksync.command.subcommand.ToggleOffGroundSubcommand;
import me.caseload.knockbacksync.event.Event;
import me.caseload.knockbacksync.event.EventBus;
import me.caseload.knockbacksync.event.OptimizedEventBus;
import me.caseload.knockbacksync.listener.packetevents.*;
import me.caseload.knockbacksync.manager.ConfigManager;
import me.caseload.knockbacksync.permission.PermissionChecker;
import me.caseload.knockbacksync.scheduler.SchedulerAdapter;
import me.caseload.knockbacksync.sender.Sender;
import me.caseload.knockbacksync.stats.custom.ClientBrandsPie;
import me.caseload.knockbacksync.stats.custom.PluginJarHashProvider;
import me.caseload.knockbacksync.stats.custom.StatsManager;
import me.caseload.knockbacksync.world.PlatformServer;
import org.incendo.cloud.CommandManager;
import org.kohsuke.github.GitHub;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

// Base class
public abstract class Base {
    public static Logger LOGGER;
    public static Base INSTANCE;

    @Getter private final Platform platform;
    @Getter protected StatsManager statsManager;
    @Getter protected PlatformServer platformServer;
    @Getter protected PluginJarHashProvider pluginJarHashProvider;
    @Getter protected SchedulerAdapter scheduler;
    @Getter protected ConfigManager configManager;
    @Getter protected CommandManager<Sender> commandManager;
    @Getter protected final EventBus eventBus = new OptimizedEventBus();

    @Getter
    protected AbstractPlayerSelectorParser<Sender> playerSelectorParser;

    protected Base() {
        this.platform = detectPlatform();
        INSTANCE = this;
    }

    private Platform detectPlatform() {
        final Map<String, Platform> platforms = Collections.unmodifiableMap(new HashMap<String, Platform>() {{
            put("io.papermc.paper.threadedregions.RegionizedServer", Platform.FOLIA);
            put("org.bukkit.Bukkit", Platform.BUKKIT);
            put("net.fabricmc.loader.api.FabricLoader", Platform.FABRIC);
        }});

        return platforms.entrySet().stream()
                .filter(entry -> isClassPresent(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unknown platform!"));
    }

    private boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public abstract Logger getLogger();

    public abstract File getDataFolder();

    public abstract InputStream getResource(String filename);

    public abstract void load();

    public void enable() {
        LOGGER = getLogger();
        saveDefaultConfig();
        initializePacketEvents();
        registerCommonListeners();
        registerPlatformListeners();
        registerCommands();
        initializeScheduler();
        configManager.loadConfig(false);
        statsManager.init();
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
        PacketEvents.getAPI().getEventManager().registerListeners(
                new AttributeChangeListener(),
                new PingSendListener(),
                new PingReceiveListener(),
                new PacketPlayerJoinQuit(),
                new ClientBrandListener()
        );
        Event.setEventBus(eventBus);
    }

    protected abstract void registerPlatformListeners();

    protected void registerCommands() {
        List<BuilderCommand> list = Arrays.asList(
                new MainCommand(),
                new ReloadCommand(),
                new PingCommand(),
                new StatusCommand(),
                new ToggleOffGroundSubcommand(),
                new ToggleCommand()
        );
        list.forEach(command -> command.register(commandManager));
    }

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

                int comparisonResult = compareVersions(currentVersion, latestVersion);

                if (comparisonResult < 0) {
                    LOGGER.warning("You are running an older version. A new update is available for download at: https://github.com/CASELOAD7000/knockback-sync/releases/latest");
                    configManager.setUpdateAvailable(true);
                } else if (comparisonResult > 0) {
                    if (currentVersion.contains("-dev")) 
                        LOGGER.info("You are running a development build newer than the latest release.");
                    else {
                        LOGGER.info("You are running a version newer than the latest release.");
                    }
                } else {
                    LOGGER.info("You are running the latest release.");
                }

                if (configManager.isUpdateAvailable() && configManager.isAutoUpdate()) {
                    LOGGER.info("Updating...");
                    byte[] bytes = downloadLatestRelease(github);
                    updatePlugin(bytes);

                    if (hasRestartScript())
                        scheduler.runTask(this::restartServer);
                    else {
                        // Yeah
                    }
                }
            } catch (Exception e) {
                LOGGER.severe("Failed to check for updates: " + e.getMessage());
            }
        });
    }

    private void updatePlugin(byte[] pluginBytes) throws URISyntaxException {
        File file = new File(getJarURL().toURI());

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(pluginBytes);
            LOGGER.info("Successfully updated the plugin!");
        } catch (Exception e) {
            LOGGER.severe("Failed to update: " + e.getMessage());
        }
    }

    private byte[] downloadLatestRelease(GitHub github) {
        try {
            return github.getRepository("CASELOAD7000/knockback-sync")
                    .getLatestRelease()
                    .getAssets().stream()
                    .filter(asset -> asset.getName().endsWith(".jar")
                            && asset.getName().contains(platform.equals(Platform.FABRIC)
                            ? "fabric" : "bukkit"))
                    .findFirst()
                    .map(asset -> {
                        try (InputStream inputStream = new URL(asset.getBrowserDownloadUrl()).openStream()) {
                            inputStream.reset();
                            byte[] bytes = new byte[inputStream.available()];
                            DataInputStream dataInputStream = new DataInputStream(inputStream);
                            dataInputStream.readFully(bytes);
                            return bytes;
                        } catch (Exception e) {
                            LOGGER.severe("Failed to download latest release: " + e.getMessage());
                            return null;
                        }
                    }).orElse(null);
        } catch (Exception e) {
            LOGGER.severe("Failed to locate latest release: " + e.getMessage());
            return null;
        }
    }

    private int compareVersions(String version1, String version2) {
        String[] v1Parts = version1.split("[-.]");
        String[] v2Parts = version2.split("[-.]");

        int length = Math.min(v1Parts.length, v2Parts.length);

        for (int i = 0; i < length; i++) {
            int comparison = compareVersionPart(v1Parts[i], v2Parts[i]);
            if (comparison != 0) {
                return comparison;
            }
        }

        // If we're here, all compared parts are equal
        if (v1Parts.length != v2Parts.length) {
            return compareSpecialVersions(v1Parts, v2Parts);
        }

        return 0; // Versions are equal
    }

    private int compareVersionPart(String part1, String part2) {
        try {
            int v1 = Integer.parseInt(part1);
            int v2 = Integer.parseInt(part2);
            return Integer.compare(v1, v2);
        } catch (NumberFormatException e) {
            // If parts are not numbers, compare them based on dev < snapshot < release
            return compareSpecialPart(part1, part2);
        }
    }

    private int compareSpecialPart(String part1, String part2) {
        if (part1.equals(part2)) return 0;
        if (part1.startsWith("dev")) return part2.startsWith("dev") ? 0 : -1;
        if (part2.startsWith("dev")) return 1;
        if (part1.equals("SNAPSHOT")) return part2.equals("SNAPSHOT") ? 0 : -1;
        if (part2.equals("SNAPSHOT")) return 1;
        return part1.compareTo(part2);
    }

    private int compareSpecialVersions(String[] v1Parts, String[] v2Parts) {
        if (v1Parts.length > v2Parts.length) {
            String specialPart = v1Parts[v2Parts.length];
            if (specialPart.startsWith("dev")) return -1;
            if (specialPart.equals("SNAPSHOT")) return -1;
            return 1; // Assume it's a release version part
        } else {
            String specialPart = v2Parts[v1Parts.length];
            if (specialPart.startsWith("dev")) return 1;
            if (specialPart.equals("SNAPSHOT")) return 1;
            return -1; // Assume it's a release version part
        }
    }

    public abstract void saveDefaultConfig();

    public abstract PermissionChecker getPermissionChecker();

    public abstract float getTickRate();

    public abstract URL getJarURL();

    public abstract void restartServer();

    public abstract boolean hasRestartScript();
}


