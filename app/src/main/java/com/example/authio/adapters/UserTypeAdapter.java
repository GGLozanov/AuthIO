package com.example.authio.api;

import com.example.authio.models.User;
import com.example.authio.persistence.UserEntity;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class UserTypeAdapter implements JsonDeserializer<User> {
    @Override
    public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        String response = jsonObject.get("response").getAsString();

        UserEntity entity = new UserEntity(
                jsonObject.get("id").getAsInt(),
                jsonObject.get("username").getAsString(),
                jsonObject.get("description").getAsString(),
                jsonObject.get("email").getAsString(),
                jsonObject.get("photo_url").getAsString()
        );

        return new User(entity, response);
    }
}
