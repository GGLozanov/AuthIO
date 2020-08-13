package com.example.authio.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Image extends Model {

    @SerializedName("title")
    @Expose
    private String title; // image title = user's id

    @SerializedName("image")
    @Expose
    private String image; // base64 image

    /**
     *
     * @param title - Image title. Used in the backend for storing the picture and correlating it to the user. Value is always user id.
     * @param response - Custom API status message received from call
     * @param image
     */
    public Image(String title, String response, String image) {
        super(response);
        this.title = title;
        this.image = image;
    }

    /**
     *
     * @param response - Custom API status message received from call
     */
    private Image(String response) {
        super(response);
    }

    public static Image asFailed(String response) { // couldn't use base class method due to casting issues; FIXME optimise this repetition for all models
        return new Image(response);
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

}
