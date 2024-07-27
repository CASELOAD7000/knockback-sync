package me.caseload.kbsync.listener;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPosition;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPositionAndRotation;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import net.jafama.FastMath;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LagCompensator implements Listener {

    private static final Logger LOGGER = Logger.getLogger(LagCompensator.class.getName());
    private final ListMultimap<UUID, Pair<Location, Long>> locationTimes = ArrayListMultimap.create();
    private final AtomicBoolean enableLagCompensation = new AtomicBoolean(true); // Default to true for simplicity
    private final int historySize = 40; // Default value
    private final int pingOffset = 120; // Default value
    private final int timeResolution = 30; // Default value
    private final double compensationFactor = 1.0; // Default value
    private final ExecutorService executorService;

    public LagCompensator() {
        // Create a ThreadFactory that sets thread priority to HIGH
        ThreadFactory highPriorityThreadFactory = r -> {
            Thread thread = new Thread(r);
            thread.setPriority(Thread.MAX_PRIORITY); // Set to highest priority
            return thread;
        };

        // Initialize the ExecutorService with the high priority ThreadFactory
        this.executorService = Executors.newFixedThreadPool(2, highPriorityThreadFactory);

        // Register the PacketListener
        PacketEvents.getAPI().getEventManager().registerListener(new PositionPacketListener(), PacketListenerPriority.HIGH);
    }

    private class PositionPacketListener implements PacketListener {
        @Override
        public void onPacketReceive(PacketReceiveEvent event) {
            try {
                // Leer los datos necesarios del paquete en el hilo de recepción del paquete
                if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION) {
                    WrapperPlayClientPlayerPosition packet = new WrapperPlayClientPlayerPosition(event);
                    Player player = (Player) event.getPlayer();
                    Location newLocation = new Location(
                        player.getWorld(),
                        packet.getLocation().getX(),
                        packet.getLocation().getY(),
                        packet.getLocation().getZ()
                    );

                    // Enviar la tarea al ExecutorService
                    executorService.submit(() -> registerMovement(player, newLocation));
                } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
                    WrapperPlayClientPlayerPositionAndRotation packet = new WrapperPlayClientPlayerPositionAndRotation(event);
                    Player player = (Player) event.getPlayer();
                    Location newLocation = new Location(
                        player.getWorld(),
                        packet.getLocation().getX(),
                        packet.getLocation().getY(),
                        packet.getLocation().getZ(),
                        packet.getYaw(),
                        packet.getPitch()
                    );

                    // Enviar la tarea al ExecutorService
                    executorService.submit(() -> registerMovement(player, newLocation));
                } else {
                    LOGGER.warning("[LagCompensator] Received unknown packet type: " + event.getPacketType().getName());
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error processing packet: " + event.getPacketType().getName(), e);
            }
        }
    }

    public Location getHistoryLocation(Player player, int rewindMillisecs) {
        if (!enableLagCompensation.get() || !locationTimes.containsKey(player.getUniqueId())) {
            return player.getLocation();
        }

        List<Pair<Location, Long>> previousLocations = locationTimes.get(player.getUniqueId());
        long currentTime = System.currentTimeMillis();

        int rewindTime = rewindMillisecs + pingOffset;
        int timesSize = previousLocations.size() - 1;

        for (int i = timesSize; i >= 0; i--) {
            Pair<Location, Long> locationPair = previousLocations.get(i);
            long locationTime = locationPair.getValue();
            int elapsedTime = (int) (currentTime - locationTime);

            if (elapsedTime >= rewindTime) {
                if (i == timesSize) {
                    return locationPair.getKey();
                }

                Pair<Location, Long> nextPair = previousLocations.get(i + 1);
                Location before = locationPair.getKey().clone();
                Location after = nextPair.getKey();
                Vector interpolate = after.toVector().subtract(before.toVector());

                double millisSinceLastLoc = currentTime - nextPair.getValue();
                double millisSinceLoc = currentTime - locationTime;
                double movementRelAge = millisSinceLoc - (rewindMillisecs + pingOffset);
                double nextMoveWeight = FastMath.min(1.0, movementRelAge / FastMath.max(1, millisSinceLastLoc) * compensationFactor);

                interpolate.multiply(nextMoveWeight);
                before.add(interpolate);

                return before;
            }
        }

        return player.getLocation();
    }

    private void processPosition(Location loc, Player p) {
        if (!enableLagCompensation.get()) return;

        UUID playerId = p.getUniqueId();
        long currTime = System.currentTimeMillis();
        List<Pair<Location, Long>> locations = locationTimes.get(playerId);

        if (!locations.isEmpty() && currTime - locations.get(locations.size() - 1).getValue() < timeResolution) {
            return;
        }

        locationTimes.put(playerId, Pair.of(loc, currTime));

        if (locations.size() > historySize) {
            locationTimes.remove(playerId, locations.get(0));
        }
    }

    public void registerMovement(Player player, Location to) {
        processPosition(to, player);
    }

    public void clearCache(Player player) {
        locationTimes.removeAll(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        registerMovement(player, to);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
