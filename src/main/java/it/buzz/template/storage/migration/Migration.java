package it.buzz.template.storage.migration;

import it.buzz.template.storage.database.Database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * One forward-only schema change. Never edit a migration that has shipped: add a new one with a higher version.
 *
 * @param version     strictly increasing, starting from 1
 * @param description human readable summary, logged when applied
 * @param action      the change itself, executed inside a transaction
 */
public record Migration(int version, String description, Action action) {

    @FunctionalInterface
    public interface Action {
        void apply(Connection connection, Database database) throws SQLException;
    }
}
