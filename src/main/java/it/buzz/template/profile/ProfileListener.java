package it.buzz.template.profile;

import it.buzz.template.core.config.MessagesConfig;
import it.buzz.template.core.message.MessageService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

/** Bridges Bukkit's connection lifecycle to {@link ProfileService}. */
final class ProfileListener implements Listener {

    private final ProfileService service;
    private final MessageService messages;
    private final Plugin plugin;

    ProfileListener(ProfileService service, MessageService messages, Plugin plugin) {
        this.service = service;
        this.messages = messages;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }
        try {
            service.load(event.getUniqueId(), event.getName());
        } catch (RuntimeException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load profile of " + event.getName(), e);
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, messages.render(MessagesConfig.DATA_LOAD_FAILED));
        }
    }

    /** Another plugin denied the login after we loaded: free the memory. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreLoginResult(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            service.discard(event.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        service.join(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        service.quit(event.getPlayer());
    }
}
