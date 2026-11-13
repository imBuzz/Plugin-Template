package it.buzz.template.storage.dialect;

/** Remote MariaDB. Driver downloaded through plugin.yml {@code libraries}. */
public final class MariaDbDialect extends MySqlDialect {

    @Override
    public String driverClass() {
        return "org.mariadb.jdbc.Driver";
    }

    @Override
    protected String scheme() {
        return "mariadb";
    }
}
