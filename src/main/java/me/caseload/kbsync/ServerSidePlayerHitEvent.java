package me.caseload.kbsync;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ServerSidePlayerHitEvent extends Event implements Cancellable {

    private boolean cancelled;
    private final Player attacker;
    private final Player victim;

    private static final HandlerList handlers = new HandlerList();

    public ServerSidePlayerHitEvent(Player attacker, Player victim) {
        this.attacker = attacker;
        this.victim = victim;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getAttacker() {
        return attacker;
    }

    public Player getVictim() {
        return victim;
    }
}
