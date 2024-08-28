package me.caseload.knockbacksync.listener;

import me.caseload.knockbacksync.KnockbackSync;
import me.caseload.knockbacksync.manager.KnockbackManager;
import me.caseload.knockbacksync.manager.PingManager;
import me.caseload.knockbacksync.util.PlayerUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerDamageListener implements Listener {

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!KnockbackSync.getInstance().getConfig().getBoolean("enabled") || event.isCancelled() || !(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player))
            return;

        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();

        Double modifiedYAxis = PlayerUtil.getModifiedYAxis(victim, attacker);
        KnockbackManager.getKnockbackMap().put(victim.getUniqueId(), modifiedYAxis);
        PingManager.sendPacket(victim);
    }
}