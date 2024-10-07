package me.caseload.knockbacksync.manager;

import io.github.retrooper.packetevents.util.GeyserUtil;
import me.caseload.knockbacksync.util.FloodgateUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private static final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();
    private static final Set<UUID> exemptPlayers = ConcurrentHashMap.newKeySet();

    public static @NotNull PlayerData getPlayerData(UUID uuid) {
        if (!playerDataMap.containsKey(uuid) && !exemptPlayers.contains(uuid))
            addPlayerData(uuid, new PlayerData(Bukkit.getPlayer(uuid)));

        return playerDataMap.get(uuid);
    }

    public static void addPlayerData(@NotNull UUID uuid, @NotNull PlayerData playerData) {
        if (!willExempt(uuid))
            playerDataMap.put(uuid, playerData);
    }

    public static void removePlayerData(@NotNull UUID uuid) {
        playerDataMap.remove(uuid);
    }

    public static boolean isExempt(@NotNull UUID uuid) {
        return exemptPlayers.contains(uuid);
    }

    public static void setExempt(@NotNull UUID uuid, boolean state) {
        if (state)
            exemptPlayers.add(uuid);
        else
            exemptPlayers.remove(uuid);
    }

    private static boolean willExempt(@NotNull UUID uuid) {
        if (exemptPlayers.contains(uuid))
            return true;

        // Geyser players don't have Java movement
        if (GeyserUtil.isGeyserPlayer(uuid)
                // Floodgate is the authentication system for Geyser on servers that use Geyser as a proxy instead of installing it as a plugin directly on the server
                || FloodgateUtil.isFloodgatePlayer(uuid)
                // Geyser formatted player string
                // This will never happen for Java players, as the first character in the 3rd group is always 4 (xxxxxxxx-xxxx-4xxx-xxxx-xxxxxxxxxxxx)
                || uuid.toString().startsWith("00000000-0000-0000-0009")) {
            exemptPlayers.add(uuid);
            return true;
        }

        return false;
    }

}
