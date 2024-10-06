package me.caseload.knockbacksync.listener;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import me.caseload.knockbacksync.KnockbackSync;
import me.caseload.knockbacksync.manager.PlayerData;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import org.bukkit.entity.Player;

public class PingReceiveListener extends PacketListenerAbstract {

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!KnockbackSync.getInstance().getConfigManager().isToggled())
            return;

        if (event.getPacketType() != PacketType.Play.Client.PONG)
            return;

        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player.getUniqueId());

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