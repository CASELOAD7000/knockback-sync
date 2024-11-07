package me.caseload.knockbacksync.listener.packetevents;

import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.PlayerData;
import me.caseload.knockbacksync.util.data.Pair;

import java.util.Queue;

public class PingReceiveListener extends PacketListenerAbstract {

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        // Do not immediately return if KB sync is disabled. This is because if we send a packet, disable the plugin
        // And then receive a response we have to ensure that if the packet was sent by us we cancel it
        // otherwise the server will likely kick the player
        PacketTypeCommon packetType = event.getPacketType();
        if (packetType == PacketType.Play.Client.KEEP_ALIVE) {

            WrapperPlayClientKeepAlive keepAlive = new WrapperPlayClientKeepAlive(event);
            PlayerData playerData = PlayerDataManager.getPlayerData(event.getUser().getUUID());
            if (playerData == null) return;

            long receivedId = keepAlive.getId();

            handlePingCalculationPackets(event, playerData, receivedId, playerData.keepaliveMap);
        } else if (packetType == PacketType.Play.Client.PONG) {
            WrapperPlayClientPong pong = new WrapperPlayClientPong(event);
            PlayerData playerData = PlayerDataManager.getPlayerData(event.getUser().getUUID());
            if (playerData == null) return;

            int id = pong.getId();

            handlePingCalculationPackets(event, playerData, id, playerData.transactionsSent);
        } else if (packetType == PacketType.Play.Client.WINDOW_CONFIRMATION) {
            WrapperPlayClientWindowConfirmation windowConfirmation = new WrapperPlayClientWindowConfirmation(event);
            PlayerData playerData = PlayerDataManager.getPlayerData(event.getUser().getUUID());
            if (playerData == null) return;

            int id = windowConfirmation.getActionId();

            handlePingCalculationPackets(event, playerData, id, playerData.transactionsSent);
        }
    }

    private <T extends Number> void handlePingCalculationPackets(PacketReceiveEvent event, PlayerData playerData, long id, Queue<Pair<T, Long>> packetSentList) {
        // We can cancel for all 3 cases
        if (playerData.didWeSendThatPacket(id)) {
            // Pong not needed as vanilla ignores the packet, its needed for packet limiters
            // We want to cancel Window Confirmations that we send, and ...
            // Minecraft kicks players that send invalid keepAliveID packets
            // Since Minecraft doesn't know we just sent a keepalive, we gotta cancel it
            // To stop MC from processing it and kicking the player
            event.setCancelled(true);
        }

        if (!Base.INSTANCE.getConfigManager().isToggled()) return;

        Pair<T, Long> longPair = packetSentList.poll();
        if (longPair == null) {
            throw new IllegalStateException("packetSentList was empty. Knockbacksync should continue to function but ping measurements may be inaccurate due to conflicts with other plugins.");
        }
        long pingNanos = (System.nanoTime() - longPair.getSecond());
        double diffMillisDouble = pingNanos / 1_000_000.0;

        playerData.setPreviousPing(playerData.getPing());
        playerData.setPing(diffMillisDouble);

        playerData.getJitterCalculator().addPing(pingNanos);
        double jitter = playerData.getJitterCalculator().calculateJitter();
        playerData.setJitter(jitter);
    }
}