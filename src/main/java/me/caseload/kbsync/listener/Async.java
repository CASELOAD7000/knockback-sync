package me.caseload.kbsync.listener;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class Async implements Listener {

    private final ProtocolManager protocolManager;
    private final LagCompensator lagCompensator;

    public Async(ProtocolManager protocolManager, LagCompensator lagCompensator) {
        this.protocolManager = protocolManager;
        this.lagCompensator = lagCompensator;

        // Agregar el listener de paquetes en el constructor
        this.protocolManager.addPacketListener(new PacketAdapter(KbSync.getInstance(), ListenerPriority.NORMAL, com.comphenix.protocol.PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                handlePacket(event);
            }
        });
    }

    private void handlePacket(PacketEvent event) {
        if (event.getPacketType() == com.comphenix.protocol.PacketType.Play.Client.USE_ENTITY) {
            Player attacker = event.getPlayer();

            try {
                int entityId = event.getPacket().getIntegers().read(0);
                EnumWrappers.EntityUseAction action = event.getPacket().getEntityUseActions().read(0);

                if (action == EnumWrappers.EntityUseAction.ATTACK) {
                    Player damaged = Bukkit.getOnlinePlayers().stream()
                            .filter(player -> player.getEntityId() == entityId)
                            .findFirst()
                            .orElse(null);

                    if (damaged != null) {
                        // Registra la nueva ubicación del jugador atacado para la compensación de lag
                        lagCompensator.registerMovement(damaged, damaged.getLocation());
                        runAsync(attacker, damaged);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // Manejo de errores si ocurre algún problema con el paquete
            }
        }
    }

    public void runAsync(Player attacker, Player damaged) {
        CompletableFuture.runAsync(() -> {
            // Obtener la ubicación compensada para ajustar el cálculo del knockback
            Location compensatedLocation = lagCompensator.getHistoryLocation(damaged, 100); // Ajusta el tiempo base según sea necesario

            damaged.setHealth(Math.max(0, damaged.getHealth() - 1));

            // Realizar cálculos de knockback usando FastMath y la ubicación compensada
            double yawRadians = FastMath.toRadians(attacker.getLocation().getYaw());
            double knockbackX = -FastMath.sin(yawRadians) * 0.5;
            double knockbackZ = FastMath.cos(yawRadians) * 0.5;
            double knockbackY = 0.1;
            Vector knockback = new Vector(knockbackX, knockbackY, knockbackZ);

            // Usar la ubicación compensada para ajustar la dirección del knockback
            Vector direction = compensatedLocation.toVector().subtract(damaged.getLocation().toVector()).normalize();
            knockback.add(direction.multiply(0.5)); // Ajusta el factor multiplicador según sea necesario

            Vector velocity = damaged.getVelocity();
            damaged.setVelocity(velocity.add(knockback));

            // Llamar al evento de cambio de velocidad
            PlayerVelocityEvent event = new PlayerVelocityEvent(damaged, damaged.getVelocity());
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                damaged.setVelocity(event.getVelocity());
            }

        }, ForkJoinPool.commonPool());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        lagCompensator.registerMovement(event.getPlayer(), event.getPlayer().getLocation());
    }
}
