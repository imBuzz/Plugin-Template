package it.buzz.template.storage.dialect;

import com.zaxxer.hikari.HikariConfig;
import it.buzz.template.storage.database.DatabaseSettings;

import java.util.Arrays;
import java.util.stream.Collectors;

/** Local single-writer file. Driver bundled with Paper. */
public final class SqliteDialect implements SqlDialect {

    @Override
    public String driverClass() {
        return "org.sqlite.JDBC";
    }

    @Override
    public String jdbcUrl(DatabaseSettings settings) {
        return "jdbc:sqlite:" + settings.file();
    }

    @Override
    public void configure(HikariConfig hikari, DatabaseSettings settings) {
        // SQLite allows a single writer: one connection avoids SQLITE_BUSY entirely.
        hikari.setMaximumPoolSize(1);
        hikari.addDataSourceProperty("journal_mode", "WAL");
        hikari.addDataSourceProperty("synchronous", "NORMAL");
        hikari.addDataSourceProperty("foreign_keys", "true");
        hikari.addDataSourceProperty("busy_timeout", "5000");
    }

    @Override
    public boolean fileBased() {
        return true;
    }

    @Override
    public String defaultFileName() {
        return "database.db";
    }

    @Override
    public String upsert(String table, String key, String[] columns, String[] updated) {
        return "INSERT INTO " + table + " (" + String.join(", ", columns) + ") VALUES (" + SqlDialect.placeholders(columns.length) + ")"
                + " ON CONFLICT(" + key + ") DO UPDATE SET "
                + Arrays.stream(updated).map(c -> c + " = excluded." + c).collect(Collectors.joining(", "));
    }
}
