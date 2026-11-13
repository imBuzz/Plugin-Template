package it.buzz.template.core.lifecycle;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceRegistryTest {

    private final List<String> events = new ArrayList<>();
    private final ServiceRegistry registry = new ServiceRegistry(Logger.getAnonymousLogger());

    private Service service(String name, boolean failOnEnable) {
        return new Service() {
            @Override
            public void enable() {
                if (failOnEnable) {
                    throw new IllegalStateException("boom");
                }
                events.add("enable " + name);
            }

            @Override
            public void disable() {
                events.add("disable " + name);
            }

            @Override
            public void reload() {
                events.add("reload " + name);
            }
        };
    }

    @Test
    void enablesInOrderAndDisablesInReverse() {
        registry.register(service("a", false));
        registry.register(service("b", false));

        registry.enableAll();
        registry.reloadAll();
        registry.disableAll();

        assertEquals(List.of("enable a", "enable b", "reload a", "reload b", "disable b", "disable a"), events);
    }

    @Test
    void failedStartupRollsBackEnabledServices() {
        registry.register(service("a", false));
        registry.register(service("b", false));
        registry.register(service("c", true));

        assertThrows(ServiceRegistry.ServiceException.class, registry::enableAll);
        assertEquals(List.of("enable a", "enable b", "disable b", "disable a"), events);

        registry.disableAll(); // second call must be a no-op
        assertEquals(4, events.size());
    }

    @Test
    void failingDisableDoesNotStopOthers() {
        registry.register(service("a", false));
        registry.register(new Service() {
            @Override
            public void enable() {
            }

            @Override
            public void disable() {
                throw new IllegalStateException("boom");
            }
        });
        registry.enableAll();
        registry.disableAll();
        assertEquals(List.of("enable a", "disable a"), events);
    }
}
