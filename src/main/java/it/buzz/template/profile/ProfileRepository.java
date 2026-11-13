package it.buzz.template.profile;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Storage port for profiles. {@link SqlProfileRepository} is the default adapter; a Redis, Mongo or in-memory
 * implementation can be swapped in without touching {@link ProfileService}.
 * <p>
 * Every method blocks and must be called off the server thread.
 */
public interface ProfileRepository {

    Optional<ProfileData> find(UUID uuid) throws StorageException;

    /** Case-insensitive; when a name was used by several accounts, returns the most recently updated one. */
    Optional<ProfileData> findByName(String name) throws StorageException;

    /** Loads the profile, creating and persisting a fresh one on first join. */
    LoadResult findOrCreate(UUID uuid, String name) throws StorageException;

    void save(ProfileSnapshot snapshot) throws StorageException;

    /** Saves many profiles atomically (single transaction / batch where supported). */
    void saveAll(Collection<ProfileSnapshot> snapshots) throws StorageException;

    /**
     * @param created {@code true} if the profile did not exist and was just created
     */
    record LoadResult(ProfileData profile, boolean created) {
    }

    /** Unchecked wrapper so callers are not tied to JDBC's {@code SQLException}. */
    final class StorageException extends RuntimeException {
        public StorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
