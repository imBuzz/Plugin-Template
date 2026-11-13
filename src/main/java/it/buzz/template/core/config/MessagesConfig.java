package it.buzz.template.core.config;

import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
import ch.jalu.configme.properties.Property;

import static ch.jalu.configme.properties.PropertyInitializer.newProperty;

/**
 * Schema of {@code messages.yml}. Every value is a <a href="https://docs.advntr.dev/minimessage/format.html">MiniMessage</a>
 * string; {@code <prefix>} and the named placeholders listed next to each key are available.
 */
public final class MessagesConfig implements SettingsHolder {

    public static final Property<String> PREFIX = newProperty("prefix", "<gradient:#5e4fa2:#f79459>Template</gradient> <dark_gray>»</dark_gray> ");

    // ---- Plugin commands ----
    public static final Property<String> RELOAD_SUCCESS = newProperty("commands.reload.success", "<prefix><green>Configuration reloaded in <white><time></white> ms.");
    public static final Property<String> RELOAD_FAILED = newProperty("commands.reload.failed", "<prefix><red>Reload failed, check the console for details.");
    public static final Property<String> INFO = newProperty("commands.info", "<prefix><gray>Running <white><name></white> v<white><version></white> · storage <white><storage></white> · <white><cached></white> players cached.");
    public static final Property<String> STATS = newProperty("commands.stats.result", "<prefix><gray><player></gray> <dark_gray>|</dark_gray> joins: <white><joins></white> <dark_gray>|</dark_gray> first seen: <white><first_seen></white>");
    public static final Property<String> STATS_NOT_FOUND = newProperty("commands.stats.not-found", "<prefix><red>No data found for <white><player></white>.");
    public static final Property<String> STATS_LOADING = newProperty("commands.stats.loading", "<prefix><gray>Loading data for <white><player></white>...");

    // ---- Player data ----
    public static final Property<String> DATA_LOAD_FAILED = newProperty("player-data.load-failed", "<red>Your data could not be loaded. Please try again in a moment.");

    // ---- Command framework errors (placeholders in angle brackets) ----
    public static final Property<String> ERR_NO_PERMISSION = newProperty("errors.no-permission", "<prefix><red>You do not have permission to do that.");
    public static final Property<String> ERR_MUST_BE_PLAYER = newProperty("errors.must-be-player", "<prefix><red>Only players can use this command.");
    public static final Property<String> ERR_MUST_BE_CONSOLE = newProperty("errors.must-be-console", "<prefix><red>This command can only be run from the console.");
    public static final Property<String> ERR_INVALID_PLAYER = newProperty("errors.invalid-player", "<prefix><red>Player <white><input></white> not found.");
    public static final Property<String> ERR_INVALID_WORLD = newProperty("errors.invalid-world", "<prefix><red>World <white><input></white> not found.");
    public static final Property<String> ERR_INVALID_SELECTOR = newProperty("errors.invalid-selector", "<prefix><red>Malformed selector <white><input></white>: <error>");
    public static final Property<String> ERR_ONLY_ONE_ENTITY = newProperty("errors.only-one-entity", "<prefix><red>Only one entity is allowed, but the selector matches more.");
    public static final Property<String> ERR_NON_PLAYERS = newProperty("errors.non-players-not-allowed", "<prefix><red>Selector <white><input></white> may only target players.");
    public static final Property<String> ERR_EMPTY_SELECTOR = newProperty("errors.empty-selector", "<prefix><red>No entities were found.");
    public static final Property<String> ERR_INVALID_CHOICE = newProperty("errors.invalid-choice", "<prefix><red>Invalid choice <white><input></white>.");
    public static final Property<String> ERR_EXPECTED_LITERAL = newProperty("errors.expected-literal", "<prefix><red>Expected <white><expected></white>, found <white><input></white>.");
    public static final Property<String> ERR_INVALID_QUOTE = newProperty("errors.invalid-quoted-string", "<prefix><red>Invalid quoted string.");
    public static final Property<String> ERR_EXPECTED_WHITESPACE = newProperty("errors.expected-whitespace", "<prefix><red>Unexpected trailing input.");
    public static final Property<String> ERR_INVALID_BOOLEAN = newProperty("errors.invalid-boolean", "<prefix><red>Expected <white>true</white> or <white>false</white>, found <white><input></white>.");
    public static final Property<String> ERR_INVALID_NUMBER = newProperty("errors.invalid-number", "<prefix><red>Invalid number <white><input></white>.");
    public static final Property<String> ERR_INVALID_UUID = newProperty("errors.invalid-uuid", "<prefix><red>Invalid UUID <white><input></white>.");
    public static final Property<String> ERR_MISSING_ARGUMENT = newProperty("errors.missing-argument", "<prefix><red>Missing <white><parameter></white>. Usage: <white>/<usage></white>");
    public static final Property<String> ERR_NUMBER_RANGE = newProperty("errors.number-not-in-range", "<prefix><red><parameter> must be between <white><min></white> and <white><max></white> (found <white><input></white>).");
    public static final Property<String> ERR_SIZE_RANGE = newProperty("errors.size-not-in-range", "<prefix><red><parameter> must have between <white><min></white> and <white><max></white> entries/characters.");
    public static final Property<String> ERR_UNKNOWN_COMMAND = newProperty("errors.unknown-command", "<prefix><red>Unknown command <white><input></white>.");
    public static final Property<String> ERR_INVALID_HELP_PAGE = newProperty("errors.invalid-help-page", "<prefix><red>Page must be between <white>1</white> and <white><pages></white>.");
    public static final Property<String> ERR_UNKNOWN_FLAG = newProperty("errors.unknown-flag", "<prefix><red>Unknown flag <white><input></white>.");
    public static final Property<String> ERR_COOLDOWN = newProperty("errors.cooldown", "<prefix><red>Please wait <white><time></white> before using this again.");
    public static final Property<String> ERR_VALUE_NOT_ALLOWED = newProperty("errors.value-not-allowed", "<prefix><red><white><input></white> is not allowed. Allowed: <white><allowed></white>.");
    public static final Property<String> ERR_INTERNAL = newProperty("errors.internal", "<prefix><red>An internal error occurred. Please contact an administrator.");

    private MessagesConfig() {
    }

    @Override
    public void registerComments(CommentsConfiguration conf) {
        conf.setComment("", "All messages use MiniMessage: https://docs.advntr.dev/minimessage/format.html",
                "Use <prefix> anywhere to insert the prefix. Leave a message empty (\"\") to disable it.");
    }
}
