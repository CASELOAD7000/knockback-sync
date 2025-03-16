package me.caseload.knockbacksync.world;

import me.caseload.knockbacksync.player.PlatformPlayer;

import java.util.Collection;
import java.util.UUID;

public interface PlatformServer {
    Collection<PlatformPlayer> getOnlinePlayers();

    PlatformPlayer getPlayer(UUID uuid);

    PlatformPlayer getPlayer(Object nativePlatformPlayer);
}