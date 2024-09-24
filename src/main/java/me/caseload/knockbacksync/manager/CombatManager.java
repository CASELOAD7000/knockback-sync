package me.caseload.knockbacksync.manager;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class CombatManager {

    private static final Set<UUID> combatPlayers = new HashSet<>();

    public static @NotNull Set<UUID> getPlayers() {
        return Collections.unmodifiableSet(combatPlayers);
    }

    public static void addPlayer(UUID uuid) {
        combatPlayers.add(uuid);
    }

    public static void removePlayer(UUID uuid) {
        combatPlayers.remove(uuid);
    }
}
