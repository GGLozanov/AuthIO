package com.example.authio.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Image {

    @SerializedName("title")
    @Expose
    private String title; // image title = user's id

    @SerializedName("image")
    @Expose
    private String image; // base64 image

    @SerializedName("response")
    @Expose
    private String response; // response from server

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getResponse() {
        return response;
    }
}
