package me.caseload.knockbacksync;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.caseload.knockbacksync.command.PlayerSelector;
import me.caseload.knockbacksync.command.bukkit.MainCommand;
import me.caseload.knockbacksync.listener.bukkit.BukkitPlayerDamageListener;
import me.caseload.knockbacksync.listener.bukkit.BukkitPlayerJoinQuitListener;
import me.caseload.knockbacksync.listener.bukkit.BukkitPlayerKnockbackListener;
import me.caseload.knockbacksync.permission.PermissionChecker;
import me.caseload.knockbacksync.permission.PluginPermissionChecker;
import me.caseload.knockbacksync.scheduler.BukkitSchedulerAdapter;
import me.caseload.knockbacksync.scheduler.FoliaSchedulerAdapter;
import me.caseload.knockbacksync.sender.BukkitPlayerSelectorAdapter;
import me.caseload.knockbacksync.sender.BukkitSenderFactory;
import me.caseload.knockbacksync.sender.Sender;
import me.caseload.knockbacksync.stats.custom.BukkitStatsManager;
import me.caseload.knockbacksync.stats.custom.PluginJarHashProvider;
import me.caseload.knockbacksync.world.BukkitServer;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.bukkit.parser.selector.SinglePlayerSelectorParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

public class KBSyncBukkitBase extends KnockbackSyncBase {

    private final JavaPlugin plugin;
    private final BukkitSenderFactory bukkitSenderFactory;

    public KBSyncBukkitBase(JavaPlugin plugin) {
        this.plugin = plugin;
        this.bukkitSenderFactory = new BukkitSenderFactory(this);
    }

    private final PluginPermissionChecker permissionChecker = new PluginPermissionChecker();

    {
        statsManager = new BukkitStatsManager();
        platformServer = new BukkitServer();
        pluginJarHashProvider = new PluginJarHashProvider(this.getClass().getProtectionDomain().getCodeSource().getLocation());
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
                new BukkitPlayerJoinQuitListener()
        );
    }

    @Override
    protected void registerCommands() {
        new MainCommand(this).register();
        LegacyPaperCommandManager<Sender> legacyPaperCommandManager = new LegacyPaperCommandManager<>(
                this.plugin,
                ExecutionCoordinator.simpleCoordinator(),
                bukkitSenderFactory
        );

        if (legacyPaperCommandManager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            legacyPaperCommandManager.registerBrigadier();
        } else if (legacyPaperCommandManager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            legacyPaperCommandManager.registerAsynchronousCompletions();
        }

        legacyPaperCommandManager.command(
                legacyPaperCommandManager.commandBuilder("knockbacksync", "kbsync", "kbs")
                        .literal("ping")
                        .optional("target", SinglePlayerSelectorParser.singlePlayerSelectorParser())
                        .handler(commandContext -> {
                            PlayerSelector selector = new BukkitPlayerSelectorAdapter(commandContext.get("target"));
                            commandContext.sender().sendMessage("Hello World");
                        })
        );



//            CommandDispatcher dispatcher = Brigadier.getCommandDispatcher();
//            dispatcher.register(KnockbackSyncCommand.build());
//            dispatcher.register(
//                    Commands.literal("kbsync")
//                            .redirect(dispatcher.getRoot().getChild("knockbacksync"))
//            );
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