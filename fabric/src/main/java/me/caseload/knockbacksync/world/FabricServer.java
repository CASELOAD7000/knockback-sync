package me.caseload.knockbacksync.world;

import me.caseload.knockbacksync.KnockbackSyncFabric;
import me.caseload.knockbacksync.player.FabricPlayer;
import me.caseload.knockbacksync.player.PlatformPlayer;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class FabricServer implements PlatformServer {
    public Collection<PlatformPlayer> getOnlinePlayers() {
        return KnockbackSyncFabric.getServer().getPlayerList().getPlayers().stream()
                .map(FabricPlayer::new)
                .collect(Collectors.toList());
    }

    @Override
    public PlatformPlayer getPlayer(UUID uuid) {
        return new FabricPlayer(KnockbackSyncFabric.getServer().getPlayerList().getPlayer(uuid));
    }

}
