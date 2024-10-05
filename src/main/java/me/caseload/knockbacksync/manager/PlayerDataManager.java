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
    public static final Collection<UUID> exemptPlayers = Collections.synchronizedCollection(new HashSet<>());

    public static @NotNull PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    public static void addPlayerData(UUID uuid, PlayerData playerData) {
        if (shouldCheck(uuid)) {
            playerDataMap.put(uuid, playerData);
        }
    }

    public static boolean shouldCheck(UUID uuid) {
        if (exemptPlayers.contains(uuid)) return false;

        if (uuid != null) {
            // Geyser players don't have Java movement
            if (GeyserUtil.isGeyserPlayer(uuid)
                // Floodgate is the authentication system for Geyser on servers that use Geyser as a proxy instead of installing it as a plugin directly on the server
                || FloodgateUtil.isFloodgatePlayer(uuid)
                // Geyser formatted player string
                // This will never happen for Java players, as the first character in the 3rd group is always 4 (xxxxxxxx-xxxx-4xxx-xxxx-xxxxxxxxxxxx)
                || uuid.toString().startsWith("00000000-0000-0000-0009")) {
                exemptPlayers.add(uuid);
                return false;
            }

            // Has exempt permission
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.hasPermission("knockbacksync.exempt")) {
                exemptPlayers.add(uuid);
                return false;
            }
        }
        return true;
    }

    public static void removePlayerData(UUID uuid) {
        playerDataMap.remove(uuid);
    }
}
