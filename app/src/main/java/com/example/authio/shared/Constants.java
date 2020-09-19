package com.example.authio.shared;

public class Constants {
    public static final long CACHE_TIMEOUT = 60 * 30; // 30 minutes

    public static final String SUCCESS_RESPONSE = "ok";
    public static final String FAILED_RESPONSE = "failed";
    public static final String IMAGE_UPLOAD_SUCCESS_RESPONSE = "Image Uploaded";
    public static final String IMAGE_UPLOAD_FAILED_RESPONSE = "Image Upload Failed";
    public static final String EXISTS_RESPONSE = "exists";
    public static final String REAUTH_FLAG = "Reauth";
    public static final String FAILED_FLAG = "Failed: ";

    // enum would've been more concise here but annotations can't have enums call toString()
    public static final String ID = "id";
    public static final String RESPONSE = "response";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String USERNAME = "username";
    public static final String DESCRIPTION = "description";
    public static final String PHOTO_URL = "photo_url";

    public static final String AUTH_HEADER = "Authorization";
}
