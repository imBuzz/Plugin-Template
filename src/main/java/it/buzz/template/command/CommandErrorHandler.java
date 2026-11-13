package it.buzz.template.command;

import ch.jalu.configme.properties.Property;
import it.buzz.template.core.message.MessageService;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.bukkit.plugin.Plugin;
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
import revxrsal.commands.exception.CommandInvocationException;
import revxrsal.commands.exception.CooldownException;
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
import revxrsal.commands.exception.TrailingInputException;
import revxrsal.commands.exception.UnknownCommandException;
import revxrsal.commands.exception.UnknownParameterException;
import revxrsal.commands.exception.ValueNotAllowedException;
import revxrsal.commands.node.ParameterNode;

import java.util.logging.Level;

import static it.buzz.template.core.config.MessagesConfig.*;
import static it.buzz.template.core.message.MessageService.arg;

/** Routes every Lamp error to a configurable line of {@code messages.yml}. */
public final class CommandErrorHandler extends BukkitExceptionHandler {

    private final MessageService messages;
    private final Plugin plugin;

    public CommandErrorHandler(MessageService messages, Plugin plugin) {
        this.messages = messages;
        this.plugin = plugin;
    }

    private void send(BukkitCommandActor actor, Property<String> message, TagResolver... args) {
        messages.send(actor.sender(), message, args);
    }

    // ---- Bukkit specific ----

    @Override public void onInvalidPlayer(InvalidPlayerException e, BukkitCommandActor actor) { send(actor, ERR_INVALID_PLAYER, arg("input", e.input())); }
    @Override public void onInvalidWorld(InvalidWorldException e, BukkitCommandActor actor) { send(actor, ERR_INVALID_WORLD, arg("input", e.input())); }
    @Override public void onSenderNotConsole(SenderNotConsoleException e, BukkitCommandActor actor) { send(actor, ERR_MUST_BE_CONSOLE); }
    @Override public void onSenderNotPlayer(SenderNotPlayerException e, BukkitCommandActor actor) { send(actor, ERR_MUST_BE_PLAYER); }
    @Override public void onMalformedEntitySelector(MalformedEntitySelectorException e, BukkitCommandActor actor) { send(actor, ERR_INVALID_SELECTOR, arg("input", e.input()), arg("error", e.errorMessage())); }
    @Override public void onNonPlayerEntities(NonPlayerEntitiesException e, BukkitCommandActor actor) { send(actor, ERR_NON_PLAYERS, arg("input", e.input())); }
    @Override public void onMoreThanOneEntity(MoreThanOneEntityException e, BukkitCommandActor actor) { send(actor, ERR_ONLY_ONE_ENTITY); }
    @Override public void onEmptyEntitySelector(EmptyEntitySelectorException e, BukkitCommandActor actor) { send(actor, ERR_EMPTY_SELECTOR); }

    // ---- Generic ----

