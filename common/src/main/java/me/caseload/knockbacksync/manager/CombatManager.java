package me.caseload.knockbacksync.manager;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatManager {

    private static final Set<@NotNull UUID> combatPlayers = ConcurrentHashMap.newKeySet();

    public static @NotNull Set<@NotNull UUID> getPlayers() {
        return combatPlayers;
    }

    public static void addPlayer(@NotNull UUID uuid) {
        combatPlayers.add(uuid);
    }

    public static void removePlayer(@NotNull UUID uuid) {
        combatPlayers.remove(uuid);
    }
}
