package me.caseload.knockbacksync.listener.bukkit;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import me.caseload.knockbacksync.listener.PlayerJoinQuitListener;
import me.caseload.knockbacksync.player.BukkitPlayer;
import me.caseload.knockbacksync.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BukkitPlayerJoinQuitListener extends PlayerJoinQuitListener implements Listener {

    public static List<Object> chunkMapList = new ArrayList<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        onPlayerJoin(new PlayerData(new BukkitPlayer(event.getPlayer())));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        onPlayerQuit(event.getPlayer().getUniqueId());
    }
}
