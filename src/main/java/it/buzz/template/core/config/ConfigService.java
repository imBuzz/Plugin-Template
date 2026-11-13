package it.buzz.template.core.config;

import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import ch.jalu.configme.properties.Property;
import it.buzz.template.core.lifecycle.Service;

import java.nio.file.Files;
import java.nio.file.Path;

/** Loads and reloads {@code config.yml} and {@code messages.yml}. */
public final class ConfigService implements Service {

    private final Path dataFolder;
    private SettingsManager config;
    private SettingsManager messages;

    public ConfigService(Path dataFolder) {
        this.dataFolder = dataFolder;
    }

    @Override
    public void enable() throws Exception {
        if (config != null) {
            return; // already enabled eagerly by the plugin bootstrap
        }
        Files.createDirectories(dataFolder);
        config = open("config.yml", PluginConfig.class);
        messages = open("messages.yml", MessagesConfig.class);
    }

    @Override
    public void reload() {
        config.reload();
        messages.reload();
    }

    public <T> T get(Property<T> property) {
        return config.getProperty(property);
    }

    public String message(Property<String> property) {
        return messages.getProperty(property);
    }

    private SettingsManager open(String file, Class<? extends SettingsHolder> holder) {
        return SettingsManagerBuilder
                .withYamlFile(dataFolder.resolve(file))
                .configurationData(holder)
                .useDefaultMigrationService()
                .create();
    }
}
