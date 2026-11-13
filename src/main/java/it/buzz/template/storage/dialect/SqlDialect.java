package it.buzz.template.storage.dialect;

import com.zaxxer.hikari.HikariConfig;
import it.buzz.template.storage.database.DatabaseSettings;

import java.util.Collections;

/**
 * Everything that differs between database engines: driver, JDBC URL, pool tuning and non-standard SQL.
 * Adding an engine means implementing this interface and adding a constant to
 * {@link it.buzz.template.storage.database.DatabaseType}; no other class changes.
 */
public interface SqlDialect {

    String driverClass();

    String jdbcUrl(DatabaseSettings settings);

    /** Applies engine-specific pool settings and driver properties. */
    void configure(HikariConfig hikari, DatabaseSettings settings);

    /** {@code true} when the database lives in a local file next to the plugin. */
    default boolean fileBased() {
        return false;
    }

    /** File name (inside the plugin folder) for file based engines. */
    default String defaultFileName() {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " is not file based");
    }

    /**
     * Atomic "insert or update" keyed on {@code key}.
     *
     * @param table   fully qualified table name
     * @param key     primary key column
     * @param columns all columns to insert, including {@code key}
     * @param updated columns overwritten when the row already exists
     */
    String upsert(String table, String key, String[] columns, String[] updated);

    /** Comma separated list of {@code n} JDBC placeholders. */
    static String placeholders(int n) {
        return String.join(", ", Collections.nCopies(n, "?"));
    }
}
