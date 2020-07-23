package com.example.authio.models;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// a transcribed model for an HTTP response from the Web service
// contains 'response' (status), 'username', 'description', and 'email' (at most) in a JSON format
public class User {

    @SerializedName("id")
    @Expose
    @Nullable
    private Integer id;

    @SerializedName("username")
    @Expose
    @Nullable
    private String username;

    @SerializedName("description")
    @Expose
    @Nullable
    private String description;

    @SerializedName("email")
    @Expose
    @Nullable
    private String email;

    @SerializedName("response") // indicates that the field should be serialized to JSON
    @Expose // indicates the field should be exposed for said JSON serialization
    private String response; // i.e. the field is part of a JSON format and the name inside is the field value (key)
    // the value of the JSON key (constructor argument) should be specified from the Web service used
    // and the response it sends from HTTP

    public User(Integer id, String response, String username, String description, String email) {
        this.id = id;
        this.response = response;
        this.username = username;
        this.description = description;
        this.email = email;
    }

    public Integer getId() { return id; }

    public String getResponse() {
        return response;
    }

    public String getUsername() {
        return username;
    }

    public String getDescription() {
        return description;
    }

    public String getEmail() {
        return email;
    }
}
