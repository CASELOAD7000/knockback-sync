package me.caseload.knockbacksync.world;

import me.caseload.knockbacksync.player.BukkitPlayer;
import me.caseload.knockbacksync.player.PlatformPlayer;
import org.bukkit.Bukkit;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

public class BukkitServer implements PlatformServer {
    public Collection<PlatformPlayer> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().stream()
                .map(BukkitPlayer::new)
                .collect(Collectors.toList());
    }

    @Override
    public PlatformPlayer getPlayer(UUID uuid) {
        return new BukkitPlayer(Bukkit.getPlayer(uuid));
    }
}
