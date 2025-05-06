package it.buzz.template.manager.database.connector;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import it.buzz.template.config.serializable.DatabaseCredentials;
import lombok.Getter;

public class SQLConnector {

    @Getter private final HikariDataSource hikari;

    public SQLConnector(DatabaseCredentials credentials) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(
                "jdbc:mysql://%host%:%port%/%database%"
                        .replace("%host%", credentials.getHost())
                        .replace("%port%", String.valueOf(credentials.getPort()))
                        .replace("%database%", credentials.getDatabase())
        );

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        config.setUsername(credentials.getUser());
        config.setPassword(credentials.getPassword());

        this.hikari = new HikariDataSource(config);
    }

}
