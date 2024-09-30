package me.caseload.knockbacksync.manager;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatManager {

    private static final Set<UUID> combatPlayers = ConcurrentHashMap.newKeySet();

    public static @NotNull Set<UUID> getPlayers() {
        return combatPlayers;
    }

    public static void addPlayer(UUID uuid) {
        combatPlayers.add(uuid);
    }

    public static void removePlayer(UUID uuid) {
        combatPlayers.remove(uuid);
    }
}
