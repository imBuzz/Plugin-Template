package it.buzz.template.storage.dialect;

import com.zaxxer.hikari.HikariConfig;
import it.buzz.template.storage.database.DatabaseSettings;

import java.util.Arrays;
import java.util.stream.Collectors;

/** Remote MySQL. Driver bundled with Paper. */
public class MySqlDialect implements SqlDialect {

    @Override
    public String driverClass() {
        return "com.mysql.cj.jdbc.Driver";
    }

    protected String scheme() {
        return "mysql";
    }

    @Override
    public String jdbcUrl(DatabaseSettings settings) {
        return "jdbc:%s://%s:%d/%s".formatted(scheme(), settings.host(), settings.port(), settings.database());
    }

    @Override
    public void configure(HikariConfig hikari, DatabaseSettings settings) {
        hikari.setUsername(settings.user());
        hikari.setPassword(settings.password());
        hikari.setMaximumPoolSize(settings.poolSize());
        hikari.setMinimumIdle(Math.min(settings.minIdle(), settings.poolSize()));
        hikari.setMaxLifetime(1_800_000);
        hikari.setKeepaliveTime(60_000);
        hikari.addDataSourceProperty("cachePrepStmts", "true");
        hikari.addDataSourceProperty("prepStmtCacheSize", "250");
        hikari.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikari.addDataSourceProperty("useServerPrepStmts", "true");
        hikari.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikari.addDataSourceProperty("characterEncoding", "utf8");
    }

    @Override
    public String upsert(String table, String key, String[] columns, String[] updated) {
        // VALUES() is understood by both MySQL 8 and MariaDB (MariaDB lacks the newer row-alias syntax).
        return "INSERT INTO " + table + " (" + String.join(", ", columns) + ") VALUES (" + SqlDialect.placeholders(columns.length) + ")"
                + " ON DUPLICATE KEY UPDATE "
                + Arrays.stream(updated).map(c -> c + " = VALUES(" + c + ")").collect(Collectors.joining(", "));
    }
}
