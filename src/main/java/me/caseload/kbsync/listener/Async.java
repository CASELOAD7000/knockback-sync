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
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jafama.FastMath;

public class Async implements Listener {

    private static final Logger LOGGER = Logger.getLogger(Async.class.getName());
    private final LagCompensator lagCompensator;
    private final Map<Integer, Player> entityIdCache = new HashMap<>();
    private static final double MAX_HIT_REACH = 3.1;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2); // Usar un grupo de hilos dedicado
    private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock(); // Para acceso concurrente seguro

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

                    executorService.submit(() -> {
                        try {
                            Player target = getPlayerFromEntityId(entityId);
                            if (target != null && isHitValid(attacker, target)) {
                                // Añadir un pequeño retraso para permitir que otros plugins modifiquen el knockback primero
                                Bukkit.getScheduler().runTaskLater(KbSync.getInstance(), () -> handleHit(attacker, target), 1L);
                            }
                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, "Error processing hit: " + e.getMessage(), e);
                        }
                    });
                }
            } else {
                LOGGER.warning("Received unknown packet type: " + event.getPacketType().getName());
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
        return attacker.getLocation().distance(target.getLocation()) <= MAX_HIT_REACH;
    }

    private void handleHit(Player attacker, Player target) {
        Location compensatedLocation = lagCompensator.getHistoryLocation(target, 100);

        // Reducir la salud del objetivo
        target.setHealth(Math.max(0, target.getHealth() - 1));

        // Ajuste de los valores de knockback
        double yawRadians = FastMath.toRadians(attacker.getLocation().getYaw());
        double knockbackX = -FastMath.sin(yawRadians) * 0.2; // Reducir el valor para menos movimiento horizontal
        double knockbackZ = FastMath.cos(yawRadians) * 0.2; // Reducir el valor para menos movimiento horizontal
        double knockbackY = 0.1; // Mantener el mismo valor vertical si es necesario
        Vector knockback = new Vector(knockbackX, knockbackY, knockbackZ);

        // Calcular la dirección de la compensación
        Vector direction = compensatedLocation.toVector().subtract(target.getLocation().toVector()).normalize();
        knockback.add(direction.multiply(0.2)); // Ajustar el multiplicador para una menor influencia de la compensación

        // Aplicar el knockback
        Vector velocity = target.getVelocity();
        target.setVelocity(velocity.add(knockback));

        // Llamar al evento de velocidad del jugador
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
