package me.caseload.knockbacksync.listener;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import me.caseload.knockbacksync.manager.PingManager;

import java.util.UUID;

public class PacketReceiveListener implements PacketListener {

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() != PacketType.Play.Client.PONG)
            return;

        UUID uuid = event.getUser().getUUID();

        Long sendTime = PingManager.getTimelineMap().get(uuid);
        if (sendTime == null)
            return;

        long ping = System.currentTimeMillis() - sendTime;
        PingManager.getPingMap().put(uuid, ping);
    }
}