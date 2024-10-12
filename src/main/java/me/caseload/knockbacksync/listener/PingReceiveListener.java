package me.caseload.knockbacksync.listener;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.KnockbackSyncPlugin;
import me.caseload.knockbacksync.manager.PlayerData;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import org.bukkit.entity.Player;

public class PingReceiveListener extends PacketListenerAbstract {

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!KnockbackSyncBase.INSTANCE.getConfigManager().isToggled())
            return;

        if (event.getPacketType() != PacketType.Play.Client.PONG)
            return;

        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player.getUniqueId());

        // If player is timed out by the server and removed from the map on the main thread
        // The server can still receive ping packets from the disconnected client
        // At which point the entry will no longer be in the map but this code will be processed!
        if (playerData == null)
            return;

        int packetId = new WrapperPlayClientPong(event).getId();

        Long sendTime = playerData.getTimeline().get(packetId);
        if (sendTime == null)
            return;

        long ping = System.currentTimeMillis() - sendTime;

        playerData.getTimeline().remove(packetId);
        playerData.setPreviousPing(playerData.getPing() != null ? playerData.getPing() : ping);
        playerData.setPing(ping);
    }
}