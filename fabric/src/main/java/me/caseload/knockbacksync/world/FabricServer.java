package me.caseload.knockbacksync.world;

import me.caseload.knockbacksync.FabricLoaderMod;
import me.caseload.knockbacksync.player.FabricPlayer;
import me.caseload.knockbacksync.player.PlatformPlayer;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class FabricServer implements PlatformServer {
    public Collection<PlatformPlayer> getOnlinePlayers() {
        return FabricLoaderMod.getServer().getPlayerManager().getPlayerList().stream()
                .map(FabricPlayer::new)
                .collect(Collectors.toList());
    }

    @Override
    public PlatformPlayer getPlayer(UUID uuid) {
        return new FabricPlayer(FabricLoaderMod.getServer().getPlayerManager().getPlayer(uuid));
    }

    @Override
    public PlatformPlayer getPlayer(Object nativePlatformPlayer) {
        return new FabricPlayer((ServerPlayerEntity) nativePlatformPlayer);
    }
}
