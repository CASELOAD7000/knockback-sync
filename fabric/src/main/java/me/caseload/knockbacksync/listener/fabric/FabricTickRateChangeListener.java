package me.caseload.knockbacksync.listener.fabric;

import me.caseload.knockbacksync.callback.TickRateChangeEvent;
import me.caseload.knockbacksync.player.PlayerData;

public class FabricTickRateChangeListener {
    public void register() {
        TickRateChangeEvent.EVENT.register((oldTickRate, newTickRate) -> {
            if (oldTickRate != newTickRate) {
                PlayerData.TICK_RATE = newTickRate;
            }
        });
    }
}
