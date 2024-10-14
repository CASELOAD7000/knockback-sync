package me.caseload.knockbacksync;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.fabric.FabricPacketEventsBuilder;
import me.caseload.knockbacksync.command.KnockbackSyncCommand;
import me.caseload.knockbacksync.listener.fabric.FabricPlayerDamageListener;
import me.caseload.knockbacksync.listener.fabric.FabricPlayerJoinQuitListener;
import me.caseload.knockbacksync.listener.fabric.FabricPlayerKnockbackListener;
import me.caseload.knockbacksync.permission.FabricPermissionChecker;
import me.caseload.knockbacksync.permission.PermissionChecker;
import me.caseload.knockbacksync.scheduler.FabricSchedulerAdapter;
import me.caseload.knockbacksync.stats.custom.BuildTypePie;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
//import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Logger;

public class KnockbackSyncFabric implements PreLaunchEntrypoint, ModInitializer {

  public static MinecraftServer server;

  private final KnockbackSyncBase core = new KnockbackSyncBase() {

    private final Logger logger = Logger.getLogger(KnockbackSyncFabric.class.getName());
    private final FabricPermissionChecker permissionChecker = new FabricPermissionChecker();

    @Override
    public Logger getLogger() {
      return logger;
    }

    @Override
    public File getDataFolder() {
      return FabricLoader.getInstance().getConfigDir().toFile();
    }

    @Override
    public InputStream getResource(String filename) {
      return getClass().getResourceAsStream("/config.yml");
    }

    @Override
    public void load() {
      PacketEvents.setAPI(FabricPacketEventsBuilder.build("knockbacksync"));
      PacketEvents.getAPI().load();
    }

    @Override
    public void initializeScheduler() {
      scheduler = new FabricSchedulerAdapter();
    }

    @Override
    protected void registerPlatformListeners() {
      new FabricPlayerJoinQuitListener().register();
      new FabricPlayerDamageListener().register();
      new FabricPlayerKnockbackListener().register();
    }

    @Override
    protected void registerCommands() {
//      ServerLifecycleEvents.SERVER_STARTED.register(server -> {
//        CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
//        dispatcher.register(KnockbackSyncCommand.build());
//      });
      CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
        dispatcher.register(KnockbackSyncCommand.build());
        dispatcher.register(
                Commands.literal("kbsync")
                        .redirect(dispatcher.getRoot().getChild("knockbacksync")));
      });
    }

    @Override
    protected String getVersion() {
      return FabricLoader.getInstance().getModContainer("knockbacksync")
              .map(modContainer -> modContainer.getMetadata().getVersion().getFriendlyString())
              .orElse("unknown");
    }

    @Override
    public void saveDefaultConfig() {
      File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "config.yml");
      if (!configFile.exists()) {
        try (InputStream inputStream = getClass().getResourceAsStream("/config.yml")) {
          if (inputStream != null) {
            Files.copy(inputStream, configFile.toPath());
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    @Override
    public PermissionChecker getPermissionChecker() {
      return permissionChecker;
    }

    @Override
    public void initializePacketEvents() {
      PacketEvents.getAPI().getSettings()
              .checkForUpdates(false)
              .debug(false);

      PacketEvents.getAPI().init();
    }
  };

  @Override
  public void onPreLaunch() {
    core.load();
  }

  @Override
  public void onInitialize() {
    ServerLifecycleEvents.SERVER_STARTING.register((minecraftServer -> {
      server = minecraftServer;
    }));
    core.enable();
  }
}
