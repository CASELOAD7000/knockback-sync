package me.caseload.kbsync;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.*;
import me.caseload.kbsync.command.Subcommands;
import me.caseload.kbsync.listener.PlayerHitListener;
import me.caseload.kbsync.listener.PlayerVelocityListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class KbSync extends JavaPlugin {

    private static KbSync instance;
    private static ProtocolManager protocolManager;

    private static final Map<UUID, List<Long>> keepAliveTime = Collections.synchronizedMap(new HashMap<UUID, List<Long>>());
    private final Map<UUID, Integer> accuratePing = new HashMap<>();

    public static final Map<UUID, Double> kb = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        protocolManager = ProtocolLibrary.getProtocolManager();

        saveDefaultConfig();
        setupProtocolLib();

        String pingRetrievalMethod = getConfig().getString("ping_retrieval.method").toLowerCase();

        if (pingRetrievalMethod.equals("runnable")) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
                PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.PING);
                Bukkit.getOnlinePlayers().forEach(player -> sendPingPacket(player, packet));
            }, 0L, getConfig().getInt("ping_retrieval.runnable_ticks"));
            Bukkit.getLogger().info("[KbSync] Started bukkit runnable");
        } else if (!pingRetrievalMethod.equals("hit")) {
            Bukkit.getLogger().severe("[KbSync] Disabling plugin. The ping retrieval method \"" + pingRetrievalMethod + "\" does not exist.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new PlayerHitListener(protocolManager), this);
        Bukkit.getLogger().info("[KbSync] Registered PlayerHitListener");
        Bukkit.getLogger().info("[KbSync] Using the \"" + pingRetrievalMethod + "\" ping retrieval method.");

        getServer().getPluginManager().registerEvents(new PlayerVelocityListener(accuratePing), this);
        getCommand("knockbacksync").setExecutor(new Subcommands(accuratePing));
        getCommand("knockbacksync").setTabCompleter(new Subcommands(accuratePing));
    }

    public static KbSync getInstance() {
        return instance;
    }

    public static void sendPingPacket(Player p, PacketContainer packet) {
        UUID uuid = p.getUniqueId();
        Long currentTime = System.currentTimeMillis();
        List<Long> timeData = keepAliveTime.get(uuid);
        if (timeData == null) {
            timeData = new ArrayList<Long>(2);
            timeData.add(0L);
            timeData.add(0L);
        }
        timeData.set(0, currentTime);
        keepAliveTime.put(uuid, timeData);
        protocolManager.sendServerPacket(p, packet);
    }

    private void setupProtocolLib() {
        protocolManager.addPacketListener(new PacketAdapter(
                this,
                ListenerPriority.NORMAL,
                PacketType.Play.Client.PONG)
        {
            @Override
            public void onPacketReceiving(PacketEvent event)
            {
                Long currentTime = System.currentTimeMillis();
                final Player player = event.getPlayer();
                UUID uuid = player.getUniqueId();

                Long pingTime = 0L;
                List<Long> timeData = keepAliveTime.get(uuid);
                if (timeData == null)
                {
                    timeData = new ArrayList<Long>(2);
                    timeData.add(0L);
                    timeData.add(0L);
                }
                else
                {
                    pingTime = currentTime - timeData.get(0);
                    timeData.set(1, pingTime);
                }
                keepAliveTime.put(uuid, timeData);
                final Long ping = pingTime;

                int exactPing = 0;
                try {
                    exactPing = Math.toIntExact(ping);
                } catch(ArithmeticException ignored) {}
                if(exactPing>10000) exactPing = 0;
                if (player.isOnline())
                {
                    accuratePing.put(uuid, exactPing);
                }
            }
        });
    }
}
