package me.caseload.kbsync.listener;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Async implements Listener {

    private final LagCompensator lagCompensator;
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
                    final Player attacker = (Player) event.getPlayer();
                    final int entityId = interactEntityPacket.getEntityId();

                    executorService.submit(() -> {
                        final Player target = getPlayerFromEntityId(entityId);
                        if (target != null && isHitValid(attacker, target)) {
                            Bukkit.getScheduler().runTask(KbSync.getInstance(), () -> handleHit(attacker, target));
                        }
                    });
                }
            }
        }
    }

    private Player getPlayerFromEntityId(int entityId) {
        cacheLock.readLock().lock();
        try {
            return Bukkit.getOnlinePlayers().stream()
                    .filter(player -> player.getEntityId() == entityId)
                    .findFirst()
                    .orElse(null);
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    private boolean isHitValid(Player attacker, Player target) {
        return attacker.getWorld().equals(target.getWorld()) &&
               FastMath.sqrt(FastMath.pow2(attacker.getLocation().getX() - target.getLocation().getX()) +
                             FastMath.pow2(attacker.getLocation().getY() - target.getLocation().getY()) +
                             FastMath.pow2(attacker.getLocation().getZ() - target.getLocation().getZ())) <= reach;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        cacheLock.writeLock().lock();
        try {
            // No se usa entityIdCache en este caso
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        cacheLock.writeLock().lock();
        try {
            // No se usa entityIdCache en este caso
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    @EventHandler
    public void detectHit(PlayerAnimationEvent e) {
        final Player attacker = e.getPlayer();
        final Location attackerLoc = attacker.getLocation();
        final Vector attackerPos = attackerLoc.toVector().add(new Vector(0, attacker.isSneaking() ? 1.52625 : 1.62, 0));
        final List<Entity> nearbyEntities = attacker.getNearbyEntities(reach + 2, reach + 2, reach + 2);
        final AABB victimBox = playerBoundingBox.clone();
        final Vector boxOffset = playerBoundingBox.getMin();
        Ray ray = null;
        double hitDistance = Double.MAX_VALUE;
        Player victim = null;
        final int ping = PacketEvents.getAPI().getPlayerUtils().getPing(attacker);

        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof Player)) continue;

            Player potentialVictim = (Player) entity;
            ray = new Ray(attackerPos, attackerLoc.getDirection());
            final Location compensatedLocation = lagCompensator.getHistoryLocation(ping, potentialVictim);
            if (compensatedLocation == null) continue;

            victimBox.translateTo(compensatedLocation.toVector());
            victimBox.translate(boxOffset);
            Vector intersection = victimBox.intersectsRay(ray, 0, reach);

            if (intersection == null) continue;
            double chkHitDistance = intersection.distance(attackerPos);

            if (chkHitDistance < hitDistance) {
                hitDistance = chkHitDistance;
                victim = potentialVictim;
            }
        }

        if (victim == null || ray == null) return;

        final int blockIterIterations = (int) hitDistance;
        if (blockIterIterations != 0) {
            BlockIterator iter = new BlockIterator(attacker.getWorld(), attackerPos, ray.getDirection(), 0, blockIterIterations);
            while (iter.hasNext()) {
                if (iter.next().getType().isSolid()) return;
            }
        }

        final Player finalVictim = victim;
        Bukkit.getScheduler().runTask(KbSync.getInstance(), () -> Bukkit.getPluginManager().callEvent(new ServerSidePlayerHitEvent(attacker, finalVictim)));
    }

    private void handleHit(Player attacker, Player target) {
        // Lógica del golpe ya está manejada en detectHit, este método puede estar vacío
    }
}
