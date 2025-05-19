package me.caseload.knockbacksync.manager;

import com.github.retrooper.packetevents.protocol.player.User;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CombatManager {

    private static final Set<@NotNull User> combatPlayers = ConcurrentHashMap.newKeySet();

    public static @NotNull Set<@NotNull User> getPlayers() {
        return combatPlayers;
    }

    public static void addPlayer(@NotNull User user) {
        combatPlayers.add(user);
    }

    public static void removePlayer(@NotNull User user) {
        combatPlayers.remove(user);
    }
}
