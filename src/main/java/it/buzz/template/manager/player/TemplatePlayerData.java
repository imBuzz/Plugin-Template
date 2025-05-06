package it.buzz.template.manager.player;

import it.ytnoos.loadit.api.UserData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TemplatePlayerData extends UserData {

    @Getter private final Data data;

    public TemplatePlayerData(@NotNull UUID uuid, @NotNull String name) {
        super(uuid, name);
        this.data = new Data();
    }

    public TemplatePlayerData(@NotNull UUID uuid, @NotNull String name, Data data) {
        super(uuid, name);
        this.data = data;
    }

    @AllArgsConstructor @lombok.Data
    public static class Data {

    }

}
