package it.buzz.template.api.player;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

import java.util.UUID;

/**
 * Persistent data of one player.
 * <p>
 * Instances obtained for <b>online</b> players are live: changes are saved automatically (on quit, on autosave and on
 * shutdown). Instances obtained through {@link PlayerProfileService#lookup(String)} for <b>offline</b> players are
 * read-only snapshots. All mutators must be called from the server thread.
 */
public interface PlayerProfile {

    UUID uuid();

    /** Last known name. */
    String name();

    int joins();

    /** Epoch millis of the first join. */
    long firstSeen();

    /** Epoch millis of the last join or quit. */
    long lastSeen();

    /** Value of a named counter, {@code 0} when never incremented. */
    int counter(String key);

    /** Read-only view of every counter. */
    Object2IntMap<String> counters();

    /**
     * Adds {@code amount} (may be negative) to a named counter.
     *
     * @return the updated value
     */
    int increment(String key, int amount);
}
