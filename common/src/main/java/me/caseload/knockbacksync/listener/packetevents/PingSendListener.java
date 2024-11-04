package me.caseload.knockbacksync.listener.packetevents;

import com.github.retrooper.packetevents.event.*;
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

import java.util.Queue;

public class PingSendListener extends PacketListenerAbstract {

    public PingSendListener() {
        // See all actually outgoing, not cancelled, ping packets
        super(PacketListenerPriority.LOWEST);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (!Base.INSTANCE.getConfigManager().isToggled()) return;

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
}