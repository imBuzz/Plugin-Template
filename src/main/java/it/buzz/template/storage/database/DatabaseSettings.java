package it.buzz.template.storage.database;

import it.buzz.template.core.config.ConfigService;
import it.buzz.template.core.config.PluginConfig;
import it.buzz.template.storage.dialect.SqlDialect;

import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Immutable snapshot of the database configuration, decoupled from Bukkit so it can be unit tested.
 *
 * @param file           base path of the local database (SQLITE and H2 only)
 * @param h2AutoServer   H2 only: let several processes open the same file at once (see {@link #jdbcUrl()})
 * @param h2AutoServerPort H2 only: fixed TCP port for the auto server, 0 = pick a free one
 */
public record DatabaseSettings(
        DatabaseType type,
        String host,
        int port,
        String database,
        String user,
        String password,
        String tablePrefix,
        int poolSize,
        int minIdle,
        long connectionTimeoutMs,
        Path file,
        boolean h2AutoServer,
        int h2AutoServerPort
) {

    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("[A-Za-z0-9_]*");

    public DatabaseSettings {
        if (!SAFE_IDENTIFIER.matcher(tablePrefix).matches()) {
            throw new IllegalArgumentException("database.table-prefix may only contain letters, digits and '_': " + tablePrefix);
        }
        if (poolSize < 1) {
            throw new IllegalArgumentException("database.pool.maximum-size must be >= 1");
        }
        if (h2AutoServerPort < 0 || h2AutoServerPort > 65535) {
            throw new IllegalArgumentException("database.h2.auto-server-port must be between 0 and 65535");
        }
        if (file != null) {
            // H2 2.x rejects relative paths, and a relative SQLite path would depend on the server's working dir.
            file = file.toAbsolutePath().normalize();
        }
    }

    public static DatabaseSettings from(ConfigService config, Path dataFolder) {
        DatabaseType type = config.get(PluginConfig.DB_TYPE);
        return new DatabaseSettings(
                type,
                config.get(PluginConfig.DB_HOST),
                config.get(PluginConfig.DB_PORT),
                config.get(PluginConfig.DB_NAME),
                config.get(PluginConfig.DB_USER),
                config.get(PluginConfig.DB_PASSWORD),
                config.get(PluginConfig.DB_TABLE_PREFIX),
                config.get(PluginConfig.DB_POOL_SIZE),
                config.get(PluginConfig.DB_POOL_MIN_IDLE),
                config.get(PluginConfig.DB_POOL_TIMEOUT_MS),
                type.dialect().fileBased() ? dataFolder.resolve(type.dialect().defaultFileName()) : null,
                config.get(PluginConfig.DB_H2_AUTO_SERVER),
                config.get(PluginConfig.DB_H2_AUTO_SERVER_PORT));
    }

    public static DatabaseSettings sqlite(Path file, String tablePrefix) {
        return new DatabaseSettings(DatabaseType.SQLITE, "", 0, "", "", "", tablePrefix, 1, 1, 5000, file, false, 0);
    }

    public static DatabaseSettings h2(Path file, String tablePrefix, int poolSize, boolean autoServer) {
        return new DatabaseSettings(DatabaseType.H2, "", 0, "", "sa", "", tablePrefix, poolSize, 1, 10_000, file, autoServer, 0);
    }

    public SqlDialect dialect() {
        return type.dialect();
    }

    public String jdbcUrl() {
        return type.dialect().jdbcUrl(this);
    }

    public String table(String name) {
        return tablePrefix + name;
    }

    @Override
    public String toString() {
        // Never leak the password in logs.
        if (type.dialect().fileBased()) {
            return "DatabaseSettings[type=%s, file=%s, prefix=%s%s]".formatted(type, file, tablePrefix,
                    type == DatabaseType.H2 ? ", pool=" + poolSize + ", auto-server=" + h2AutoServer : "");
        }
        return "DatabaseSettings[type=%s, url=%s, user=%s, prefix=%s, pool=%d]"
                .formatted(type, jdbcUrl(), user, tablePrefix, poolSize);
    }
}
