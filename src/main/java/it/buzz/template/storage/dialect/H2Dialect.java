package it.buzz.template.storage.dialect;

import com.zaxxer.hikari.HikariConfig;
import it.buzz.template.storage.database.DatabaseSettings;

/**
 * Local file with real concurrent writes (MVStore, MVCC row-level locks), optionally shared by several processes.
 * Driver downloaded through plugin.yml {@code libraries}.
 * <p>
 * The URL fixes the classic embedded-H2 problems:
 * <ul>
 *     <li>Absolute path (normalized by {@link DatabaseSettings}): H2 2.x refuses implicit relative paths.</li>
 *     <li>{@code LOCK_TIMEOUT=10000}: concurrent writers on the same row wait up to 10 s instead of the 1 s default,
 *     avoiding spurious "Timeout trying to lock table" under load.</li>
 *     <li>{@code MAX_COMPACT_TIME=2000}: compacts the file for up to 2 s on close, so the {@code .mv.db} does not grow forever.</li>
 *     <li><b>Single process</b> ({@code auto-server: false}): {@code DB_CLOSE_ON_EXIT=FALSE}. H2's JVM shutdown hook would
 *     otherwise close the database <i>while</i> Paper is still disabling plugins on Ctrl+C / SIGTERM, making the final
 *     flush fail with "Database is already closed". The pool is closed explicitly in {@code onDisable} instead.</li>
 *     <li><b>Shared file</b> ({@code auto-server: true}): {@code AUTO_SERVER=TRUE}. The first process to open the file
 *     becomes its owner and serves it over localhost; every other process connects through it transparently instead of
 *     failing with "Database may be already in use", and takes over if the owner stops. H2 forbids combining this with
 *     {@code DB_CLOSE_ON_EXIT=FALSE}, so on a hard kill the last unsaved changes (at most one autosave interval) can be
 *     lost; a normal {@code /stop} is always safe.</li>
 * </ul>
 */
public final class H2Dialect implements SqlDialect {

    @Override
    public String driverClass() {
        return "org.h2.Driver";
    }

    @Override
    public String jdbcUrl(DatabaseSettings settings) {
        StringBuilder url = new StringBuilder("jdbc:h2:file:").append(settings.file())
                .append(";LOCK_TIMEOUT=10000")
                .append(";MAX_COMPACT_TIME=2000");
        if (settings.h2AutoServer()) {
            url.append(";AUTO_SERVER=TRUE");
            if (settings.h2AutoServerPort() > 0) {
                url.append(";AUTO_SERVER_PORT=").append(settings.h2AutoServerPort());
            }
        } else {
            url.append(";DB_CLOSE_ON_EXIT=FALSE");
        }
        return url.toString();
    }

    @Override
    public void configure(HikariConfig hikari, DatabaseSettings settings) {
        hikari.setUsername(settings.user());
        hikari.setPassword(settings.password());
        hikari.setMaximumPoolSize(settings.poolSize());
        hikari.setMinimumIdle(Math.min(settings.minIdle(), settings.poolSize()));
        // In auto-server mode the owning process may change: validate connections before handing them out.
        hikari.setConnectionTestQuery("SELECT 1");
    }

    @Override
    public boolean fileBased() {
        return true;
    }

    @Override
    public String defaultFileName() {
        return "database"; // H2 appends ".mv.db"
    }

    @Override
    public String upsert(String table, String key, String[] columns, String[] updated) {
        // Native atomic upsert, no MySQL compatibility mode needed.
        return "MERGE INTO " + table + " (" + String.join(", ", columns) + ") KEY(" + key + ") VALUES ("
                + SqlDialect.placeholders(columns.length) + ")";
    }
}
