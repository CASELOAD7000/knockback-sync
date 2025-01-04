package me.caseload.knockbacksync;

import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.plugin.java.JavaPlugin;

public final class BukkitLoaderPlugin extends JavaPlugin {

    private final Base core = new BukkitBase(this);

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
}