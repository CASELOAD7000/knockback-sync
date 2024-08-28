package me.caseload.knockbacksync.manager;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PingManager {

    private static final Map<UUID, Long> pingMap = new HashMap<>();
    private static final Map<UUID, Long> timelineMap = new HashMap<>();

    public static Map<UUID, Long> getPingMap() {
        return pingMap;
    }

    public static Map<UUID, Long> getTimelineMap() {
        return timelineMap;
    }

    public static void sendPacket(Player player) {
        WrapperPlayServerPing packet = new WrapperPlayServerPing(1);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
        PingManager.getTimelineMap().put(player.getUniqueId(), System.currentTimeMillis());
    }
}