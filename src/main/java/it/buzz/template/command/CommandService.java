package it.buzz.template.command;

import it.buzz.template.core.lifecycle.Service;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.util.List;

/** Registers every command class with Lamp. Add new command objects to the list passed in. */
public final class CommandService implements Service {

    private final JavaPlugin plugin;
    private final CommandErrorHandler errorHandler;
    private final List<Object> commands;
    private Lamp<BukkitCommandActor> lamp;

    public CommandService(JavaPlugin plugin, CommandErrorHandler errorHandler, Object... commands) {
        this.plugin = plugin;
        this.errorHandler = errorHandler;
        this.commands = List.of(commands);
    }

    @Override
    public void enable() {
        lamp = BukkitLamp.builder(plugin)
                .exceptionHandler(errorHandler)
                .build();
        commands.forEach(lamp::register);
    }

    @Override
    public void disable() {
        if (lamp != null) {
            lamp.unregisterAllCommands();
        }
    }
}
