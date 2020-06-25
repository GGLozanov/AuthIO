package com.example.authio.api;

import com.google.gson.annotations.SerializedName;

// a transcribed model for an HTTP response from the Web service
// contains 'status' and 'username' at most in a JSON format
public class NetworkModel {

    @SerializedName("response") // indicates that the field should be serialized to JSON
    private String response; // i.e. the field is part of a JSON format and the name inside is the field value (key)
    // the value of the JSON key (constructor argument) should be specified from the Web service used
    // and the response it sends from HTTP

    @SerializedName("username")
    private String username;


    @SerializedName("description")
    private String description;


    public String getResponse() {
        return response;
    }

    public String getUsername() {
        return username;
    }

    public String getDescription() {
        return description;
    }
}
