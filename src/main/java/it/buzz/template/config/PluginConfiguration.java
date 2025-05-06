package it.buzz.template.config;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.properties.BeanProperty;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.properties.StringProperty;
import it.buzz.template.config.serializable.DatabaseCredentials;

public class PluginConfiguration implements SettingsHolder {

    public static final Property<DatabaseCredentials> DATABASE_CREDENTIALS =
            new BeanProperty<>(DatabaseCredentials.class, "settings.database",
                    new DatabaseCredentials("localhost", "root", "root", "minecraft", 3306));


    @Comment("Don't touch the part 'messages.commands' if you don't see these messages")
    public static final Property<String> INVALID_ENUM           = new StringProperty("language.commands.invalid-enum",           "&cInvalid {0}: {1}.");
    public static final Property<String> INVALID_NUMBER         = new StringProperty("language.commands.invalid-number",         "&cExpected a number, but found {0}.");
    public static final Property<String> INVALID_UUID           = new StringProperty("language.commands.invalid-uuid",           "&cInvalid UUID: {0}");
    public static final Property<String> INVALID_URL            = new StringProperty("language.commands.invalid-url",            "&cInvalid URL: {0}");
    public static final Property<String> INVALID_BOOLEAN        = new StringProperty("language.commands.invalid-boolean",        "&cExpected true or false, but found {0}");
    public static final Property<String> INVALID_QUOTED_STRING  = new StringProperty("language.commands.invalid-quoted-string",  "&cInvalid quoted string");
    public static final Property<String> INVALID_COMMAND        = new StringProperty("language.commands.invalid-command",        "&cInvalid command: {0}.");
    public static final Property<String> INVALID_SUBCOMMAND     = new StringProperty("language.commands.invalid-subcommand",     "&cInvalid subcommand: {0}.");
    public static final Property<String> INVALID_HELP_PAGE      = new StringProperty("language.commands.invalid-help-page",      "&cInvalid help page: {0}. Must be between 1 and {1}.");

    public static final Property<String> MISSING_ARGUMENT       = new StringProperty("language.commands.missing-argument",       "&cYou must specify a value for the {0}!");
    public static final Property<String> NO_PERMISSION          = new StringProperty("language.commands.no-permission",          "&cYou do not have permission to execute this command.");
    public static final Property<String> ERROR_OCCURRED         = new StringProperty("language.commands.error-occurred",         "&cAn error occurred while executing this command.");
    public static final Property<String> TOO_MANY_ARGUMENTS     = new StringProperty("language.commands.too-many-arguments",     "&cToo many arguments! Correct usage: /{0}");
    public static final Property<String> NO_SUBCOMMAND_SPECIFIED= new StringProperty("language.commands.no-subcommand-specified","&cYou must specify a subcommand!");
    public static final Property<String> NUMBER_NOT_IN_RANGE    = new StringProperty("language.commands.number-not-in-range",    "&c{0} must be between {1} and {2} (found {3})");

    public static final Property<String> MUST_BE_PLAYER         = new StringProperty("language.commands.must-be-player",         "&cYou must be a player to use this command!");
    public static final Property<String> MUST_BE_CONSOLE        = new StringProperty("language.commands.must-be-console",        "&cThis command can only be used on console!");
    public static final Property<String> INVALID_PLAYER         = new StringProperty("language.commands.invalid-player",         "&cInvalid player: &e{0}");
    public static final Property<String> INVALID_WORLD          = new StringProperty("language.commands.invalid-world",          "&cInvalid world: &e{0}");
    public static final Property<String> INVALID_SELECTOR       = new StringProperty("language.commands.invalid-selector",       "&cMalformed selector: &e{0}");
    public static final Property<String> ONLY_ONE_PLAYER        = new StringProperty("language.commands.only-one-player",        "&cSelector &e{0}&c allows more than one player. Only one is allowed");
    public static final Property<String> NON_PLAYERS_NOT_ALLOWED= new StringProperty("language.commands.non-players-not-allowed","&cNon-player entities are not allowed");

    public static final Property<String> EMPTY_SELECTOR         = new StringProperty("language.commands.empty-selector",         "&cNo entities were found.");
    public static final Property<String> EXPECTED_WHITESPACE    = new StringProperty("language.commands.expected-whitespace",    "&cExpected whitespace to end one argument, but found trailing data.");
    public static final Property<String> STRING_TOO_SHORT       = new StringProperty("language.commands.string-too-short",       "&cParameter {0} must be at least {1} characters long.");
    public static final Property<String> STRING_TOO_LONG        = new StringProperty("language.commands.string-too-long",        "&cParameter {0} can be at most {1} characters long.");
    public static final Property<String> UNKNOWN_FLAG           = new StringProperty("language.commands.unknown-flag",           "&cUnknown flag: {0}");
    public static final Property<String> UNKNOWN_SHORTHAND_FLAG = new StringProperty("language.commands.unknown-shorthand-flag", "&cUnknown shorthand flag: {0}");
}
