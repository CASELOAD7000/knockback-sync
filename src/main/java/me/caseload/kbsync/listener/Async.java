package me.caseload.kbsync.listener;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import me.caseload.kbsync.KbSync;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;
import net.jafama.FastMath;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class Async implements Listener {

    private final LagCompensator lagCompensator;
    private final Map<Integer, Player> entityIdCache = new HashMap<>();
    private static final double MAX_HIT_REACH = 3.1;

    public Async(LagCompensator lagCompensator) {
        this.lagCompensator = lagCompensator;

        // Registrar los eventos de Bukkit
        Bukkit.getPluginManager().registerEvents(this, KbSync.getInstance());

        // Registrar el PacketListener
        PacketEvents.getAPI().getEventManager().registerListener(new HitPacketListener(), PacketListenerPriority.HIGHEST);
    }

    private class HitPacketListener implements PacketListener {
        @Override
        public void onPacketReceive(PacketReceiveEvent event) {
            if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
                WrapperPlayClientInteractEntity interactEntityPacket = new WrapperPlayClientInteractEntity(event);
                if (interactEntityPacket.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                    Player attacker = (Player) event.getPlayer();
                    int entityId = interactEntityPacket.getEntityId();

                    Player target = getPlayerFromEntityId(entityId);
                    if (target != null && isHitValid(attacker, target)) {
                        CompletableFuture.runAsync(() -> handleHit(attacker, target), ForkJoinPool.commonPool());
                    }
                }
            }
        }
    }

    private Player getPlayerFromEntityId(int entityId) {
        return entityIdCache.getOrDefault(entityId, Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.getEntityId() == entityId)
                .findFirst()
                .orElse(null));
    }

    private boolean isHitValid(Player attacker, Player target) {
        return attacker.getLocation().distance(target.getLocation()) <= MAX_HIT_REACH;
    }

    private void handleHit(Player attacker, Player target) {
        Location compensatedLocation = lagCompensator.getHistoryLocation(target, 100);

        target.setHealth(Math.max(0, target.getHealth() - 1));

        double yawRadians = FastMath.toRadians(attacker.getLocation().getYaw());
        double knockbackX = -FastMath.sin(yawRadians) * 0.5;
        double knockbackZ = FastMath.cos(yawRadians) * 0.5;
        double knockbackY = 0.1;
        Vector knockback = new Vector(knockbackX, knockbackY, knockbackZ);

        Vector direction = compensatedLocation.toVector().subtract(target.getLocation().toVector()).normalize();
        knockback.add(direction.multiply(0.5));

        Vector velocity = target.getVelocity();
        target.setVelocity(velocity.add(knockback));

        PlayerVelocityEvent event = new PlayerVelocityEvent(target, target.getVelocity());
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            target.setVelocity(event.getVelocity());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        lagCompensator.registerMovement(event.getPlayer(), event.getPlayer().getLocation());
    }
}
