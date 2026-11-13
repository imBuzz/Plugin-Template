package it.buzz.template.api.event;

import it.buzz.template.api.player.PlayerProfile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired on the server thread when a player quits, just <b>before</b> their profile is saved and evicted.
 * Last chance to modify it: changes made here are persisted.
 */
public final class PlayerProfileUnloadEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final PlayerProfile profile;

    public PlayerProfileUnloadEvent(PlayerProfile profile) {
        this.profile = profile;
    }

    public PlayerProfile profile() {
        return profile;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
