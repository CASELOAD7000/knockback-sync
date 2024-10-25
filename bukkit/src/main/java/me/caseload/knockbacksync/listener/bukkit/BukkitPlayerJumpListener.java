package me.caseload.knockbacksync.listener.bukkit;

import me.caseload.knockbacksync.listener.PlayerJumpListener;
import me.caseload.knockbacksync.player.BukkitPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class BukkitPlayerJumpListener extends PlayerJumpListener implements Listener {

    @EventHandler
    public void onPlayerJump(PlayerMoveEvent event) {
        if (event.getFrom().getY() == event.getTo().getY())
            return;

        onPlayerJump(new BukkitPlayer(event.getPlayer()));
    }
}
