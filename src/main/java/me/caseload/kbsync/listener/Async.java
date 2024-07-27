package me.caseload.kbsync.listener;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import net.jafama.FastMath;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import org.bukkit.Location; // Asegúrate de importar la clase Location correcta

public class Async extends JavaPlugin implements Listener {

    private Map<String, Long> delay = new HashMap<>();
    private ProtocolManager protocolManager;
    private LagCompensator lagCompensator;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        protocolManager = ProtocolLibrary.getProtocolManager();
        lagCompensator = new LagCompensator();

        protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, com.comphenix.protocol.PacketType.Play.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == com.comphenix.protocol.PacketType.Play.Client.USE_ENTITY) {
                    Player attacker = event.getPlayer();
                    if (delay.containsKey(attacker.getName())) {
                        long timeElapsed = (System.currentTimeMillis() - delay.get(attacker.getName())) / 1000 * 20;
                        if (timeElapsed < 5) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                    delay.put(attacker.getName(), System.currentTimeMillis());

                    int entityId = event.getPacket().getIntegers().read(0);
                    EnumWrappers.EntityUseAction action = event.getPacket().getEntityUseActions().read(0);

                    if (action == EnumWrappers.EntityUseAction.ATTACK) {
                        Player damaged = null;
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.getEntityId() == entityId) {
                                damaged = player;
                                break;
                            }
                        }

                        if (damaged != null) {
                            // Registra la nueva ubicación del jugador atacado para la compensación de lag
                            lagCompensator.registerMovement(damaged, damaged.getLocation());
                            runAsync(attacker, damaged);
                        }
                    }
                }
            }
        });
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
