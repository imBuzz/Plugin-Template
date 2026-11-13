package it.buzz.template.api;

import it.buzz.template.api.player.PlayerProfileService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Entry point of the public API, registered in Bukkit's {@link org.bukkit.plugin.ServicesManager}.
 *
 * <pre>{@code
 * TemplateApi api = TemplateApi.get();
 * api.profiles().get(player).increment("kills", 1);
 * }</pre>
 *
 * Depend on the {@code api} artifact with {@code compileOnly} and add the plugin to {@code depend}
 * (or {@code softdepend}) in your {@code plugin.yml}.
 */
public interface TemplateApi {

    /** Per-player persistent data. */
    PlayerProfileService profiles();

    /** Version of the running implementation. */
    String version();

    /**
     * Looks up the running implementation.
     *
     * @throws IllegalStateException if the plugin is not installed or not enabled yet
     */
    static TemplateApi get() {
        RegisteredServiceProvider<TemplateApi> provider = Bukkit.getServicesManager().getRegistration(TemplateApi.class);
        if (provider == null) {
            throw new IllegalStateException("TemplateApi is not available: is the plugin installed and enabled?");
        }
        return provider.getProvider();
    }
}
