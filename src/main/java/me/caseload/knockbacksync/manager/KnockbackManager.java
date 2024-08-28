package me.caseload.knockbacksync.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.util.Vector;

public class KnockbackManager {

    private static final Map<UUID, Double> knockbackMap = new HashMap<>();

    public static Map<UUID, Double> getKnockbackMap() {
        return knockbackMap;
    }

    public static Vector getCorrectedKnockback(UUID uuid, Vector originalKnockback) {
        return originalKnockback.clone().setY(knockbackMap.get(uuid));
    }
}