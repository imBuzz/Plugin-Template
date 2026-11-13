package it.buzz.template.core.config;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
import ch.jalu.configme.properties.Property;
import it.buzz.template.storage.database.DatabaseType;

import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

/** Schema of {@code config.yml}. Missing keys are added automatically with their defaults. */
public final class PluginConfig implements SettingsHolder {

    @Comment("Prints extra diagnostic information to the console.")
    public static final Property<Boolean> DEBUG = newProperty("debug", false);

    @Comment({"Storage backend:",
            "  SQLITE  - local file, zero setup, one writer at a time. Good for small servers.",
            "  H2      - local file, concurrent writers, can be shared by several servers on the same machine.",
            "  MYSQL / MARIADB - remote database, shared by servers on any machine."})
    public static final Property<DatabaseType> DB_TYPE = newProperty(DatabaseType.class, "database.type", DatabaseType.SQLITE);
    public static final Property<String> DB_HOST = newProperty("database.host", "localhost");
    public static final Property<Integer> DB_PORT = newProperty("database.port", 3306);
    public static final Property<String> DB_NAME = newProperty("database.name", "minecraft");
    public static final Property<String> DB_USER = newProperty("database.user", "root");
    public static final Property<String> DB_PASSWORD = newProperty("database.password", "");
    @Comment({"H2 only. When true, several processes (e.g. two servers on the same machine, or the H2 console)",
            "can open and write the same database file at the same time: the first one serves the others over localhost.",
            "Leave false for a single server: shutdown is then guaranteed to flush everything even on Ctrl+C."})
    public static final Property<Boolean> DB_H2_AUTO_SERVER = newProperty("database.h2.auto-server", false);
    @Comment("H2 only. TCP port used by the auto server; 0 picks a free port automatically.")
    public static final Property<Integer> DB_H2_AUTO_SERVER_PORT = newProperty("database.h2.auto-server-port", 0);
    @Comment("Prefix for every table created by this plugin. Letters, digits and underscores only.")
    public static final Property<String> DB_TABLE_PREFIX = newProperty("database.table-prefix", "template_");
    @Comment("Maximum number of pooled connections (MYSQL, MARIADB, H2). SQLITE always uses 1.")
    public static final Property<Integer> DB_POOL_SIZE = newProperty("database.pool.maximum-size", 10);
    public static final Property<Integer> DB_POOL_MIN_IDLE = newProperty("database.pool.minimum-idle", 2);
    public static final Property<Integer> DB_POOL_TIMEOUT_MS = newProperty("database.pool.connection-timeout-ms", 5000);

    @Comment("How often (in seconds) modified player data is flushed to the database. 0 disables autosave.")
    public static final Property<Integer> AUTOSAVE_SECONDS = newProperty("player-data.autosave-interval-seconds", 300);

    private PluginConfig() {
    }

    @Override
    public void registerComments(CommentsConfiguration conf) {
        conf.setComment("database", "Database connection settings. Changes require a server restart.");
    }
}
