package com.example.authio.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Image extends Model {

    @SerializedName("image")
    @Expose
    private String image; // base64 image

    /**
     *
     * @param response - Custom API status message received from call
     * @param image - base64 encoded image
     */
    public Image(String response, String image) {
        super(response);
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

    public String getImage() {
        return image;
    }

}
