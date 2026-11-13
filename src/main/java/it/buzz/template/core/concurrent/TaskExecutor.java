package it.buzz.template.core.concurrent;

import it.buzz.template.core.lifecycle.Service;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Central place for thread hopping.
 * <ul>
 *     <li>{@link #async()} runs blocking work (I/O, JDBC) on virtual threads, off the server thread.</li>
 *     <li>{@link #main()} hands results back to the server thread to touch the Bukkit API safely.</li>
 * </ul>
 * Unlike the Bukkit async scheduler, this executor is drained on shutdown, so pending saves are never lost.
 */
public final class TaskExecutor implements Service {

    private final Plugin plugin;
    private final Executor mainThread;
    private ExecutorService async;

    public TaskExecutor(Plugin plugin) {
        this.plugin = plugin;
        this.mainThread = task -> {
            if (Bukkit.isPrimaryThread()) {
                task.run();
            } else if (plugin.isEnabled()) {
                Bukkit.getScheduler().runTask(plugin, task);
            }
        };
    }

    @Override
    public void enable() {
        async = Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual().name(plugin.getName() + "-async-", 0).factory());
    }

    @Override
    public void disable() throws InterruptedException {
        async.shutdown();
        if (!async.awaitTermination(15, TimeUnit.SECONDS)) {
            plugin.getLogger().warning("Async tasks did not finish within 15 seconds, forcing shutdown");
            async.shutdownNow();
        }
    }

    public Executor async() {
        return async;
    }

    public Executor main() {
        return mainThread;
    }

    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, async).whenComplete(this::logFailure);
    }

    public CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, async).whenComplete(this::logFailure);
    }

    private void logFailure(Object ignored, Throwable error) {
        if (error != null) {
            plugin.getLogger().log(Level.SEVERE, "Unhandled exception in async task", error);
        }
    }
}
