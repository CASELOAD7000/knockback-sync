package me.caseload.knockbacksync.world;

import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.player.BukkitPlayer;
import me.caseload.knockbacksync.player.FabricPlayer;
import me.caseload.knockbacksync.player.PlatformPlayer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.stream.Collectors;

public interface PlatformServer {
    static Collection<PlatformPlayer> getOnlinePlayers() {
        switch (KnockbackSyncBase.INSTANCE.platform) {
            case BUKKIT:
            case FOLIA:
                return Bukkit.getOnlinePlayers().stream()
                        .map(BukkitPlayer::new)
                        .collect(Collectors.toList());
            case FABRIC:
                MinecraftServer server = (MinecraftServer) FabricLoader.getInstance().getGameInstance();
                return server.getPlayerList().getPlayers().stream()
                        .map(FabricPlayer::new)
                        .collect(Collectors.toList());
            default:
                throw new IllegalStateException("Unexpected platform: " + KnockbackSyncBase.INSTANCE.platform);
        }
    }
}