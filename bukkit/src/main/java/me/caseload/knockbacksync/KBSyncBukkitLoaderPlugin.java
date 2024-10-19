package me.caseload.knockbacksync;

import com.github.retrooper.packetevents.PacketEvents;
import org.bukkit.plugin.java.JavaPlugin;

public final class KBSyncBukkitLoaderPlugin extends JavaPlugin {

    private final KnockbackSyncBase core = new KBSyncBukkitBase();

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