package me.caseload.knockbacksync;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.server.MinecraftServer;

public class FabricLoaderMod implements PreLaunchEntrypoint, ModInitializer {

    private final Base core = new FabricBase();

    public static MinecraftServer getServer() {
        return (MinecraftServer) FabricLoader.getInstance().getGameInstance();
    }

    @Override
    public void onPreLaunch() {
        core.load();
    }

    @Override
    public void onInitialize() {
        core.enable();
        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            core.scheduler.shutdown();
            core.statsManager.getMetrics().shutdown();
        });
    }
}
