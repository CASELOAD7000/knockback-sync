package me.caseload.knockbacksync.manager;

import com.github.retrooper.packetevents.protocol.player.User;
import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.player.PlatformPlayer;
import me.caseload.knockbacksync.player.PlayerData;
import me.caseload.knockbacksync.util.FloodgateUtil;
import me.caseload.knockbacksync.util.GeyserUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private static final Map<User, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    public static @Nullable PlayerData getPlayerData(@NotNull User user) {
        return playerDataMap.get(user);
    }

    public static void addPlayerData(@Nullable User user, @Nullable PlatformPlayer platformPlayer) {
        if (user == null || platformPlayer == null) {
            return;
        }

        if (!shouldExempt(platformPlayer.getUUID())) {
            PlayerData playerData = new PlayerData(user, platformPlayer);
            playerDataMap.put(user, playerData);
            Base.INSTANCE.getEventBus().registerListeners(playerData);
        }
    }

    public static void removePlayerData(@NotNull User user) {
        PlayerData playerData = playerDataMap.remove(user);
        if (playerData != null)
            Base.INSTANCE.getEventBus().unregisterListeners(playerData);
    }

    public static boolean containsPlayerData(@NotNull User user) {
        return playerDataMap.containsKey(user);
    }

    public static boolean shouldExempt(@NotNull UUID uuid) {
        // Geyser players don't have Java movement
        return GeyserUtil.isGeyserPlayer(uuid)
                // Floodgate is the authentication system for Geyser on servers that use Geyser as a proxy instead of installing it as a plugin directly on the server
                || FloodgateUtil.isFloodgatePlayer(uuid)
                // Geyser formatted player string
                // This will never happen for Java players, as the first character in the 3rd group is always 4 (xxxxxxxx-xxxx-4xxx-xxxx-xxxxxxxxxxxx)
                || uuid.toString().startsWith("00000000-0000-0000-0009");
    }
}
