package me.caseload.knockbacksync;

import com.github.retrooper.packetevents.PacketEvents;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.plugin.java.JavaPlugin;

public final class KBSyncBukkitLoaderPlugin extends JavaPlugin {

    private final KnockbackSyncBase core = new KBSyncBukkitBase(this);

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
}