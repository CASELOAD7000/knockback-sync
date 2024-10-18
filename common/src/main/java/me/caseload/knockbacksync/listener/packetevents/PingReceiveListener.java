package me.caseload.knockbacksync.listener.packetevents;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerKeepAlive;
import me.caseload.knockbacksync.KnockbackSyncBase;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.PlayerData;

public class PingReceiveListener extends PacketListenerAbstract {
// TODO supplment our own ping calculations with over keepalive sent by the server with
//    @Override
//    public void onPacketSend(PacketSendEvent event) {
//        if (event.getPacketType() == PacketType.Play.Server.KEEP_ALIVE) {
//        }
//    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!KnockbackSyncBase.INSTANCE.getConfigManager().isToggled()) return;
        if (event.getPacketType() != PacketType.Play.Client.KEEP_ALIVE) return;

        WrapperPlayClientKeepAlive keepAlive = new WrapperPlayClientKeepAlive(event);
        PlayerData playerData = PlayerDataManager.getPlayerData(event.getUser().getUUID());
        if (playerData == null) return;

        long receivedId = keepAlive.getId();
        System.out.println("Received keepalive with ID: " + receivedId);
        System.out.println("Last sent keepalive ID: " + playerData.getLastKeepAliveID());

        if (playerData.isKeepAliveIDOurs(receivedId)) {
            long ping = (System.nanoTime() - receivedId);
            long diffMillis = ping / 1_000_000;
            // Calculate the remainder nanoseconds for fractional milliseconds
            long remainderNano = ping % 1_000_000;
            System.out.printf("Difference: %d.%03d milliseconds%n", diffMillis, remainderNano / 1_000);
            double diffMillisDouble = ping / 1_000_000.0;
            System.out.printf("Difference: %.3f milliseconds%n", diffMillisDouble);
            System.out.println("Calculated ping: " + ping);
            playerData.setPreviousPing(playerData.getPing());
            playerData.setPing(diffMillisDouble);

            playerData.getJitterCalculator().addPing(ping);
            double jitter = playerData.getJitterCalculator().calculateJitter();
            playerData.setJitter(jitter);
            // Minecraft kicks players that send invalid keepAliveID packets
            // Since Minecraft doesn't know we just sent a keepalive, we gotta cancel it
            // To stop MC from processing it and kicking the player
            event.setCancelled(true);
        } else {
            System.out.println("Received unexpected keepalive ID");
        }
    }
}