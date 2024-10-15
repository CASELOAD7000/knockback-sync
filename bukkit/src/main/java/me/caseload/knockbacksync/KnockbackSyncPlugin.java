package me.caseload.knockbacksync;

import com.github.retrooper.packetevents.PacketEvents;
import com.mojang.brigadier.CommandDispatcher;
import dev.jorel.commandapi.*;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.caseload.knockbacksync.command.KnockbackSyncCommand;
import me.caseload.knockbacksync.listener.bukkit.BukkitPlayerJoinQuitListener;
import me.caseload.knockbacksync.listener.bukkit.BukkitPlayerDamageListener;
import me.caseload.knockbacksync.listener.bukkit.BukkitPlayerKnockbackListener;
import me.caseload.knockbacksync.permission.PermissionChecker;
import me.caseload.knockbacksync.permission.PluginPermissionChecker;
import me.caseload.knockbacksync.scheduler.BukkitSchedulerAdapter;
import me.caseload.knockbacksync.scheduler.FoliaSchedulerAdapter;
import me.caseload.knockbacksync.stats.custom.BukkitStatsManager;
import me.caseload.knockbacksync.stats.custom.PluginJarHashProvider;
import me.caseload.knockbacksync.world.BukkitServer;
import net.minecraft.commands.Commands;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.util.logging.Logger;

public final class KnockbackSyncPlugin extends JavaPlugin {

    private final KnockbackSyncBase core = new KnockbackSyncBase() {

        {
            statsManager = new BukkitStatsManager();
            platformServer = new BukkitServer();
            pluginJarHashProvider = new PluginJarHashProvider() {
                @Override
                public String getPluginJarHash() throws Exception {
//                  Will give path to remapped jar on paper forks, actual jar on Spigot
                    URL jarUrl = Bukkit.getPluginManager().getPlugin("KnockbackSync").getClass().getProtectionDomain().getCodeSource().getLocation();
//                Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer("knockbacksync");
//                if (modContainer.isPresent()) {
//                    String jarPath = modContainer.get().getRootPath().getFileSystem().toString();
//                    jarPath = jarPath.replaceAll("^jar:", "").replaceAll("!/$", "");
//                    jarUrl = new File(jarPath).toURI().toURL();
//                }
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    try (InputStream is = jarUrl.openStream()) {
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = is.read(buffer)) > 0) {
                            digest.update(buffer, 0, read);
                        }
                    }
                    byte[] hash = digest.digest();
                    StringBuilder hexString = new StringBuilder();
                    for (byte b : hash) {
                        String hex = Integer.toHexString(0xff & b);
                        if (hex.length() == 1) hexString.append('0');
                        hexString.append(hex);
                    }
                    return hexString.toString();
                }
            };
        }

        private final PluginPermissionChecker permissionChecker = new PluginPermissionChecker();

        @Override
        public Logger getLogger() {
            return KnockbackSyncPlugin.this.getLogger();
        }

        @Override
        public File getDataFolder() {
            return KnockbackSyncPlugin.this.getDataFolder();
        }

        @Override
        public InputStream getResource(String filename) {
            return KnockbackSyncPlugin.this.getResource(filename);
        }

        @Override
        public void load() {
            PacketEvents.setAPI(SpigotPacketEventsBuilder.build(KnockbackSyncPlugin.this));
            PacketEvents.getAPI().load();
        }

        @Override
        public void initializeScheduler() {
            switch (platform) {
                case BUKKIT:
                    super.scheduler = new BukkitSchedulerAdapter(KnockbackSyncPlugin.this);
                    break;
                case FOLIA:
                    super.scheduler = new FoliaSchedulerAdapter(KnockbackSyncPlugin.this);
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
            CommandDispatcher dispatcher = Brigadier.getCommandDispatcher();
            dispatcher.register(KnockbackSyncCommand.build());
            dispatcher.register(
                    Commands.literal("kbsync")
                            .redirect(dispatcher.getRoot().getChild("knockbacksync"))
            );
        }

        @Override
        protected String getVersion() {
            return getDescription().getVersion();
        }

        @Override
        public void saveDefaultConfig() {
            KnockbackSyncPlugin.this.saveDefaultConfig();
        }

        @Override
        public PermissionChecker getPermissionChecker() {
            return permissionChecker;
        }
    };

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
        core.load();
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();
        core.enable();
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
        PacketEvents.getAPI().terminate();
    }

    private void registerPluginListeners(Listener... listeners) {
        PluginManager pluginManager = getServer().getPluginManager();
        for (Listener listener : listeners)
            pluginManager.registerEvents(listener, this);
    }
}