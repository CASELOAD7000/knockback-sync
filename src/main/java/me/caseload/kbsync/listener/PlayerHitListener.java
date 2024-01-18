package me.caseload.kbsync.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import me.caseload.kbsync.KbSync;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerHitListener implements Listener {

    private final ProtocolManager protocolManager;

    public PlayerHitListener(ProtocolManager protocolManager) {
        this.protocolManager = protocolManager;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled()
                || !(event.getEntity() instanceof Player)
                || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        double calculatedValue = calculateYAxis(damager, victim);
        KbSync.kb.put(victim.getUniqueId(), calculatedValue);

        String pingRetrievalMethod = KbSync.getInstance().getConfig().getString("ping_retrieval.method").toLowerCase();

        if (pingRetrievalMethod.equals("hit")) {
            Bukkit.getScheduler().runTaskAsynchronously(KbSync.getInstance(), () -> {
                KbSync.sendPingPacket(victim, protocolManager.createPacket(PacketType.Play.Server.PING));
            });
        }
    }

    public static double calculateYAxis(Player attacker, Player victim) {
        float attackCooldown = attacker.getAttackCooldown();
        double a = (attackCooldown > 0.9) ? 0.4 : 0.36080000519752503;

        if (!attacker.isSprinting()) {
            a = 0.36080000519752503;
            double b = victim.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).getValue() * 10;
            double c = 0.04000000119 * b;
            a -= c;
        }

        return a;
    }
}