    @Override public void onEnumNotFound(@NotNull EnumNotFoundException e, @NotNull BukkitCommandActor actor) { send(actor, ERR_INVALID_CHOICE, arg("input", e.input())); }
    @Override public void onExpectedLiteral(@NotNull ExpectedLiteralException e, @NotNull BukkitCommandActor actor) { send(actor, ERR_EXPECTED_LITERAL, arg("expected", e.node().name()), arg("input", e.input())); }
    @Override public void onInvalidBoolean(@NotNull InvalidBooleanException e, @NotNull BukkitCommandActor actor) { send(actor, ERR_INVALID_BOOLEAN, arg("input", e.input())); }
    @Override public void onInvalidDecimal(@NotNull InvalidDecimalException e, @NotNull BukkitCommandActor actor) { send(actor, ERR_INVALID_NUMBER, arg("input", e.input())); }
    @Override public void onInvalidInteger(@NotNull InvalidIntegerException e, @NotNull BukkitCommandActor actor) { send(actor, ERR_INVALID_NUMBER, arg("input", e.input())); }
    @Override public void onInvalidUUID(@NotNull InvalidUUIDException e, @NotNull BukkitCommandActor actor) { send(actor, ERR_INVALID_UUID, arg("input", e.input())); }
    @Override public void onNoPermission(@NotNull NoPermissionException e, @NotNull BukkitCommandActor actor) { send(actor, ERR_NO_PERMISSION); }
    @Override public void onUnknownCommand(@NotNull UnknownCommandException e, @NotNull BukkitCommandActor actor) { send(actor, ERR_UNKNOWN_COMMAND, arg("input", e.input())); }
    @Override public void onTrailingInput(@NotNull TrailingInputException e, @NotNull BukkitCommandActor actor) { send(actor, ERR_EXPECTED_WHITESPACE); }
    @Override public void onInvalidHelpPage(@NotNull InvalidHelpPageException e, @NotNull BukkitCommandActor actor) { send(actor, ERR_INVALID_HELP_PAGE, arg("pages", e.numberOfPages())); }
    @Override public void onUnknownParameter(@NotNull UnknownParameterException e, @NotNull BukkitCommandActor actor) { send(actor, ERR_UNKNOWN_FLAG, arg("input", e.name())); }
    @Override public void onCooldown(@NotNull CooldownException e, @NotNull BukkitCommandActor actor) { send(actor, ERR_COOLDOWN, arg("time", formatTimeFancy(e.getTimeLeftMillis()))); }
    @Override public void onValueNotAllowed(@NotNull ValueNotAllowedException e, @NotNull BukkitCommandActor actor) { send(actor, ERR_VALUE_NOT_ALLOWED, arg("input", e.input()), arg("allowed", String.join(", ", e.allowedValues()))); }

    @Override
    public void onInputParse(@NotNull InputParseException e, @NotNull BukkitCommandActor actor) {
        send(actor, e.cause() == InputParseException.Cause.EXPECTED_WHITESPACE ? ERR_EXPECTED_WHITESPACE : ERR_INVALID_QUOTE);
    }

    @Override
    public void onMissingArgument(@NotNull MissingArgumentException e, @NotNull BukkitCommandActor actor, @NotNull ParameterNode<BukkitCommandActor, ?> parameter) {
        send(actor, ERR_MISSING_ARGUMENT, arg("parameter", parameter.name()), arg("usage", parameter.command().usage()));
    }

    @Override
    public void onNumberNotInRange(@NotNull NumberNotInRangeException e, @NotNull BukkitCommandActor actor, @NotNull ParameterNode<BukkitCommandActor, Number> parameter) {
        send(actor, ERR_NUMBER_RANGE, arg("parameter", parameter.name()), arg("min", fmt(e.minimum())), arg("max", fmt(e.maximum())), arg("input", fmt(e.input())));
    }

    @Override
    public void onInvalidListSize(@NotNull InvalidListSizeException e, @NotNull BukkitCommandActor actor, @NotNull ParameterNode<BukkitCommandActor, ?> parameter) {
        send(actor, ERR_SIZE_RANGE, arg("parameter", parameter.name()), arg("min", fmt(e.minimum())), arg("max", fmt(e.maximum())));
    }

    @Override
    public void onInvalidStringSize(@NotNull InvalidStringSizeException e, @NotNull BukkitCommandActor actor, @NotNull ParameterNode<BukkitCommandActor, ?> parameter) {
        send(actor, ERR_SIZE_RANGE, arg("parameter", parameter.name()), arg("min", fmt(e.minimum())), arg("max", fmt(e.maximum())));
    }

    @Override
    public void onCommandInvocation(@NotNull CommandInvocationException e, @NotNull BukkitCommandActor actor) {
        send(actor, ERR_INTERNAL);
        plugin.getLogger().log(Level.SEVERE, "Unhandled exception while executing a command", e.cause());
    }
}
