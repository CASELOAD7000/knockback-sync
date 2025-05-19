package me.caseload.knockbacksync.listener.packetevents;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.manager.PlayerDataManager;
import me.caseload.knockbacksync.player.PlayerData;

public class ClientBrandListener extends PacketListenerAbstract {
    String brand = "vanilla";
    boolean hasBrand = false;

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLUGIN_MESSAGE) {
            WrapperPlayClientPluginMessage packet = new WrapperPlayClientPluginMessage(event);
            String channelName = packet.getChannelName();
            handle(channelName, packet.getData());
            PlayerData playerData = PlayerDataManager.getPlayerData(event.getUser());
            if (playerData == null) return;
            playerData.getPlatformPlayer().setClientBrand(brand);
        }
    }

    public void handle(String channel, byte[] data) {
        if (channel.equalsIgnoreCase("minecraft:brand") || // 1.13+
                channel.equals("MC|Brand")) { // 1.12
            if (data.length > 64 || data.length == 0) {
                brand = "sent " + data.length + " bytes as brand";
            } else if (!hasBrand) {
                byte[] minusLength = new byte[data.length - 1];
                System.arraycopy(data, 1, minusLength, 0, minusLength.length);
                brand = new String(minusLength).replace(" (Velocity)", ""); //removes velocity's brand suffix
            }
            hasBrand = true;
        }
    }
}

