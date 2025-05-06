package it.buzz.template.manager.database.loader;

import it.buzz.template.manager.database.DatabaseManager;
import it.buzz.template.manager.player.TemplatePlayerData;
import it.ytnoos.loadit.api.DataLoader;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class PlayerDataLoader implements DataLoader<TemplatePlayerData> {

    private final DatabaseManager databaseManager;

    @Override
    public Optional<TemplatePlayerData> getOrCreate(UUID uuid, String name) {
        boolean result = databaseManager.existsPlayer(uuid);
        if (!result)
            databaseManager.createPlayer(uuid, name);

        return databaseManager.retrievePlayer(uuid);
    }

    @Override
    public Optional<TemplatePlayerData> load(UUID uuid) {
        return databaseManager.retrievePlayer(uuid);
    }

    @Override
    public Optional<TemplatePlayerData> load(String name) {
        UUID uuid = databaseManager.getPlayerUUID(name);
        if (uuid == null) return Optional.empty();

        return load(uuid);
    }

}
