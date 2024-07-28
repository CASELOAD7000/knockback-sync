package me.caseload.kbsync.listener;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;

public class PacketEventsPacketListener extends PacketListenerAbstract {

    public PacketEventsPacketListener() {
        super(PacketListenerPriority.NORMAL);
    }

    @Override
    public void onPacketReceive(com.github.retrooper.packetevents.event.PacketReceiveEvent event) {
        // Manejar la recepción de paquetes aquí
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        //Manejar el envío de paquetes aquí
    }
}
