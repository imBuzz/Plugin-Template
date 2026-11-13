package it.buzz.template.profile;

import java.util.UUID;

/**
 * Immutable, thread-safe copy of a {@link ProfileData} taken on the server thread and handed to the storage thread.
 *
 * @param json      serialized payload produced by {@link ProfileCodec}
 * @param createdAt epoch millis of the first join, stored in its own column for querying
 */
public record ProfileSnapshot(UUID uuid, String name, String json, long createdAt) {
}
