package it.buzz.template.config.serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class DatabaseCredentials {

    private String host, user, password, database;
    private int port;

}
