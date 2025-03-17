package me.caseload.knockbacksync.listener.packetevents;

import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.PingStrategy;
import me.caseload.knockbacksync.player.PlayerData;
import me.caseload.knockbacksync.util.data.Pair;

import java.util.Queue;
import java.util.UUID;

public class PingReceiveListener extends PacketListenerAbstract {

    public PingReceiveListener() {
        super(PacketListenerPriority.LOWEST);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        // Do not immediately return if KB sync is disabled. This is because if we send a packet, disable the plugin
        // And then receive a response we have to ensure that if the packet was sent by us we cancel it
        // otherwise the server will likely kick the player
        PacketTypeCommon packetType = event.getPacketType();
        User user = event.getUser();
        if (user == null) return;
        PlayerData playerData = PlayerDataManager.getPlayerData(user);
        if (playerData == null) return;

        if (playerData.pingStrategy == PingStrategy.KEEPALIVE && packetType == PacketType.Play.Client.KEEP_ALIVE) {
            WrapperPlayClientKeepAlive keepAlive = new WrapperPlayClientKeepAlive(event);
            long receivedId = keepAlive.getId();

            handlePingCalculationPackets(event, playerData, receivedId, playerData.keepaliveMap);
        } else if (playerData.pingStrategy == PingStrategy.TRANSACTION && packetType == PacketType.Play.Client.PONG) {
            WrapperPlayClientPong pong = new WrapperPlayClientPong(event);
            int id = pong.getId();

            handlePingCalculationPackets(event, playerData, id, playerData.transactionsSent);
        } else if (playerData.pingStrategy == PingStrategy.TRANSACTION && packetType == PacketType.Play.Client.WINDOW_CONFIRMATION) {
            WrapperPlayClientWindowConfirmation windowConfirmation = new WrapperPlayClientWindowConfirmation(event);
            int id = windowConfirmation.getActionId();

            handlePingCalculationPackets(event, playerData, id, playerData.transactionsSent);
        }
    }

    private <T extends Number> void handlePingCalculationPackets(PacketReceiveEvent event, PlayerData playerData, long id, Queue<Pair<T, Long>> packetSentList) {
//        System.out.println("Received response ID: " + id + " Queue size before: " + packetSentList.size());
//        System.out.println("Current queue contents: " + packetSentList.toString());

        if (playerData.didWeSendThatPacket(id)) {
            event.setCancelled(true);
        }

        if (!Base.INSTANCE.getConfigManager().isToggled()) return;

        Pair<T, Long> data = null;
        int cleared = 0;
        // Keep polling until we find the matching ID
        do {
            data = packetSentList.poll();
            cleared++;

            if (data == null) {
//                System.out.println("No data found in queue!");
                break;
            }

//            System.out.println("Cleared entry " + cleared + ": ID=" + data.getFirst() + " Time=" + data.getSecond());

            long pingNanos = (System.nanoTime() - data.getSecond());
            double diffMillisDouble = pingNanos / 1_000_000.0;

            playerData.setPreviousPing(playerData.getPing());
            playerData.setPing(diffMillisDouble);

            playerData.getJitterCalculator().addPing(pingNanos);
            double jitter = playerData.getJitterCalculator().calculateJitter();
            playerData.setJitter(jitter);

        } while (data.getFirst().longValue() != id);

//        System.out.println("Finished processing - Cleared " + cleared + " entries. Queue size after: " + packetSentList.size());
    }
}