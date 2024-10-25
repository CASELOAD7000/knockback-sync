package me.caseload.knockbacksync.world;

import me.caseload.knockbacksync.player.PlatformPlayer;

import java.util.Collection;
import java.util.UUID;

public interface PlatformServer {
    Collection<PlatformPlayer> getOnlinePlayers();

    PlatformPlayer getPlayer(UUID uuid);

//        switch (KnockbackSyncBase.INSTANCE.platform) {
//            case BUKKIT:
//            case FOLIA:
//                return Bukkit.getOnlinePlayers().stream()
//                        .map(BukkitPlayer::new)
//                        .collect(Collectors.toList());
//            case FABRIC:
//                MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
//                return server.getPlayerList().getPlayers().stream()
//                        .map(FabricPlayer::new)
//                        .collect(Collectors.toList());
//            default:
//                throw new IllegalStateException("Unexpected platform: " + KnockbackSyncBase.INSTANCE.platform);
//        }
}