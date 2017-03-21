package com.elbloquecadena.conversion;

import java.lang.reflect.Type;
import java.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public final class JSON {

    private static final Gson gson;

    static {
        gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().registerTypeAdapter(byte[].class, new ByteArraySerializer())
                // .registerTypeAdapter(byte[].class,
                // (JsonSerializer<byte[]>) (bytes, typeOfT, context) -> new JsonPrimitive(Base64.getEncoder().encodeToString(bytes)))
                // .registerTypeAdapter(byte[].class,
                // (JsonDeserializer<byte[]>) (json, typeOfT, context) -> Base64.getDecoder().decode(json.getAsString()))
                .create();
    }

    public static String toJSON(Object o) {
        return gson.toJson(o);
    }

    public static <K> K fromJSON(String s, Class<K> c) {
        return gson.fromJson(s, c);
    }

    public static class ByteArraySerializer implements JsonDeserializer<byte[]>, JsonSerializer<byte[]> {

        @Override
        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Base64.getDecoder().decode(json.getAsString());
        }

        @Override
        public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(Base64.getEncoder().encodeToString(src));
        }
    }

}
