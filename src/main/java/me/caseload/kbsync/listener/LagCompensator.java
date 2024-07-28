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
import net.jafama.FastMath;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public class LagCompensator implements Listener {

    public final ListMultimap<UUID, Pair<Location, Long>> locationTimes = ArrayListMultimap.create();
    public final AtomicBoolean enableLagCompensation = new AtomicBoolean(true); // Default to true for simplicity
    private final int historySize = 20; // Default value
    private final int pingOffset = 120; // Default value
    private final int timeResolution = 35; // Default value
    private final double compensationFactor = 0.5; // Default value
    public final ExecutorService executorService;

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

    public LagCompensator(ExecutorService executorService) {
        this.executorService = executorService;

        // Register the PacketListener
        PacketEvents.getAPI().getEventManager().registerListener(new PositionPacketListener(), PacketListenerPriority.HIGH);
    }

    private class PositionPacketListener implements PacketListener {
        @Override
        public void onPacketReceive(PacketReceiveEvent event) {
            try {
                Player player = (Player) event.getPlayer();
                if (player == null) {
                    return;
                }

                Location newLocation = null;
                if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION) {
                    WrapperPlayClientPlayerPosition packet = new WrapperPlayClientPlayerPosition(event);
                    newLocation = new Location(
                        player.getWorld(),
                        packet.getLocation().getX(),
                        packet.getLocation().getY(),
                        packet.getLocation().getZ()
                    );
                } else if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
                    WrapperPlayClientPlayerPositionAndRotation packet = new WrapperPlayClientPlayerPositionAndRotation(event);
                    newLocation = new Location(
                        player.getWorld(),
                        packet.getLocation().getX(),
                        packet.getLocation().getY(),
                        packet.getLocation().getZ(),
                        packet.getYaw(),
                        packet.getPitch()
                    );
                }

                if (newLocation != null) {
                    // Enviar la tarea al ExecutorService
                    final Location finalNewLocation = newLocation;
                    executorService.submit(() -> registerMovement(player, finalNewLocation));
                }
            } catch (Exception e) {
                e.printStackTrace(); // Print stack trace for errors
            }
        }
    }

    public Location getHistoryLocation(int rewindMillisecs, Player player) {
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
                Location before = (i > 0) ? previousLocations.get(i - 1).getKey() : locationPair.getKey();
                Location current = locationPair.getKey();
                Location after = nextPair.getKey();

                double millisSinceNextLoc = currentTime - nextPair.getValue();
                double millisSinceLoc = currentTime - locationTime;
                double movementRelAge = millisSinceLoc - (rewindMillisecs + pingOffset);

                if (millisSinceNextLoc <= 0) {
                    millisSinceNextLoc = 1; // Evita división por cero
                }

                double t = movementRelAge / millisSinceNextLoc;
                t = FastMath.min(1.0, FastMath.max(0.0, t)); // Asegura que t esté en el rango [0, 1]

                Location interpolatedLocation = interpolateCubic(before, current, after, t * compensationFactor);

                if (isLocationValid(interpolatedLocation)) {
                    return interpolatedLocation;
                } else {
                    // Log de advertencia si se encuentra un valor no finito
                    Bukkit.getLogger().warning("Compensated location contains non-finite values: " + interpolatedLocation.toVector());
                    return player.getLocation(); // Fallback en caso de valores no finitos
                }
            }
        }

        return player.getLocation();
    }

    public Location interpolateCubic(Location before, Location current, Location after, double t) {
        double t2 = t * t;
        double t3 = t2 * t;
        double a = -0.5 * t3 + t2 - 0.5 * t;
        double b = 1.5 * t3 - 2.5 * t2 + 1.0;
        double c = -1.5 * t3 + 2.0 * t2 + 0.5 * t;
        double d = 0.5 * t3 - 0.5 * t2;

        double x = a * before.getX() + b * current.getX() + c * after.getX() + d * (after.getX() - before.getX());
        double y = a * before.getY() + b * current.getY() + c * after.getY() + d * (after.getY() - before.getY());
        double z = a * before.getZ() + b * current.getZ() + c * after.getZ() + d * (after.getZ() - before.getZ());

        return new Location(current.getWorld(), x, y, z);
    }

    private boolean isLocationValid(Location location) {
        return location != null &&
               Double.isFinite(location.getX()) &&
               Double.isFinite(location.getY()) &&
               Double.isFinite(location.getZ());
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
