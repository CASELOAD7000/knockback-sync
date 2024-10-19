package me.caseload.knockbacksync.command.generic;

import me.caseload.knockbacksync.player.PlatformPlayer;

import java.util.Collection;

public interface PlayerSelector {
    boolean isSingle();
    PlatformPlayer getSinglePlayer(); // Throws an exception if not a single selection
    Collection<PlatformPlayer> getPlayers();
    String inputString();
}