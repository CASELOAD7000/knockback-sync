package me.caseload.kbsync.listener;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.apache.commons.lang3.tuple.Pair;
import me.caseload.kbsync.KbSync;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import net.jafama.FastMath; // Import de Jafama FastMath

import java.util.List;
import java.util.UUID;

public class LagCompensator {

    private final ListMultimap<UUID, Pair<Location, Long>> locationTimes = ArrayListMultimap.create();
    private final int historySize = 40;
    private final int timeResolution = 30;

    private final KbSync kbSync = KbSync.getInstance();

    // Constructor por defecto
    public LagCompensator() {
    }

    public Location getHistoryLocation(Player player, int baseRewindMillisecs) {
        int ping = kbSync.getAccuratePing(player.getUniqueId()); // Obtener el ping del jugador
        int rewindMillisecs = baseRewindMillisecs + ping;

        if (!locationTimes.containsKey(player.getUniqueId())) return player.getLocation();

        List<Pair<Location, Long>> previousLocations = locationTimes.get(player.getUniqueId());
        long currentTime = System.currentTimeMillis();

        int timesSize = previousLocations.size() - 1;

        for (int i = timesSize; i >= 0; i--) {
            Pair<Location, Long> locationPair = previousLocations.get(i);
            int elapsedTime = (int) (currentTime - locationPair.getValue());

            if (elapsedTime >= rewindMillisecs) {
                if (i == timesSize) return locationPair.getKey();

                int maxRewindMilli = rewindMillisecs;
                int millisSinceLoc = (int) (currentTime - locationPair.getValue());

                double movementRelAge = millisSinceLoc - maxRewindMilli;
                double millisSinceLastLoc = currentTime - previousLocations.get(i + 1).getValue();

                // Usando Jafama FastMath para cálculos matemáticos
                double nextMoveWeight = FastMath.max(0, movementRelAge / FastMath.max(1, millisSinceLoc - millisSinceLastLoc));
                Location before = locationPair.getKey().clone();
                Location after = previousLocations.get(i + 1).getKey();
                Vector interpolate = after.toVector().subtract(before.toVector());

                interpolate.multiply(nextMoveWeight);
                before.add(interpolate);

                return before;
            }
        }

        return player.getLocation();
    }

    private void processPosition(Location loc, Player p) {
        if (loc == null || !p.isOnline()) return;

        int timesSize = locationTimes.get(p.getUniqueId()).size();
        long currTime = System.currentTimeMillis();

        if (timesSize > 0 && currTime - locationTimes.get(p.getUniqueId()).get(timesSize - 1).getValue() < timeResolution) return;

        locationTimes.put(p.getUniqueId(), Pair.of(loc, currTime));

        if (timesSize > historySize) locationTimes.get(p.getUniqueId()).remove(0);
    }

    public void registerMovement(Player player, Location newLocation) {
        processPosition(newLocation, player);
    }

    public void clearCache(Player player) {
        locationTimes.removeAll(player.getUniqueId());
    }
}
