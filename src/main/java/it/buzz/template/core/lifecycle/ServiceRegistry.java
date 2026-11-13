package it.buzz.template.core.lifecycle;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Owns every {@link Service} and drives their lifecycle in a deterministic order. */
public final class ServiceRegistry {

    private final Logger logger;
    private final List<Service> registered = new ArrayList<>();
    private final Deque<Service> enabled = new ArrayDeque<>();

    public ServiceRegistry(Logger logger) {
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public <S extends Service> S register(S service) {
        if (!enabled.isEmpty()) {
            throw new IllegalStateException("Services must be registered before enableAll()");
        }
        registered.add(Objects.requireNonNull(service, "service"));
        return service;
    }

    /**
     * Enables all services in registration order.
     *
     * @throws ServiceException if any service fails; services enabled so far are rolled back first
     */
    public void enableAll() {
        for (Service service : registered) {
            long start = System.nanoTime();
            try {
                service.enable();
            } catch (Exception e) {
                disableAll();
                throw new ServiceException("Failed to enable " + service.name(), e);
            }
            enabled.push(service);
            logger.fine(() -> "Enabled %s in %d ms".formatted(service.name(), (System.nanoTime() - start) / 1_000_000));
        }
    }

    /** Disables enabled services in reverse order. A failing service never prevents the others from stopping. */
    public void disableAll() {
        while (!enabled.isEmpty()) {
            Service service = enabled.pop();
            try {
                service.disable();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to disable " + service.name(), e);
            }
        }
    }

    /** Reloads every enabled service in startup order. */
    public void reloadAll() throws ServiceException {
        List<Service> order = new ArrayList<>(enabled);
        Collections.reverse(order);
        for (Service service : order) {
            try {
                service.reload();
            } catch (Exception e) {
                throw new ServiceException("Failed to reload " + service.name(), e);
            }
        }
    }

    public static final class ServiceException extends RuntimeException {
        public ServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
