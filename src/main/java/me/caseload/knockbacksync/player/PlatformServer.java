package me.caseload.knockbacksync.player;

import java.util.UUID;

public interface PlatformServer {
    PlatformPlayer getPlayer(UUID uuid);
    // Add more methods as needed
}