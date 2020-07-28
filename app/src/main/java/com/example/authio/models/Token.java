package com.example.authio.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Token extends Model {

    @SerializedName("jwt")
    @Expose
    @Nullable
    String jwt; // may be null (in failed register/login request)

    @SerializedName("refresh_jwt")
    @Expose
    @Nullable
    String refresh_jwt; // may be null (in refresh token request)

    @SerializedName("userId")
    @Expose
    @Nullable
    Integer userId; // may be null (in login & refresh token requests)

    /**
     *
     * @param jwt - JWT token received by successful authentication; used for accessing secure endpoints
     * @param response - Custom API status message received from call
     * @param refresh_jwt - Refresh JWT token with long expiry date used when access JWT has expired
     * @param userId - Newly authenticated user's id
     */
    public Token(@Nullable String jwt, String response, @Nullable String refresh_jwt, @Nullable Integer userId) {
        super(response);
        this.jwt = jwt;
        this.refresh_jwt = refresh_jwt;
        this.userId = userId;
    }

    /**
     *
     * @param response - Custom API status message received from call
     */
    private Token(String response) {
        super(response);
    }

    public static Token asFailed(String response) { // couldn't use base class method due to casting issues; FIXME optimise this repetition for all models
        return new Token(response);
    } // abstract this constructor call for semantic ease

    public String getJWT() {
        return jwt;
    }

    public String getRefreshJWT() {
        return refresh_jwt;
    }

    public Integer getUserId() {
        return userId;
    }
}
