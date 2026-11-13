package it.buzz.template.storage.migration;

import java.sql.Statement;
import java.util.List;

/** The full, ordered list of schema migrations. Append new entries; never modify existing ones. */
public final class Migrations {

    public static final List<Migration> ALL = List.of(
            new Migration(1, "create players table", (connection, db) -> {
                String players = db.table("players");
                try (Statement st = connection.createStatement()) {
                    st.execute("""
                            CREATE TABLE %s (
                                uuid       CHAR(36)    NOT NULL PRIMARY KEY,
                                name       VARCHAR(16) NOT NULL,
                                data       TEXT        NOT NULL,
                                created_at BIGINT      NOT NULL,
                                updated_at BIGINT      NOT NULL
                            )""".formatted(players));
                    st.execute("CREATE INDEX idx_%1$s_name ON %1$s (name)".formatted(players));
                }
            })
    );

    private Migrations() {
    }
}
