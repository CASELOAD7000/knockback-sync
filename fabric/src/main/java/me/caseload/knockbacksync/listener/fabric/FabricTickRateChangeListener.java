package me.caseload.knockbacksync.listener.fabric;

import me.caseload.knockbacksync.Base;
import me.caseload.knockbacksync.FabricBase;
import me.caseload.knockbacksync.callback.TickRateChangeEvent;

public class FabricTickRateChangeListener {
    public void register() {
        TickRateChangeEvent.EVENT.register((oldTickRate, newTickRate) -> {
            if (oldTickRate != newTickRate) {
                ((FabricBase) Base.INSTANCE).setTickRate(newTickRate);
            }
        });
    }
}
