package it.buzz.template.manager.database;

import it.buzz.template.TemplatePlugin;
import it.buzz.template.config.PluginConfiguration;
import it.buzz.template.manager.database.connector.SQLConnector;
import it.buzz.template.manager.database.loader.PlayerDataLoader;
import it.buzz.template.manager.AbstractManager;
import it.buzz.template.manager.player.TemplatePlayerData;
import it.ytnoos.loadit.Loadit;
import it.ytnoos.loadit.api.LoaditLoadListener;
import it.ytnoos.loadit.api.RemovalReason;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager extends AbstractManager {

    private final static String PLAYER_TABLE = "weekend_boost_data";

    private final SQLConnector connector;
    @Getter private final Loadit<TemplatePlayerData> dataLoadit;

    public DatabaseManager(TemplatePlugin plugin) {
        super(plugin);

        connector = new SQLConnector(plugin.getSettingsManager().getProperty(PluginConfiguration.DATABASE_CREDENTIALS));
        dataLoadit = Loadit.createInstance(plugin, new PlayerDataLoader(this));
        dataLoadit.addListener(new LoaditLoadListener<>() {
            @Override
            public void onUnload(TemplatePlayerData userData, RemovalReason reason) {
                if (reason == RemovalReason.FAILED)
                    return;

                updatePlayer(userData);
            }
        });
    }

    @Override
    public void start() {
        try {
            try (Connection connection = connector.getHikari().getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         """
                            CREATE TABLE IF NOT EXISTS %table%
                            (
                                uuid VARCHAR(36) PRIMARY KEY,
                                name VARCHAR(16),
                                data JSON NOT NULL
                            );
                            """.replace("%table%", PLAYER_TABLE)
                 )){

                statement.execute();
            }
            catch (Exception e){
                plugin.getLogger().log(Level.SEVERE, "Could not load MYSQL Table", e);
                Bukkit.getPluginManager().disablePlugin(plugin);

                return;
            }

            dataLoadit.init();
        }
        catch (Exception e){
            plugin.getLogger().log(Level.SEVERE, "Could not load MYSQL Table", e);
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    @Override
    public void stop() {
        dataLoadit.stop();
        connector.getHikari().close();
    }

    public void updatePlayer(TemplatePlayerData data){
        final String update = """
            UPDATE %table%
            SET data = CAST(? AS JSON)
            WHERE uuid = ?;
            """.replace("%table%", PLAYER_TABLE);

        try (Connection connection = connector.getHikari().getConnection(); PreparedStatement stmt = connection.prepareStatement(update)) {
            stmt.setString(1, TemplatePlugin.GSON.toJson(data.getData()));
            stmt.setString(2, data.getUUID().toString());
            stmt.executeUpdate();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Optional<TemplatePlayerData> retrievePlayer(UUID uuid) {
        try (Connection connection = connector.getHikari().getConnection(); PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + PLAYER_TABLE + " WHERE uuid = '" + uuid + "'");
             ResultSet resultSet = statement.executeQuery()){
            while (resultSet.next()) {
                return Optional.of(new TemplatePlayerData(uuid, resultSet.getString("name"), TemplatePlugin.GSON.fromJson(resultSet.getString("data"), TemplatePlayerData.Data.class)));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public void createPlayer(UUID uuid, String name) {
        String sql = "INSERT INTO " + PLAYER_TABLE + " (uuid, name) VALUES (?, ?);";

        try (Connection connection = connector.getHikari().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public UUID getPlayerUUID(String playerName) {
        try (Connection conn = connector.getHikari().getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                                    SELECT uuid
                                    FROM   %s
                                    WHERE  name = ?;
                                    """.formatted(PLAYER_TABLE))
        ) {

            ps.setString(1, playerName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return UUID.fromString(rs.getString("uuid"));
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean existsPlayer(UUID uuid) {
        String sql = """
        SELECT EXISTS(
          SELECT 1
          FROM   %s
          WHERE  uuid = ?
        ) AS has_uuid;
        """.formatted(PLAYER_TABLE);

        try (Connection conn = connector.getHikari().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("has_uuid") == 1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
