package com.example.authio.adapters;

import com.example.authio.models.User;
import com.example.authio.persistence.BaseEntity;
import com.example.authio.persistence.UserEntity;
import com.example.authio.shared.Constants;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class UserTypeAdapter implements JsonDeserializer<User> {
    private JsonObject jsonObject;

    private enum DeserializeOption {
        AS_STRING, AS_INT
    }

    @Override
    public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        jsonObject = json.getAsJsonObject();

        UserEntity entity = new UserEntity(
                (int) deserializeJSONField(Constants.ID, DeserializeOption.AS_INT),
                (String) deserializeJSONField(Constants.USERNAME, DeserializeOption.AS_STRING),
                (String) deserializeJSONField(Constants.DESCRIPTION, DeserializeOption.AS_STRING),
                (String) deserializeJSONField(Constants.EMAIL, DeserializeOption.AS_STRING),
                (String) deserializeJSONField(Constants.PHOTO_URL, DeserializeOption.AS_STRING)
        );

        String response = (String) deserializeJSONField(Constants.RESPONSE, DeserializeOption.AS_STRING); // response may not always be contained in API return JSON (like in getUsers request)
        if(response == null) {
            response = Constants.SUCCESS_RESPONSE;
        }

        return new User(entity, response);
    }

    private Object deserializeJSONField(String field, DeserializeOption flag) {
        JsonElement jsonElement = jsonObject.get(field);
        if(jsonElement != null && !jsonElement.isJsonNull()) {
            // banal/naive/whatever way to do it but oh well. . .
            switch(flag) {
                case AS_INT:
                    return jsonElement.getAsInt();
                case AS_STRING:
                    return jsonElement.getAsString();
            }
        }
        return null;
    }
}
