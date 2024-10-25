package me.caseload.knockbacksync.listener.packetevents;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPong;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientWindowConfirmation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerKeepAlive;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPing;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowConfirmation;
import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.PlayerData;
import me.caseload.knockbacksync.util.data.Pair;

import java.util.List;

public class PingReceiveListener extends PacketListenerAbstract {
// TODO supplment our own ping calculations with over keepalive sent by the server with
    @Override
    public void onPacketSend(PacketSendEvent event) {
        PacketTypeCommon packetType = event.getPacketType();
        if (packetType.equals(PacketType.Play.Server.KEEP_ALIVE)) {
            WrapperPlayServerKeepAlive keepAlive = new WrapperPlayServerKeepAlive(event);
            long id = keepAlive.getId();

            PlayerData playerData = PlayerDataManager.getPlayerData(event.getUser().getUUID());
            if (playerData == null) return;

            playerData.keepaliveMap.add(new Pair<>(id, System.nanoTime()));
        } else if (packetType.equals(PacketType.Play.Server.PING)) {
            WrapperPlayServerPing ping = new WrapperPlayServerPing(event);
            int id = ping.getId();

            PlayerData playerData = PlayerDataManager.getPlayerData(event.getUser().getUUID());
            if (playerData == null) return;

            playerData.transactionsSent.add(new Pair<>(id, System.nanoTime()));
        } else if (packetType.equals(PacketType.Play.Server.WINDOW_CONFIRMATION)) {
            WrapperPlayServerWindowConfirmation confirmation = new WrapperPlayServerWindowConfirmation(event);
            int id = confirmation.getActionId();

            PlayerData playerData = PlayerDataManager.getPlayerData(event.getUser().getUUID());
            if (playerData == null) return;

            playerData.transactionsSent.add(new Pair<>(id, System.nanoTime()));
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (!Base.INSTANCE.getConfigManager().isToggled()) return;

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

    private <T extends Number> void handlePingCalculationPackets(PacketReceiveEvent event, PlayerData playerData, long id, List<Pair<T, Long>> packetSentList) {
        long pingNanos = (System.nanoTime() - packetSentList.remove(0).getSecond());
        double diffMillisDouble = pingNanos / 1_000_000.0;

        playerData.setPreviousPing(playerData.getPing());
        playerData.setPing(diffMillisDouble);

        playerData.getJitterCalculator().addPing(pingNanos);
        double jitter = playerData.getJitterCalculator().calculateJitter();
        playerData.setJitter(jitter);

        // We can cancel for all 3 cases
        if (playerData.didWeSendThatPacket(id)) {
            // Pong not needed as vanilla ignores the packet, its needed for packet limiters
            // We want to cancel Window Confirmations that we send, and ...
            // Minecraft kicks players that send invalid keepAliveID packets
            // Since Minecraft doesn't know we just sent a keepalive, we gotta cancel it
            // To stop MC from processing it and kicking the player
            event.setCancelled(true);
        }
    }
}