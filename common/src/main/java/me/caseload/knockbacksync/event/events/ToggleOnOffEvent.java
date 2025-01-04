package me.caseload.knockbacksync.event.events;

import me.caseload.knockbacksync.event.Event;

public class ToggleOnOffEvent extends Event {

    private boolean newEnabledState;

    public ToggleOnOffEvent(boolean newEnabledState) {
        this.newEnabledState = newEnabledState;
    }

    public boolean getStatus() {
        return newEnabledState;
    }

    public void setStatus(boolean newEnabledState) {
        this.newEnabledState = newEnabledState;
    }
    @Override
    public void setCancelled(boolean cancelled) {
        super.setCancelled(cancelled);
    }
}
