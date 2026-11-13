package it.buzz.template.profile;

import it.buzz.template.storage.database.Database;
import it.buzz.template.storage.database.DatabaseSettings;
import it.buzz.template.storage.database.DatabaseType;
import it.buzz.template.storage.migration.Migrations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.EnumSource;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/** Runs every scenario against each embedded backend. */
@ParameterizedClass
@EnumSource(value = DatabaseType.class, names = {"SQLITE", "H2"})
class SqlProfileRepositoryTest {

    @Parameter
    DatabaseType type;

    @TempDir
    Path dir;

    private Database database;
    private SqlProfileRepository repository;
    private final ProfileCodec codec = new ProfileCodec();

    @BeforeEach
    void setUp() throws Exception {
        DatabaseSettings settings = type == DatabaseType.H2
                ? DatabaseSettings.h2(dir.resolve("test"), "t_", 4, false)
                : DatabaseSettings.sqlite(dir.resolve("test.db"), "t_");
        database = new Database(settings, "test");
        database.migrate(Migrations.ALL, Logger.getAnonymousLogger());
        repository = new SqlProfileRepository(database, codec);
    }

    @AfterEach
    void tearDown() {
        database.close();
    }

    @Test
    void createsOnFirstLoadAndReturnsSameRecordAfter() throws Exception {
        UUID id = UUID.randomUUID();
        ProfileRepository.LoadResult first = repository.findOrCreate(id, "Steve");
        assertTrue(first.created());
        ProfileData created = first.profile();
        assertEquals(0, created.joins());
        assertFalse(created.isDirty(), "freshly persisted data must not be dirty");

        created.recordJoin("Steve");
        repository.save(created.snapshot(codec));

        ProfileRepository.LoadResult second = repository.findOrCreate(id, "Steve");
        assertFalse(second.created());
        ProfileData reloaded = second.profile();
        assertEquals(1, reloaded.joins());
        assertEquals(created.firstSeen(), reloaded.firstSeen());
        assertEquals(id, reloaded.uuid());
    }

    @Test
    void findByNameIsCaseInsensitiveAndTracksRenames() throws Exception {
        UUID id = UUID.randomUUID();
        ProfileData data = repository.findOrCreate(id, "Alex").profile();
        data.recordJoin("AlexRenamed");
        repository.save(data.snapshot(codec));

        assertTrue(repository.findByName("alex").isEmpty());
        assertEquals(id, repository.findByName("alexrenamed").orElseThrow().uuid());
    }

    @Test
    void saveAllUpsertsInBatch() throws Exception {
        List<ProfileData> players = List.of(
                new ProfileData(UUID.randomUUID(), "A"),
                new ProfileData(UUID.randomUUID(), "B"),
                new ProfileData(UUID.randomUUID(), "C"));
        repository.saveAll(players.stream().map(p -> p.snapshot(codec)).toList());
        players.forEach(p -> p.recordJoin(p.name()));
        repository.saveAll(players.stream().map(p -> p.snapshot(codec)).toList());

        try (var c = database.connection(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM t_players")) {
            rs.next();
            assertEquals(3, rs.getInt(1));
        }
        for (ProfileData p : players) {
            assertEquals(1, repository.find(p.uuid()).orElseThrow().joins());
        }
    }

    @Test
    void unknownJsonFieldsAndMissingFieldsAreTolerated() {
        UUID id = UUID.randomUUID();
        ProfileData data = codec.decode(id, "Legacy", "{\"joins\":7,\"removedField\":true}");
        assertEquals(7, data.joins());
        assertEquals(0, data.lastSeen());
        assertEquals("Legacy", data.name());
    }

    @Test
    void fastutilCountersRoundTrip() throws Exception {
        UUID id = UUID.randomUUID();
        ProfileData data = repository.findOrCreate(id, "Counter").profile();
        data.increment("kills", 3);
        assertEquals(5, data.increment("kills", 2));
        data.increment("blocks", 1);
        repository.save(data.snapshot(codec));

        ProfileData reloaded = repository.find(id).orElseThrow();
        assertEquals(5, reloaded.counter("kills"));
        assertEquals(1, reloaded.counter("blocks"));
        assertEquals(0, reloaded.counter("missing"));
    }

    @Test
    void migrationsAreIdempotent() throws Exception {
        database.migrate(Migrations.ALL, Logger.getAnonymousLogger());
        try (var c = database.connection(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM t_schema_history")) {
            rs.next();
            assertEquals(Migrations.ALL.size(), rs.getInt(1));
        }
    }
}
