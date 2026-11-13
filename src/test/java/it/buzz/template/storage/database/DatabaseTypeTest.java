package it.buzz.template.storage.database;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseTypeTest {

    private static final String[] COLS = {"id", "a", "b"};
    private static final String[] UPD = {"a", "b"};

    @Test
    void sqliteUpsert() {
        assertEquals("INSERT INTO t (id, a, b) VALUES (?, ?, ?) ON CONFLICT(id) DO UPDATE SET a = excluded.a, b = excluded.b",
                DatabaseType.SQLITE.dialect().upsert("t", "id", COLS, UPD));
    }

    @Test
    void mysqlUpsert() {
        String expected = "INSERT INTO t (id, a, b) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE a = VALUES(a), b = VALUES(b)";
        assertEquals(expected, DatabaseType.MYSQL.dialect().upsert("t", "id", COLS, UPD));
        assertEquals(expected, DatabaseType.MARIADB.dialect().upsert("t", "id", COLS, UPD));
    }

    @Test
    void h2Upsert() {
        assertEquals("MERGE INTO t (id, a, b) KEY(id) VALUES (?, ?, ?)", DatabaseType.H2.dialect().upsert("t", "id", COLS, UPD));
    }

    @Test
    void h2UrlModesAreMutuallyExclusive() {
        String single = DatabaseSettings.h2(Path.of("data/db"), "p_", 4, false).jdbcUrl();
        String shared = DatabaseSettings.h2(Path.of("data/db"), "p_", 4, true).jdbcUrl();
        assertTrue(single.startsWith("jdbc:h2:file:/"), "H2 path must be absolute: " + single);
        assertTrue(single.contains("DB_CLOSE_ON_EXIT=FALSE") && !single.contains("AUTO_SERVER"));
        assertTrue(shared.contains("AUTO_SERVER=TRUE") && !shared.contains("DB_CLOSE_ON_EXIT"));
    }

    @Test
    void rejectsUnsafeTablePrefix() {
        assertThrows(IllegalArgumentException.class,
                () -> DatabaseSettings.sqlite(Path.of("x.db"), "x; DROP TABLE users; --"));
    }

    @Test
    void toStringHidesPassword() {
        var settings = new DatabaseSettings(DatabaseType.MARIADB, "db", 3306, "mc", "root", "s3cret", "p_", 4, 1, 1000, null, false, 0);
        assertEquals("jdbc:mariadb://db:3306/mc", settings.jdbcUrl());
        assertFalse(settings.toString().contains("s3cret"));
    }
}
