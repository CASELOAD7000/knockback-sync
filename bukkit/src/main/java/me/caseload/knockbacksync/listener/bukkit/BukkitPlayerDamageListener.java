package me.caseload.knockbacksync.listener.bukkit;

import me.caseload.knockbacksync.listener.PlayerDamageListener;
import me.caseload.knockbacksync.player.BukkitPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class BukkitPlayerDamageListener extends PlayerDamageListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if ((event.getEntity() instanceof Player victim) && (event.getDamager() instanceof Player attacker))
            onPlayerDamage(new BukkitPlayer(victim), new BukkitPlayer(attacker));

    }
}