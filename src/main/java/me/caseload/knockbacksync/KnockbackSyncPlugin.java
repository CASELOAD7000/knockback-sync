package me.caseload.knockbacksync;

import com.github.retrooper.packetevents.PacketEvents;
import dev.jorel.commandapi.*;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.caseload.knockbacksync.command.KnockbackSyncCommand;
import me.caseload.knockbacksync.listener.*;
import me.caseload.knockbacksync.permission.PermissionChecker;
import me.caseload.knockbacksync.permission.PluginPermissionChecker;
import me.caseload.knockbacksync.scheduler.BukkitSchedulerAdapter;
import me.caseload.knockbacksync.scheduler.FoliaSchedulerAdapter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

public final class KnockbackSyncPlugin extends JavaPlugin {

    private KnockbackSyncBase core = new KnockbackSyncBase() {

        private PluginPermissionChecker permissionChecker = new PluginPermissionChecker();

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
        protected void initializeScheduler() {
            switch (platform) {
                case BUKKIT:
                    scheduler = new BukkitSchedulerAdapter(KnockbackSyncPlugin.this);
                case FOLIA:
                    scheduler = new FoliaSchedulerAdapter(KnockbackSyncPlugin.this);
            }
        }

        @Override
        protected void registerPlatformListeners() {
            registerPluginListeners(
                    new PlayerDamageListener(),
                    new PlayerKnockbackListener(),
                    new PlayerJoinQuitListener()
            );
        }

        @Override
        protected void registerCommands() {
            Brigadier.getCommandDispatcher().register(KnockbackSyncCommand.build());
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
        core.load();
    }

    @Override
    public void onEnable() {
        core.enable();
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }

    private void registerPluginListeners(Listener... listeners) {
        PluginManager pluginManager = getServer().getPluginManager();
        for (Listener listener : listeners)
            pluginManager.registerEvents(listener, this);
    }
}