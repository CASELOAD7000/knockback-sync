package me.caseload.knockbacksync;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.caseload.knockbacksync.command.FabricCommandSource;
import me.caseload.knockbacksync.command.FabricSenderFactory;
import me.caseload.knockbacksync.command.SenderFactory;
import me.caseload.knockbacksync.command.subcommand.ReloadSubCommand;
import me.caseload.knockbacksync.world.KBSyncFabricBase;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

public class KBSyncFabricLoaderMod implements PreLaunchEntrypoint, ModInitializer {

//  private static MinecraftServer SERVER;

    private final KnockbackSyncBase core = new KBSyncFabricBase();

    public static MinecraftServer getServer() {
        return (MinecraftServer) FabricLoader.getInstance().getGameInstance();
    }

    @Override
    public void onPreLaunch() {
        core.load();
//    ServerLifecycleEvents.SERVER_STARTING.register((minecraftServer -> {
//      SERVER = minecraftServer;
//      core.initializeScheduler();
//      core.configManager.loadConfig(false);
//      core.statsManager.init();
//      core.checkForUpdates();
//    }));
    }

    @Override
    public void onInitialize() {
        core.enable();
        core.initializeScheduler();
        core.configManager.loadConfig(false);
        core.statsManager.init();
        core.checkForUpdates();
        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
            core.scheduler.shutdown();
            core.statsManager.getMetrics().shutdown();
        });

//        SenderFactory<KnockbackSyncBase, FabricCommandSource> fabricSenderFactory = new FabricSenderFactory(null);
//        LiteralArgumentBuilder<FabricCommandSource> fabricCommand = ReloadSubCommand.build(fabricSenderFactory);
    }
}
