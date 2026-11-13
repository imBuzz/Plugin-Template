package it.buzz.template.storage.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import it.buzz.template.storage.dialect.SqlDialect;
import it.buzz.template.storage.migration.Migration;
import it.buzz.template.storage.migration.MigrationRunner;

import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Connection pool + schema migrations. Pure JDBC, no Bukkit dependency, engine details delegated to a {@link SqlDialect}.
 * <p>
 * All methods block: call them from {@link it.buzz.template.core.concurrent.TaskExecutor#async()}, never the server thread.
 */
public final class Database implements AutoCloseable {

    private final DatabaseSettings settings;
    private final HikariDataSource dataSource;

    public Database(DatabaseSettings settings, String poolName) throws Exception {
        this.settings = settings;
        SqlDialect dialect = settings.dialect();

        if (dialect.fileBased()) {
            Files.createDirectories(settings.file().getParent());
        }

        HikariConfig hikari = new HikariConfig();
        hikari.setPoolName(poolName);
        hikari.setDriverClassName(dialect.driverClass());
        hikari.setJdbcUrl(dialect.jdbcUrl(settings));
        hikari.setConnectionTimeout(settings.connectionTimeoutMs());
        dialect.configure(hikari, settings);

        // Fails fast with a clear message if the database is unreachable or misconfigured.
        this.dataSource = new HikariDataSource(hikari);
    }

    public void migrate(List<Migration> migrations, Logger logger) throws SQLException {
        new MigrationRunner(this, logger).run(migrations);
    }

    public Connection connection() throws SQLException {
        return dataSource.getConnection();
    }

    public DatabaseSettings settings() {
        return settings;
    }

    public SqlDialect dialect() {
        return settings.dialect();
    }

    public String table(String name) {
        return settings.table(name);
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
