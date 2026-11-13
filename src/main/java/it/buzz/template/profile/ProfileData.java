package it.buzz.template.profile;

import it.buzz.template.api.player.PlayerProfile;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.Objects;
import java.util.UUID;

/**
 * Implementation of {@link PlayerProfile}, persisted as JSON in the {@code data} column.
 * <p>
 * <b>Adding a field:</b> declare it with a default value and mutate it through a method that calls
 * {@link #markDirty()}. Rows written before the field existed keep the default: no migration needed.
 * {@code transient} fields are not serialized.
 * <p>
 * <b>Threading:</b> mutate only from the server thread. Persistence works on immutable {@link ProfileSnapshot}s.
 */
public final class ProfileData implements PlayerProfile {

    private transient UUID uuid;
    private transient String name;
    private transient volatile boolean dirty;

    // ---- Persisted fields (example data; replace with your own) ----
    private int joins;
    private long firstSeen;
    private long lastSeen;
    /**
     * Named counters (kills, blocks broken, ...). fastutil stores {@code int} values without boxing, and
     * {@link Object2IntOpenHashMap#addTo} increments with a single hash lookup.
     */
    private Object2IntOpenHashMap<String> counters = new Object2IntOpenHashMap<>();

    /** Used by Gson; keeps field initializers working. */
    @SuppressWarnings("unused")
    private ProfileData() {
    }

    ProfileData(UUID uuid, String name) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
        this.name = Objects.requireNonNull(name, "name");
        this.firstSeen = System.currentTimeMillis();
        this.dirty = true;
    }

    /** Restores the column-backed identity after deserialization. */
    ProfileData bind(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        return this;
    }

    // ---- PlayerProfile ----

    @Override
    public UUID uuid() {
        return uuid;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int joins() {
        return joins;
    }

    @Override
    public long firstSeen() {
        return firstSeen;
    }

    @Override
    public long lastSeen() {
        return lastSeen;
    }

    @Override
    public int counter(String key) {
        return counters.getInt(key);
    }

    @Override
    public Object2IntMap<String> counters() {
        return Object2IntMaps.unmodifiable(counters);
    }

    @Override
    public int increment(String key, int amount) {
        int updated = counters.addTo(key, amount) + amount;
        markDirty();
        return updated;
    }

    // ---- Lifecycle hooks (package-private: only ProfileService drives them) ----

    void recordJoin(String currentName) {
        this.name = currentName;
        this.joins++;
        this.lastSeen = System.currentTimeMillis();
        markDirty();
    }

    void recordQuit() {
        this.lastSeen = System.currentTimeMillis();
        markDirty();
    }

    // ---- Persistence plumbing ----

    public void markDirty() {
        this.dirty = true;
    }

    boolean isDirty() {
        return dirty;
    }

    /** Captures the current state and clears the dirty flag. Server thread only. */
    ProfileSnapshot snapshot(ProfileCodec codec) {
        dirty = false;
        return new ProfileSnapshot(uuid, name, codec.encode(this), firstSeen);
    }
}
