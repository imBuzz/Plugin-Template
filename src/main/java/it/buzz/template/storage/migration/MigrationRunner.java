package it.buzz.template.storage.migration;

import it.buzz.template.storage.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/** Applies pending {@link Migration}s in order and records them in a {@code schema_history} table. */
public final class MigrationRunner {

    private final Database database;
    private final Logger logger;

    public MigrationRunner(Database database, Logger logger) {
        this.database = database;
        this.logger = logger;
    }

    public void run(List<Migration> migrations) throws SQLException {
        String history = database.table("schema_history");
        try (Connection connection = database.connection()) {
            try (Statement st = connection.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS " + history
                        + " (version INT PRIMARY KEY, description VARCHAR(255) NOT NULL, applied_at BIGINT NOT NULL)");
            }

            int current = currentVersion(connection, history);
            List<Migration> pending = migrations.stream()
                    .filter(m -> m.version() > current)
                    .sorted(Comparator.comparingInt(Migration::version))
                    .toList();

            for (Migration migration : pending) {
                logger.info("Applying database migration v%d: %s".formatted(migration.version(), migration.description()));
                boolean autoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);
                try {
                    migration.action().apply(connection, database);
                    try (PreparedStatement ps = connection.prepareStatement(
                            "INSERT INTO " + history + " (version, description, applied_at) VALUES (?, ?, ?)")) {
                        ps.setInt(1, migration.version());
                        ps.setString(2, migration.description());
                        ps.setLong(3, System.currentTimeMillis());
                        ps.executeUpdate();
                    }
                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    throw new SQLException("Migration v" + migration.version() + " failed", e);
                } finally {
                    connection.setAutoCommit(autoCommit);
                }
            }
        }
    }

    private static int currentVersion(Connection connection, String history) throws SQLException {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT MAX(version) FROM " + history)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
