package it.buzz.template.api.player;

import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Access to {@link PlayerProfile}s. */
public interface PlayerProfileService {

    /**
     * Profile of an online player. Profiles are loaded before the player joins, so this is always present
     * between {@code PlayerJoinEvent} and {@code PlayerQuitEvent}.
     */
    Optional<PlayerProfile> get(UUID uuid);

    /**
     * Profile of an online player.
     *
     * @throws IllegalStateException if the player is not online
     */
    PlayerProfile get(Player player);

    /**
     * Loads the profile of any player by name (case-insensitive), online or offline.
     * Results are cached briefly. The future completes off the server thread.
     */
    CompletableFuture<Optional<PlayerProfile>> lookup(String name);

    /** Number of profiles currently held in memory. */
    int loadedCount();
}
