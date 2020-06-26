package com.example.authio.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

// a transcribed model for an HTTP response from the Web service
// contains 'response' (status), 'username', 'description', and 'email' at most in a JSON format
public class NetworkModel {

    @SerializedName("response") // indicates that the field should be serialized to JSON
    @Expose // indicates the field should be exposed for said JSON serialization
    private String response; // i.e. the field is part of a JSON format and the name inside is the field value (key)
    // the value of the JSON key (constructor argument) should be specified from the Web service used
    // and the response it sends from HTTP

    @SerializedName("username")
    @Expose
    private String username;

    @SerializedName("description")
    @Expose
    private String description;

    @SerializedName("email")
    @Expose
    private String email;

    public NetworkModel(String response, String username, String description, String email) {
        this.response = response;
        this.username = username;
        this.description = description;
        this.email = email;
    }

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
