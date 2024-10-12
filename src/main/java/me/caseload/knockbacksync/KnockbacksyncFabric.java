package me.caseload.knockbacksync;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class KnockbacksyncFabric implements ModInitializer {

  public static MinecraftServer server;

  @Override
  public void onInitialize() {
    System.out.println("Knockbacksync Fabric initialized");
    saveDefaultConfig(); // Ensure the default config is saved
    ServerLifecycleEvents.SERVER_STARTING.register(server -> {
      KnockbacksyncFabric.server = server;
    });
  }

  private void saveDefaultConfig() {
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
}
