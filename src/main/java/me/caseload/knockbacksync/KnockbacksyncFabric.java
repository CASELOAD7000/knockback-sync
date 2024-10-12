package me.caseload.knockbacksync;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public class KnockbacksyncFabric implements ModInitializer {

  public static MinecraftServer server;

  @Override
  public void onInitialize() {
    System.out.println("Knockbacksync Fabric initialized");
    ServerLifecycleEvents.SERVER_STARTING.register(server -> {
      KnockbacksyncFabric.server = server;
    });
  }
}
