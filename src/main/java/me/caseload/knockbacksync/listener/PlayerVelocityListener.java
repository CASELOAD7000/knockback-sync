package me.caseload.knockbacksync.listener;

import me.caseload.knockbacksync.KnockbackSync;
import me.caseload.knockbacksync.manager.KnockbackManager;
import me.caseload.knockbacksync.util.PlayerUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

public class PlayerVelocityListener implements Listener {

    @EventHandler
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        if (!KnockbackSync.getInstance().getConfig().getBoolean("enabled") || event.isCancelled())
            return;

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

        Vector knockback = victim.getVelocity();
        if (victim.isOnGround() || !PlayerUtil.predictiveOnGround(victim, knockback.getY()))
            return;

        victim.setVelocity(KnockbackManager.getCorrectedKnockback(victim.getUniqueId(), knockback));
    }
}