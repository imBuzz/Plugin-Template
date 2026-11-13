package it.buzz.template.storage.database;

import it.buzz.template.core.config.ConfigService;
import it.buzz.template.storage.migration.Migrations;
import it.buzz.template.core.lifecycle.Service;
import org.bukkit.plugin.Plugin;

/** Bukkit-facing wrapper that opens the {@link Database} from {@code config.yml} and runs migrations. */
public final class DatabaseService implements Service {

    private final Plugin plugin;
    private final ConfigService config;
    private Database database;

    public DatabaseService(Plugin plugin, ConfigService config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void enable() throws Exception {
        DatabaseSettings settings = DatabaseSettings.from(config, plugin.getDataFolder().toPath());
        plugin.getLogger().info("Connecting to " + settings);
        database = new Database(settings, plugin.getName() + "-pool");
        database.migrate(Migrations.ALL, plugin.getLogger());
    }

    @Override
    public void disable() {
        if (database != null) {
            database.close();
        }
    }

    public Database database() {
        return database;
    }
}
