package it.buzz.template.storage.database;

import it.buzz.template.storage.dialect.H2Dialect;
import it.buzz.template.storage.dialect.MariaDbDialect;
import it.buzz.template.storage.dialect.MySqlDialect;
import it.buzz.template.storage.dialect.SqlDialect;
import it.buzz.template.storage.dialect.SqliteDialect;

/** Values accepted by {@code database.type} in config.yml, each backed by a {@link SqlDialect}. */
public enum DatabaseType {

    SQLITE(new SqliteDialect()),
    H2(new H2Dialect()),
    MYSQL(new MySqlDialect()),
    MARIADB(new MariaDbDialect());

    private final SqlDialect dialect;

    DatabaseType(SqlDialect dialect) {
        this.dialect = dialect;
    }

    public SqlDialect dialect() {
        return dialect;
    }
}
