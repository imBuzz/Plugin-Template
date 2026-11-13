package it.buzz.template.profile;

import it.buzz.template.storage.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/** JDBC adapter of {@link ProfileRepository}, portable across every {@link it.buzz.template.storage.dialect.SqlDialect}. */
public final class SqlProfileRepository implements ProfileRepository {

    private static final String[] COLUMNS = {"uuid", "name", "data", "created_at", "updated_at"};
    private static final String[] UPDATED = {"name", "data", "updated_at"};

    private final Database database;
    private final ProfileCodec codec;
    private final String selectByUuid;
    private final String selectByName;
    private final String upsert;

    public SqlProfileRepository(Database database, ProfileCodec codec) {
        this.database = database;
        this.codec = codec;
        String table = database.table("players");
        this.selectByUuid = "SELECT uuid, name, data FROM " + table + " WHERE uuid = ?";
        this.selectByName = "SELECT uuid, name, data FROM " + table + " WHERE LOWER(name) = LOWER(?) ORDER BY updated_at DESC LIMIT 1";
        this.upsert = database.dialect().upsert(table, "uuid", COLUMNS, UPDATED);
    }

    @Override
    public Optional<ProfileData> find(UUID uuid) {
        return query(selectByUuid, uuid.toString());
    }

    @Override
    public Optional<ProfileData> findByName(String name) {
        return query(selectByName, name);
    }

    @Override
    public LoadResult findOrCreate(UUID uuid, String name) {
        Optional<ProfileData> existing = find(uuid);
        if (existing.isPresent()) {
            return new LoadResult(existing.get(), false);
        }
        ProfileData created = new ProfileData(uuid, name);
        save(created.snapshot(codec));
        return new LoadResult(created, true);
    }

    @Override
    public void save(ProfileSnapshot snapshot) {
        try (Connection connection = database.connection();
             PreparedStatement ps = connection.prepareStatement(upsert)) {
            bind(ps, snapshot, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new StorageException("Could not save profile of " + snapshot.name(), e);
        }
    }

    @Override
    public void saveAll(Collection<ProfileSnapshot> snapshots) {
        if (snapshots.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        try (Connection connection = database.connection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (PreparedStatement ps = connection.prepareStatement(upsert)) {
                for (ProfileSnapshot snapshot : snapshots) {
                    bind(ps, snapshot, now);
                    ps.addBatch();
                }
                ps.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        } catch (SQLException e) {
            throw new StorageException("Could not save " + snapshots.size() + " profile(s)", e);
        }
    }

    private Optional<ProfileData> query(String sql, String param) {
        try (Connection connection = database.connection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(codec.decode(UUID.fromString(rs.getString("uuid")), rs.getString("name"), rs.getString("data")));
            }
        } catch (SQLException e) {
            throw new StorageException("Could not load profile " + param, e);
        }
    }

    private static void bind(PreparedStatement ps, ProfileSnapshot s, long now) throws SQLException {
        ps.setString(1, s.uuid().toString());
        ps.setString(2, s.name());
        ps.setString(3, s.json());
        ps.setLong(4, s.createdAt());
        ps.setLong(5, now);
    }
}
