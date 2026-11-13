package it.buzz.template;

import it.buzz.template.command.CommandErrorHandler;
import it.buzz.template.command.CommandService;
import it.buzz.template.command.TemplateCommand;
import it.buzz.template.core.api.ApiService;
import it.buzz.template.core.concurrent.TaskExecutor;
import it.buzz.template.core.config.ConfigService;
import it.buzz.template.core.config.PluginConfig;
import it.buzz.template.core.lifecycle.ServiceRegistry;
import it.buzz.template.core.message.MessageService;
import it.buzz.template.profile.ProfileService;
import it.buzz.template.storage.database.DatabaseService;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Composition root. Builds every service, wires dependencies through constructors and hands them to the
 * {@link ServiceRegistry}, which enables them in this order and disables them in reverse.
 * <p>
 * No static singletons: other plugins use {@link it.buzz.template.api.TemplateApi#get()}.
 */
public final class TemplatePlugin extends JavaPlugin {

    private final ServiceRegistry services = new ServiceRegistry(getLogger());

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        try {
            ConfigService config = services.register(new ConfigService(getDataFolder().toPath()));
            config.enable(); // needed to build the rest of the graph

            TaskExecutor executor = services.register(new TaskExecutor(this));
            MessageService messages = services.register(new MessageService(config));
            DatabaseService database = services.register(new DatabaseService(this, config));
            ProfileService profiles = services.register(new ProfileService(this, config, database, executor, messages));
            services.register(new CommandService(this, new CommandErrorHandler(messages, this),
                    new TemplateCommand(this, services, messages, profiles, executor, config.get(PluginConfig.DB_TYPE).name())));
            services.register(new ApiService(this, profiles));

            services.enableAll();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Startup failed, disabling plugin", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("Enabled in " + (System.currentTimeMillis() - start) + " ms");
    }

    @Override
    public void onDisable() {
        services.disableAll();
    }
}
