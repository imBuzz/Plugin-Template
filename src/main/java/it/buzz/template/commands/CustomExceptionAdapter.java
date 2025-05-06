package it.buzz.template.commands;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.properties.Property;
import it.buzz.template.TemplatePlugin;
import it.buzz.template.config.PluginConfiguration;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.exception.BukkitExceptionHandler;
import revxrsal.commands.bukkit.exception.EmptyEntitySelectorException;
import revxrsal.commands.bukkit.exception.InvalidPlayerException;
import revxrsal.commands.bukkit.exception.InvalidWorldException;
import revxrsal.commands.bukkit.exception.MalformedEntitySelectorException;
import revxrsal.commands.bukkit.exception.MoreThanOneEntityException;
import revxrsal.commands.bukkit.exception.NonPlayerEntitiesException;
import revxrsal.commands.bukkit.exception.SenderNotConsoleException;
import revxrsal.commands.bukkit.exception.SenderNotPlayerException;
import revxrsal.commands.bukkit.util.BukkitUtils;
import revxrsal.commands.exception.CommandInvocationException;
import revxrsal.commands.exception.EnumNotFoundException;
import revxrsal.commands.exception.ExpectedLiteralException;
import revxrsal.commands.exception.InputParseException;
import revxrsal.commands.exception.InvalidBooleanException;
import revxrsal.commands.exception.InvalidDecimalException;
import revxrsal.commands.exception.InvalidHelpPageException;
import revxrsal.commands.exception.InvalidIntegerException;
import revxrsal.commands.exception.InvalidListSizeException;
import revxrsal.commands.exception.InvalidStringSizeException;
import revxrsal.commands.exception.InvalidUUIDException;
import revxrsal.commands.exception.MissingArgumentException;
import revxrsal.commands.exception.NoPermissionException;
import revxrsal.commands.exception.NumberNotInRangeException;
import revxrsal.commands.exception.SendableException;
import revxrsal.commands.exception.UnknownCommandException;
import revxrsal.commands.exception.UnknownParameterException;
import revxrsal.commands.node.ParameterNode;

import java.text.MessageFormat;

public class CustomExceptionAdapter extends BukkitExceptionHandler {

    private final SettingsManager settings;

    public CustomExceptionAdapter(TemplatePlugin plugin) {
        this.settings = plugin.getSettingsManager();
    }

    /* ----------------------------------------------------------
     *  Helper
     * ---------------------------------------------------------- */
    private String msg(Property<String> prop, Object... args) {
        return BukkitUtils.legacyColorize(MessageFormat.format(settings.getProperty(prop), args));
    }

    /* ----------------------------------------------------------
     *  Bukkit-specific
     * ---------------------------------------------------------- */
    @HandleException
    public void onInvalidPlayer(InvalidPlayerException e, BukkitCommandActor actor) {
        actor.error(msg(PluginConfiguration.INVALID_PLAYER, e.input()));
    }

    @HandleException
    public void onInvalidWorld(InvalidWorldException e, BukkitCommandActor actor) {
        actor.error(msg(PluginConfiguration.INVALID_WORLD, e.input()));
    }

    @HandleException
    public void onSenderNotConsole(SenderNotConsoleException e, BukkitCommandActor actor) {
        actor.error(msg(PluginConfiguration.MUST_BE_CONSOLE));
    }

    @HandleException
    public void onSenderNotPlayer(SenderNotPlayerException e, BukkitCommandActor actor) {
        actor.error(msg(PluginConfiguration.MUST_BE_PLAYER));
    }

    @HandleException
    public void onMalformedEntitySelector(MalformedEntitySelectorException e, BukkitCommandActor actor) {
        actor.error(msg(PluginConfiguration.INVALID_SELECTOR, e.input()));
    }

    @HandleException
    public void onNonPlayerEntities(NonPlayerEntitiesException e, BukkitCommandActor actor) {
        actor.error(msg(PluginConfiguration.NON_PLAYERS_NOT_ALLOWED));
    }

    @HandleException
    public void onMoreThanOneEntity(MoreThanOneEntityException e, BukkitCommandActor actor) {
        actor.error(msg(PluginConfiguration.ONLY_ONE_PLAYER, e.input()));
    }

    @HandleException
    public void onEmptyEntitySelector(EmptyEntitySelectorException e, BukkitCommandActor actor) {
        actor.error(msg(PluginConfiguration.EMPTY_SELECTOR));
    }

    /* ----------------------------------------------------------
     *  DefaultExceptionHandler override
     * ---------------------------------------------------------- */
    @HandleException
    public void onEnumNotFound(@NotNull EnumNotFoundException e, @NotNull BukkitCommandActor actor) {
        actor.error(msg(PluginConfiguration.INVALID_ENUM, "choice", e.input()));
    }

    @HandleException
    public void onExpectedLiteral(@NotNull ExpectedLiteralException e, @NotNull BukkitCommandActor actor) {
        actor.error(msg(PluginConfiguration.INVALID_SUBCOMMAND, e.input()));
    }

