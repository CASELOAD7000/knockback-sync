package me.caseload.knockbacksync;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.caseload.knockbacksync.event.ConfigReloadEvent;
import me.caseload.knockbacksync.event.KBSyncEventHandler;
import me.caseload.knockbacksync.listener.bukkit.*;
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
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Logger;

public class BukkitBase extends Base {

    private final JavaPlugin plugin;
    private final BukkitSenderFactory bukkitSenderFactory = new BukkitSenderFactory(this);
    private final PluginPermissionChecker permissionChecker = new PluginPermissionChecker();

    private int playerUpdateInterval;

    public BukkitBase(JavaPlugin plugin) {
        this.plugin = plugin;
        super.configManager = new ConfigManager();
        super.playerSelectorParser = new BukkitPlayerSelectorParser<>();
        super.statsManager = new BukkitStatsManager();
        super.platformServer = new BukkitServer();
        super.pluginJarHashProvider = new PluginJarHashProvider(this.getClass().getProtectionDomain().getCodeSource().getLocation());
        this.playerUpdateInterval = this.getConfigManager().getConfigWrapper().getInt("entity_tick_intervals.player", 2);
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
        super.simpleEventBus.registerListeners(this);
        if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_20_5)) {
            scheduler.runTaskTimerAsynchronously(this::setUpdateIntervals, 1, 1);
        }
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

    public void setUpdateIntervals() {
        try {
            for (World world : Bukkit.getWorlds()) {
                Method getWorldHandleMethod = world.getClass().getMethod("getHandle");
                Object serverLevel = getWorldHandleMethod.invoke(world);

                // Get ChunkMap
                Method getChunkSource = serverLevel.getClass().getMethod("getChunkSource");
                Object chunkSource = getChunkSource.invoke(serverLevel);
                Field chunkMapField = chunkSource.getClass().getDeclaredField("chunkMap");
                chunkMapField.setAccessible(true);
                Object chunkMap = chunkMapField.get(chunkSource);

                // Get entityMap from ChunkMap
                Field entityMapField = chunkMap.getClass().getDeclaredField("entityMap");
                entityMapField.setAccessible(true);
                Map<Integer, ?> entityMap = (Map<Integer, ?>) entityMapField.get(chunkMap);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Object trackedEntity = entityMap.get(player.getEntityId());
                    if (trackedEntity == null) // Players are removed from the entityMap when they die (and don't respawn)
                        continue;

                    Field serverEntityField = trackedEntity.getClass().getDeclaredField("serverEntity");
                    serverEntityField.setAccessible(true);
                    Object serverEntity = serverEntityField.get(trackedEntity);

                    Field updateIntervalField = serverEntity.getClass().getDeclaredField("updateInterval");
                    updateIntervalField.setAccessible(true);
                    updateIntervalField.set(serverEntity, playerUpdateInterval);
                }
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalStateException("Unable to use reflection to modify updateIntervals" + e);
        }
    }

    @KBSyncEventHandler
    public void onConfigReload(ConfigReloadEvent event) {
        playerUpdateInterval = event.getConfigManager().getConfigWrapper().getInt("entity_tick_intervals.player", 2);
    }
}