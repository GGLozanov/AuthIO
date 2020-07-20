package com.example.authio.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Token {

    @SerializedName("jwt")
    @Expose
    String jwt;

    @SerializedName("response")
    @Expose
    String response;

    public String getJWT() {
        return jwt;
    }

    public String getResponse() {
        return response;
    }
}
