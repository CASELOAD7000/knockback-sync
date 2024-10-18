package me.caseload.knockbacksync.listener.bukkit;

import me.caseload.knockbacksync.listener.PlayerDamageListener;
import me.caseload.knockbacksync.player.BukkitPlayer;
import me.caseload.knockbacksync.util.MultiLibUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class BukkitPlayerDamageListener extends PlayerDamageListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        Entity attacker = event.getDamager();
        if ((victim instanceof Player) && (attacker instanceof Player))
            if (!MultiLibUtil.isExternalPlayer((Player) victim)) {
                onPlayerDamage(new BukkitPlayer((Player) victim), new BukkitPlayer((Player) attacker));
            }
    }
}