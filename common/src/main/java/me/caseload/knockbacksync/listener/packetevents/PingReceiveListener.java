package me.caseload.knockbacksync.listener.packetevents;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.manager.PlayerData;
import me.caseload.knockbacksync.manager.PlayerDataManager;

import java.util.UUID;

public class PingReceiveListener extends PacketListenerAbstract {

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!KnockbackSyncBase.INSTANCE.getConfigManager().isToggled())
            return;

        if (event.getPacketType() != PacketType.Play.Client.PONG)
            return;

        WrapperPlayClientPong pong = new WrapperPlayClientPong(event);
        PlayerData playerData = PlayerDataManager.getPlayerData(event.getUser().getUUID());
        if (playerData == null) return;

        int id = pong.getId();
        if (playerData.isPingIdOurs(id)) {
            Long sendTime = playerData.getTimeline().remove(id);
            if (sendTime != null) {
                long ping = System.currentTimeMillis() - sendTime;
                playerData.setPreviousPing(playerData.getPing());
                playerData.setPing(ping);
            }
        }
    }
}