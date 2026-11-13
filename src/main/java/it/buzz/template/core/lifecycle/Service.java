package it.buzz.template.core.lifecycle;

/**
 * A unit of plugin functionality with an explicit lifecycle.
 * <p>
 * Services are enabled in registration order and disabled in reverse order by the {@link ServiceRegistry}.
 * Throwing from {@link #enable()} aborts startup: every service already enabled is disabled again.
 */
public interface Service {

    void enable() throws Exception;

    default void disable() throws Exception {
    }

    /** Called by {@code /template reload}. Services that cache configuration should refresh it here. */
    default void reload() throws Exception {
    }

    default String name() {
        return getClass().getSimpleName();
    }
}