    @HandleException
    public void onInputParse(@NotNull InputParseException e, @NotNull BukkitCommandActor actor) {
        switch (e.cause()) {
            case INVALID_ESCAPE_CHARACTER, UNCLOSED_QUOTE ->
                    actor.error(msg(PluginConfiguration.INVALID_QUOTED_STRING));
            case EXPECTED_WHITESPACE ->
                    actor.error(msg(PluginConfiguration.EXPECTED_WHITESPACE));
        }
    }

    @HandleException
    public void onInvalidListSize(@NotNull InvalidListSizeException e,
                                  @NotNull BukkitCommandActor actor,
                                  @NotNull ParameterNode<BukkitCommandActor, ?> parameter) {
        actor.error(msg(PluginConfiguration.NUMBER_NOT_IN_RANGE,
                parameter.name(), fmt(e.minimum()), fmt(e.maximum()), fmt(e.inputSize())));
    }

    @HandleException
    public void onInvalidStringSize(@NotNull InvalidStringSizeException e,
                                    @NotNull BukkitCommandActor actor,
                                    @NotNull ParameterNode<BukkitCommandActor, ?> parameter) {
        if (e.input().length() < e.minimum()) {
            actor.error(msg(PluginConfiguration.STRING_TOO_SHORT, parameter.name(), fmt(e.minimum())));
        } else {
            actor.error(msg(PluginConfiguration.STRING_TOO_LONG, parameter.name(), fmt(e.maximum())));
        }
    }

    @HandleException
    public void onInvalidBoolean(@NotNull InvalidBooleanException e, @NotNull BukkitCommandActor actor) {
        actor.error(msg(PluginConfiguration.INVALID_BOOLEAN, e.input()));
    }

    @HandleException
    public void onInvalidDecimal(@NotNull InvalidDecimalException e, @NotNull BukkitCommandActor actor) {
        actor.error(msg(PluginConfiguration.INVALID_NUMBER, e.input()));
    }

    @HandleException
    public void onInvalidInteger(@NotNull InvalidIntegerException e, @NotNull BukkitCommandActor actor) {
        actor.error(msg(PluginConfiguration.INVALID_NUMBER, e.input()));
    }

    @HandleException
    public void onInvalidUUID(@NotNull InvalidUUIDException e, @NotNull BukkitCommandActor actor) {
        actor.error(msg(PluginConfiguration.INVALID_UUID, e.input()));
    }

    @HandleException
    public void onMissingArgument(@NotNull MissingArgumentException e,
                                  @NotNull BukkitCommandActor actor,
                                  @NotNull ParameterNode<BukkitCommandActor, ?> parameter) {
        actor.error(msg(PluginConfiguration.MISSING_ARGUMENT, parameter.name()));
    }

    @HandleException
    public void onNoPermission(@NotNull NoPermissionException e, @NotNull BukkitCommandActor actor) {
        actor.error(msg(PluginConfiguration.NO_PERMISSION));
    }

    @HandleException
    public void onNumberNotInRange(@NotNull NumberNotInRangeException e,
                                   @NotNull BukkitCommandActor actor,
                                   @NotNull ParameterNode<BukkitCommandActor, Number> parameter) {
        actor.error(msg(PluginConfiguration.NUMBER_NOT_IN_RANGE,
                parameter.name(), fmt(e.minimum()), fmt(e.maximum()), fmt(e.input())));
    }

    @HandleException
    public void onInvalidHelpPage(@NotNull InvalidHelpPageException e, @NotNull BukkitCommandActor actor) {
        actor.error(msg(PluginConfiguration.INVALID_HELP_PAGE, e.page(), e.numberOfPages()));
    }

    @HandleException
    public void onUnknownCommand(@NotNull UnknownCommandException e, @NotNull BukkitCommandActor actor) {
        actor.error(msg(PluginConfiguration.INVALID_COMMAND, e.input()));
    }

    /* ----------------------------------------------------------
     *  Altre eccezioni
     * ---------------------------------------------------------- */
    @HandleException
    public void onCommandInvocation(@NotNull CommandInvocationException e, @NotNull BukkitCommandActor actor) {
        actor.error(msg(PluginConfiguration.ERROR_OCCURRED));
        e.cause().printStackTrace();
    }

    @HandleException
    public void onUnknownParameter(@NotNull UnknownParameterException e, @NotNull BukkitCommandActor actor) {
        if (e.shorthand()) {
            actor.error(msg(PluginConfiguration.UNKNOWN_SHORTHAND_FLAG, e.name()));
        } else {
            actor.error(msg(PluginConfiguration.UNKNOWN_FLAG, e.name()));
        }
    }

    @HandleException
    public void onSendable(@NotNull SendableException e, @NotNull BukkitCommandActor actor) {
        e.sendTo(actor);
    }
}
