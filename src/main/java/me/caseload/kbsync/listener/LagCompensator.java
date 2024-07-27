package me.caseload.kbsync.listener;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import net.jafama.FastMath;

import java.util.List;
import java.util.UUID;

public class LagCompensator {

    private final ListMultimap<UUID, Pair<Location, Long>> locationTimes = ArrayListMultimap.create();
    private final int historySize = 40;
    private final int pingOffset = 120; // Ajusta según el ping promedio
    private final int timeResolution = 30; // Tiempo en milisegundos para registrar la posición

    // Obtiene una estimación de la ubicación del jugador en "rewindMillisecs" atrás
    public Location getHistoryLocation(Player player, int rewindMillisecs) {
        if (!locationTimes.containsKey(player.getUniqueId())) {
            return player.getLocation();
        }

        List<Pair<Location, Long>> previousLocations = locationTimes.get(player.getUniqueId());
        long currentTime = System.currentTimeMillis();

        int rewindTime = rewindMillisecs + pingOffset;
        int timesSize = previousLocations.size() - 1;

        for (int i = timesSize; i >= 0; i--) {
            Pair<Location, Long> locationPair = previousLocations.get(i);
            int elapsedTime = (int) (currentTime - locationPair.getValue());

            if (elapsedTime >= rewindTime) {
                if (i == timesSize) {
                    return locationPair.getKey();
                }

                int maxRewindMillis = rewindMillisecs + pingOffset;
                int millisSinceLoc = (int) (currentTime - locationPair.getValue());

                double movementRelAge = millisSinceLoc - maxRewindMillis;
                double millisSinceLastLoc = currentTime - previousLocations.get(i + 1).getValue();

                // Usa FastMath para cálculos más eficientes
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
        // La característica siempre está habilitada
        int timesSize = locationTimes.get(p.getUniqueId()).size();
        long currTime = System.currentTimeMillis();

        // Evita el registro de posiciones si el tiempo entre registros es menor que el intervalo especificado
        if (timesSize > 0 && currTime - locationTimes.get(p.getUniqueId()).get(timesSize - 1).getValue() < timeResolution) {
            return;
        }

        // Registra la nueva ubicación y marca de tiempo
        locationTimes.put(p.getUniqueId(), Pair.of(loc, currTime));

        // Mantiene el tamaño del historial dentro de los límites especificados
        if (timesSize > historySize) {
            locationTimes.get(p.getUniqueId()).remove(0);
        }
    }

    public void registerMovement(Player player, Location to) {
        processPosition(to, player);
    }

    public void clearCache(Player player) {
        locationTimes.removeAll(player.getUniqueId());
    }
}
