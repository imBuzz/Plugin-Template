package it.buzz.template;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.buzz.template.commands.CustomExceptionAdapter;
import it.buzz.template.commands.PluginCommands;
import it.buzz.template.config.PluginConfiguration;
import it.buzz.template.manager.database.adapter.TemplatePlayerDataAdapterFactory;
import it.buzz.template.manager.database.DatabaseManager;
import it.buzz.template.manager.player.TemplatePlayerData;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.io.File;

public final class TemplatePlugin extends JavaPlugin {

    public static Gson GSON = new GsonBuilder()
            .registerTypeHierarchyAdapter(TemplatePlayerData.class, new TemplatePlayerDataAdapterFactory())
            .create();

    @Getter private SettingsManager settingsManager;
    @Getter private DatabaseManager databaseManager;

    private Lamp<BukkitCommandActor> commandHandler;

    @Override
    public void onEnable() {
        settingsManager = SettingsManagerBuilder
                .withYamlFile(new File(getDataFolder().getAbsolutePath() + File.separator + "config.yml"))
                .configurationData(PluginConfiguration.class)
                .useDefaultMigrationService()
                .create();

        databaseManager = new DatabaseManager(this);
        databaseManager.start();

        setupCommands();
    }

    @Override
    public void onDisable() {
        databaseManager.stop();
    }

    private void setupCommands() {
        commandHandler = BukkitLamp.builder(this)
                .exceptionHandler(new CustomExceptionAdapter(this))
                .build();

        commandHandler.register(new PluginCommands(this));
    }

}
