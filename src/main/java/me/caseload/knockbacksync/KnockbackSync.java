package me.caseload.knockbacksync;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.caseload.knockbacksync.command.MainCommand;
import me.caseload.knockbacksync.listener.PacketReceiveListener;
import me.caseload.knockbacksync.listener.PlayerDamageListener;
import me.caseload.knockbacksync.listener.PlayerVelocityListener;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class KnockbackSync extends JavaPlugin {

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        CommandAPI.onEnable();
        new MainCommand().register();

        registerListeners(
                new PlayerDamageListener(),
                new PlayerVelocityListener()
        );

        PacketEvents.getAPI().getEventManager().registerListener(
                new PacketReceiveListener(), PacketListenerPriority.NORMAL);

        PacketEvents.getAPI().init();
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
}