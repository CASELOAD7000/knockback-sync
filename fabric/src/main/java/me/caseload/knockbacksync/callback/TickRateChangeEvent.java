package me.caseload.knockbacksync.callback;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface TickRateChangeEvent {
    Event<TickRateChangeEvent> EVENT = EventFactory.createArrayBacked(TickRateChangeEvent.class,
            (listeners) -> (oldTickRate, newTickRate) -> {
                for (TickRateChangeEvent listener : listeners) {
                    listener.onTickRateChange(oldTickRate, newTickRate);
                }
            });

    void onTickRateChange(float oldTickRate, float newTickRate);
}
