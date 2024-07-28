package me.caseload.kbsync.listener;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import net.jafama.FastMath;
import me.caseload.kbsync.KbSync;
import me.caseload.kbsync.ServerSidePlayerHitEvent;
import me.caseload.kbsync.utils.AABB;
import me.caseload.kbsync.utils.Ray;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Async implements Listener {

    private final LagCompensator lagCompensator;
    private final Map<Integer, Player> entityIdCache = new HashMap<>();
    private static final double MAX_HIT_REACH = 3.1;
    private final ExecutorService executorService;
    private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();
    private final AABB playerBoundingBox;
    private final float reach;

    public Async(LagCompensator lagCompensator, ExecutorService executorService) {
        this.lagCompensator = lagCompensator;
        this.executorService = executorService;

        // Inicialización de la caja de colisión y otros parámetros
        double length = 0.9;
        double height = 1.8;
        reach = 4.0f;
        playerBoundingBox = new AABB(new Vector(-length / 2, 0, -length / 2), new Vector(length / 2, height, length / 2));

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

                    executorService.submit(() -> {
                        Player target = getPlayerFromEntityId(entityId);
                        if (target != null && isHitValid(attacker, target)) {
                            Bukkit.getScheduler().runTaskLater(KbSync.getInstance(), () -> handleHit(attacker, target), 1L);
                        }
                    });
                }
            }
        }
    }

    private Player getPlayerFromEntityId(int entityId) {
        cacheLock.readLock().lock();
        try {
            return entityIdCache.getOrDefault(entityId, Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.getEntityId() == entityId)
                    .findFirst()
                    .orElse(null));
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    private boolean isHitValid(Player attacker, Player target) {
        return attacker.getWorld().equals(target.getWorld()) &&
               FastMath.sqrt(FastMath.pow2(attacker.getLocation().getX() - target.getLocation().getX()) +
                             FastMath.pow2(attacker.getLocation().getY() - target.getLocation().getY()) +
                             FastMath.pow2(attacker.getLocation().getZ() - target.getLocation().getZ())) <= MAX_HIT_REACH;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        cacheLock.writeLock().lock();
        try {
            entityIdCache.put(player.getEntityId(), player);
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        cacheLock.writeLock().lock();
        try {
            entityIdCache.remove(player.getEntityId());
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    private void handleHit(Player attacker, Player target) {
        if (target == null || attacker == null) {
            return;
        }

        Location attackerLoc = attacker.getLocation();
        Vector attackerPos = attackerLoc.toVector().add(new Vector(0, attacker.isSneaking() ? 1.52625 : 1.62, 0));
        AABB victimBox = playerBoundingBox.clone();
        Vector boxOffset = playerBoundingBox.getMin();
        Ray ray = new Ray(attackerPos, attackerLoc.getDirection());
        double hitDistance = Double.MAX_VALUE;

        // Obtener la ubicación compensada por el lag
        Location compensatedLocation = lagCompensator.getHistoryLocation(100, target);
        if (compensatedLocation == null) {
            return;
        }

        // Traducir la caja de colisión del objetivo a la ubicación compensada
        victimBox.translateTo(compensatedLocation.toVector());
        victimBox.translate(boxOffset);
        Vector intersection = victimBox.intersectsRay(ray, 0, reach);

        if (intersection != null) {
            double chkHitDistance = intersection.distance(attackerPos);
            if (chkHitDistance < hitDistance) {
                hitDistance = chkHitDistance;
            }

            Bukkit.getPluginManager().callEvent(new ServerSidePlayerHitEvent(attacker, target));
        }
    }
}