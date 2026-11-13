package it.buzz.template.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.UUID;

/** JSON (de)serialization of {@link ProfileData}. Register custom type adapters here. */
public final class ProfileCodec {

    private final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .create();

    public String encode(ProfileData data) {
        return gson.toJson(data);
    }

    public ProfileData decode(UUID uuid, String name, String json) {
        ProfileData data = gson.fromJson(json, ProfileData.class);
        return (data != null ? data : new ProfileData(uuid, name)).bind(uuid, name);
    }
}
