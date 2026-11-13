package it.buzz.template.core.message;

import ch.jalu.configme.properties.Property;
import it.buzz.template.core.config.ConfigService;
import it.buzz.template.core.config.MessagesConfig;
import it.buzz.template.core.lifecycle.Service;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * Renders {@code messages.yml} entries with MiniMessage.
 * <p>
 * Use {@link #arg(String, Object)} for user-controlled values: they are inserted as plain text,
 * so a player named {@code <red>x} can never inject formatting or click events.
 */
public final class MessageService implements Service {

    private final ConfigService config;
    private final MiniMessage mini = MiniMessage.miniMessage();
    private TagResolver prefix = TagResolver.empty();

    public MessageService(ConfigService config) {
        this.config = config;
    }

    @Override
    public void enable() {
        reload();
    }

    @Override
    public void reload() {
        prefix = Placeholder.component("prefix", mini.deserialize(config.message(MessagesConfig.PREFIX)));
    }

    public static TagResolver arg(String key, Object value) {
        return Placeholder.unparsed(key, String.valueOf(value));
    }

    public static TagResolver component(String key, Component value) {
        return Placeholder.component(key, value);
    }

    public Component render(Property<String> message, TagResolver... args) {
        String raw = config.message(message);
        if (raw.isEmpty()) {
            return Component.empty();
        }
        return mini.deserialize(raw, TagResolver.builder().resolver(prefix).resolvers(args).build());
    }

    /** Sends a message; empty messages are skipped so server owners can disable any line. */
    public void send(Audience audience, Property<String> message, TagResolver... args) {
        if (!config.message(message).isEmpty()) {
            audience.sendMessage(render(message, args));
        }
    }
}
