package com.example.authio.models;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Token {

    @SerializedName("jwt")
    @Expose
    String jwt;

    @SerializedName("refresh_jwt")
    @Expose
    @Nullable
    String refresh_jwt; // may be null (in refresh token request)

    @SerializedName("response")
    @Expose
    String response;

    @SerializedName("userId")
    @Expose
    @Nullable
    Integer userId; // may be null (in login & refresh token requests)

    public String getJWT() {
        return jwt;
    }

    public String getRefreshJWT() {
        return refresh_jwt;
    }

    public String getResponse() {
        return response;
    }

    public Integer getUserId() {
        return userId;
    }
}
