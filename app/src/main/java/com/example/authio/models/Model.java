package com.example.authio.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Model { // not abstract due to usage in GSON deserialization instantiation

    @SerializedName("response") // indicates that the field should be serialized to JSON
    @Expose // indicates the field should be exposed for said JSON serialization
    protected String response; // i.e. the field is part of a JSON format and the name inside is the field value (key)
    // the value of the JSON key (constructor argument) should be specified from the Web service used
    // and the response it sends from HTTP

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Model() {}

    public Model(String response) {
        this.response = response;
    }

    public static Model asFailed(String response) {
        return new Model(response) {};
    }
}
