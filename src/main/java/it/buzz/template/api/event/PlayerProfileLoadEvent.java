package it.buzz.template.api.event;

import it.buzz.template.api.player.PlayerProfile;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired <b>asynchronously</b> while the player is logging in, right after their profile has been loaded
 * (or created on first join). Do not touch the world or the player entity here: it does not exist yet.
 */
public final class PlayerProfileLoadEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final PlayerProfile profile;
    private final boolean created;

    public PlayerProfileLoadEvent(PlayerProfile profile, boolean created) {
        super(true);
        this.profile = profile;
        this.created = created;
    }

    public PlayerProfile profile() {
        return profile;
    }

    /** {@code true} on the very first join of this player. */
    public boolean created() {
        return created;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
