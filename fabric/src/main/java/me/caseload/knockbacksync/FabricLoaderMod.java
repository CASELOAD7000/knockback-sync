package me.caseload.knockbacksync;

import net.fabricmc.api.EnvType;
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
        ensureServer();
        core.load();
    }

    @Override
    public void onInitialize() {
        ensureServer();
        core.enable();
        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            core.scheduler.shutdown();
            core.statsManager.getMetrics().shutdown();
        });
    }

    private void ensureServer() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            throw new IllegalStateException("This mod can only be run on servers");
        }
    }
}
