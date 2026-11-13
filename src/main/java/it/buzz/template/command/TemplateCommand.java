package it.buzz.template.command;

import it.buzz.template.core.concurrent.TaskExecutor;
import it.buzz.template.core.lifecycle.ServiceRegistry;
import it.buzz.template.core.message.MessageService;
import it.buzz.template.api.player.PlayerProfileService;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.logging.Level;

import static it.buzz.template.core.config.MessagesConfig.*;
import static it.buzz.template.core.message.MessageService.arg;

/** Admin command: {@code /template <reload|info|stats>}. Use it as a reference for your own commands. */
@Command({"template", "tpl"})
@CommandPermission("template.admin")
public final class TemplateCommand {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withZone(ZoneId.systemDefault());

    private final Plugin plugin;
    private final ServiceRegistry services;
    private final MessageService messages;
    private final PlayerProfileService profiles;
    private final TaskExecutor executor;
    private final String storage;

    public TemplateCommand(Plugin plugin, ServiceRegistry services, MessageService messages,
                           PlayerProfileService profiles, TaskExecutor executor, String storage) {
        this.plugin = plugin;
        this.services = services;
        this.messages = messages;
        this.profiles = profiles;
        this.executor = executor;
        this.storage = storage;
    }

    @Subcommand("reload")
    @Description("Reloads config.yml and messages.yml")
    @CommandPermission("template.admin.reload")
    public void reload(CommandSender sender) {
        long start = System.currentTimeMillis();
        try {
            services.reloadAll();
            messages.send(sender, RELOAD_SUCCESS, arg("time", System.currentTimeMillis() - start));
        } catch (ServiceRegistry.ServiceException e) {
            plugin.getLogger().log(Level.SEVERE, "Reload failed", e);
            messages.send(sender, RELOAD_FAILED);
        }
    }

    @Subcommand("info")
    @Description("Shows plugin information")
    public void info(CommandSender sender) {
        messages.send(sender, INFO,
                arg("name", plugin.getName()),
                arg("version", plugin.getPluginMeta().getVersion()),
                arg("storage", storage),
                arg("cached", profiles.loadedCount()));
    }

    @Subcommand("stats")
    @Description("Shows stored data of a player (online or offline)")
    @CommandPermission("template.admin.stats")
    public void stats(CommandSender sender, @Optional String player) {
        if (player == null) {
            if (!(sender instanceof Player self)) {
                messages.send(sender, ERR_MUST_BE_PLAYER);
                return;
            }
            player = self.getName();
        }
        String target = player;
        messages.send(sender, STATS_LOADING, arg("player", target));
        profiles.lookup(target).thenAcceptAsync(result -> result.ifPresentOrElse(
                data -> messages.send(sender, STATS,
                        arg("player", data.name()),
                        arg("joins", data.joins()),
                        arg("first_seen", DATE.format(Instant.ofEpochMilli(data.firstSeen())))),
                () -> messages.send(sender, STATS_NOT_FOUND, arg("player", target))
        ), executor.main()).exceptionally(error -> {
            executor.main().execute(() -> messages.send(sender, ERR_INTERNAL));
            return null;
        });
    }
}
