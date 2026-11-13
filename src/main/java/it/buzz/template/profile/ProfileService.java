package it.buzz.template.profile;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.buzz.template.api.event.PlayerProfileLoadEvent;
import it.buzz.template.api.event.PlayerProfileUnloadEvent;
import it.buzz.template.api.player.PlayerProfile;
import it.buzz.template.api.player.PlayerProfileService;
import it.buzz.template.core.concurrent.TaskExecutor;
import it.buzz.template.core.config.ConfigService;
import it.buzz.template.core.config.PluginConfig;
import it.buzz.template.core.lifecycle.Service;
import it.buzz.template.core.message.MessageService;
import it.buzz.template.storage.database.DatabaseService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Owns the in-memory profiles of online players.
 * <ul>
 *     <li>Loaded off-thread during {@code AsyncPlayerPreLoginEvent}: data is always ready on join.</li>
 *     <li>Saved asynchronously on quit and periodically (dirty profiles only, in one batch).</li>
 *     <li>Flushed synchronously on shutdown so nothing is lost on restart.</li>
 *     <li>Offline lookups go through a Caffeine cache that coalesces concurrent requests for the same name.</li>
 * </ul>
 */
public final class ProfileService implements Service, PlayerProfileService {

    private final Plugin plugin;
    private final ConfigService config;
    private final DatabaseService databaseService;
    private final TaskExecutor executor;
    private final MessageService messages;
    private final ProfileCodec codec = new ProfileCodec();
    private final Map<UUID, ProfileData> online = new ConcurrentHashMap<>();

    private ProfileRepository repository;
    private AsyncLoadingCache<String, Optional<PlayerProfile>> offline;
    private BukkitTask autosaveTask;

    public ProfileService(Plugin plugin, ConfigService config, DatabaseService databaseService,
                          TaskExecutor executor, MessageService messages) {
        this.plugin = plugin;
        this.config = config;
        this.databaseService = databaseService;
        this.executor = executor;
        this.messages = messages;
    }

    @Override
    public void enable() {
        repository = new SqlProfileRepository(databaseService.database(), codec);

        CacheLoader<String, Optional<PlayerProfile>> loader = name -> repository.findByName(name).map(PlayerProfile.class::cast);
        offline = Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterWrite(Duration.ofMinutes(1))
                .executor(executor.async())
                .buildAsync(loader);

        Bukkit.getPluginManager().registerEvents(new ProfileListener(this, messages, plugin), plugin);
        scheduleAutosave();

        // Supports /reload and plugin managers: load everyone already online.
        for (Player player : Bukkit.getOnlinePlayers()) {
            online.put(player.getUniqueId(), repository.findOrCreate(player.getUniqueId(), player.getName()).profile());
        }
    }

    @Override
    public void reload() {
        scheduleAutosave();
    }

    @Override
    public void disable() {
        if (autosaveTask != null) {
            autosaveTask.cancel();
        }
        // Runs on the server thread during shutdown: block until everything is persisted.
        List<ProfileSnapshot> snapshots = online.values().stream().map(p -> p.snapshot(codec)).toList();
        try {
            repository.saveAll(snapshots);
            plugin.getLogger().info("Saved " + snapshots.size() + " profile(s)");
        } catch (ProfileRepository.StorageException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save profiles on shutdown", e);
        }
        online.clear();
    }

    // ------------------------------------------------------------------
    // PlayerProfileService
    // ------------------------------------------------------------------

    @Override
    public Optional<PlayerProfile> get(UUID uuid) {
        return Optional.ofNullable(online.get(uuid));
    }

    @Override
    public PlayerProfile get(Player player) {
        ProfileData profile = online.get(player.getUniqueId());
        if (profile == null) {
            throw new IllegalStateException("No profile loaded for " + player.getName() + ": is the player online?");
        }
        return profile;
    }

    @Override
    public CompletableFuture<Optional<PlayerProfile>> lookup(String name) {
        Player player = Bukkit.getPlayerExact(name);
        if (player != null) {
            ProfileData profile = online.get(player.getUniqueId());
            if (profile != null) {
                return CompletableFuture.completedFuture(Optional.of(profile));
            }
        }
        return offline.get(key(name));
    }

    @Override
    public int loadedCount() {
        return online.size();
    }

    // ------------------------------------------------------------------
    // Hooks driven by ProfileListener
    // ------------------------------------------------------------------

    /** Async login thread: blocking is expected here. */
    void load(UUID uuid, String name) {
        offline.synchronous().invalidate(key(name));
        ProfileRepository.LoadResult result = repository.findOrCreate(uuid, name);
        online.put(uuid, result.profile());
        Bukkit.getPluginManager().callEvent(new PlayerProfileLoadEvent(result.profile(), result.created()));
    }

    /** The login was denied after loading (ban, whitelist, ...): drop without saving. */
    void discard(UUID uuid) {
        online.remove(uuid);
    }

    void join(Player player) {
        ProfileData profile = online.get(player.getUniqueId());
        if (profile != null) {
            profile.recordJoin(player.getName());
        }
    }

    void quit(Player player) {
        ProfileData profile = online.get(player.getUniqueId());
        if (profile == null) {
            return;
        }
        Bukkit.getPluginManager().callEvent(new PlayerProfileUnloadEvent(profile));
        online.remove(player.getUniqueId());
        profile.recordQuit();
        ProfileSnapshot snapshot = profile.snapshot(codec);
        executor.runAsync(() -> {
            repository.save(snapshot);
            // The database now holds newer data than anything cached while the player was online.
            offline.synchronous().invalidate(key(snapshot.name()));
        });
    }

    // ------------------------------------------------------------------

    private static String key(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    private void scheduleAutosave() {
        if (autosaveTask != null) {
            autosaveTask.cancel();
            autosaveTask = null;
        }
        long seconds = config.get(PluginConfig.AUTOSAVE_SECONDS);
        if (seconds > 0) {
            long ticks = seconds * 20L;
            autosaveTask = Bukkit.getScheduler().runTaskTimer(plugin, this::autosave, ticks, ticks);
        }
    }

    private void autosave() {
        List<ProfileSnapshot> dirty = new ArrayList<>();
        for (ProfileData profile : online.values()) {
            if (profile.isDirty()) {
                dirty.add(profile.snapshot(codec));
            }
        }
        if (dirty.isEmpty()) {
            return;
        }
        executor.runAsync(() -> {
            repository.saveAll(dirty);
            if (config.get(PluginConfig.DEBUG)) {
                plugin.getLogger().info("Autosaved " + dirty.size() + " profile(s)");
            }
        });
    }
}
