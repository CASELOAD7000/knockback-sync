package me.caseload.knockbacksync.listener.bukkit;

import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.listener.PlayerKnockbackListener;
import me.caseload.knockbacksync.manager.PlayerData;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.BukkitPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

public class BukkitPlayerKnockbackListener extends PlayerKnockbackListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        Player victim = event.getPlayer();
        EntityDamageEvent entityDamageEvent = victim.getLastDamageCause();
        if (entityDamageEvent == null)
            return;

        EntityDamageEvent.DamageCause damageCause = entityDamageEvent.getCause();
        if (damageCause != EntityDamageEvent.DamageCause.ENTITY_ATTACK)
            return;

        Entity attacker = ((EntityDamageByEntityEvent) entityDamageEvent).getDamager();
        if (!(attacker instanceof Player))
            return;

        onPlayerVelocity(new BukkitPlayer(victim), event.getVelocity());
//        event.setVelocity(victim.getVelocity()); // Update the event's velocity in Spigot
    }
}