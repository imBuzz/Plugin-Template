package it.buzz.template.manager.database.adapter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import it.buzz.template.manager.player.TemplatePlayerData;

import java.io.IOException;

public class TemplatePlayerDataAdapterFactory implements TypeAdapterFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        if (!TemplatePlayerData.Data.class.isAssignableFrom(typeToken.getRawType())) {
            return null;
        }

        return (TypeAdapter<T>) new TypeAdapter<TemplatePlayerData.Data>() {
            @Override
            public void write(JsonWriter out, TemplatePlayerData.Data data) throws IOException {
                if (data == null) {
                    out.nullValue();
                    return;
                }

                out.nullValue();
            }

            @Override
            public TemplatePlayerData.Data read(JsonReader in) throws IOException {
                if (in.peek() == JsonToken.NULL) {
                    return new TemplatePlayerData.Data();
                }


                return new TemplatePlayerData.Data();
            }
        };
    }
}
