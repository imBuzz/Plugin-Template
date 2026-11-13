package it.buzz.template.core.api;

import it.buzz.template.api.TemplateApi;
import it.buzz.template.api.player.PlayerProfileService;
import it.buzz.template.core.lifecycle.Service;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;

/** Publishes {@link TemplateApi} in Bukkit's ServicesManager for other plugins. Register it last. */
public final class ApiService implements Service, TemplateApi {

    private final Plugin plugin;
    private final PlayerProfileService profiles;

    public ApiService(Plugin plugin, PlayerProfileService profiles) {
        this.plugin = plugin;
        this.profiles = profiles;
    }

    @Override
    public void enable() {
        Bukkit.getServicesManager().register(TemplateApi.class, this, plugin, ServicePriority.Normal);
    }

    @Override
    public void disable() {
        Bukkit.getServicesManager().unregister(TemplateApi.class, this);
    }

    @Override
    public PlayerProfileService profiles() {
        return profiles;
    }

    @Override
    public String version() {
        return plugin.getPluginMeta().getVersion();
    }
}
