package me.caseload.knockbacksync.manager;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlayerDataManager {

    private static final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    public static @NotNull PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    public static void addPlayerData(UUID uuid, PlayerData playerData) {
        playerDataMap.put(uuid, playerData);
    }

    public static void removePlayerData(UUID uuid) {
        playerDataMap.remove(uuid);
    }
}
