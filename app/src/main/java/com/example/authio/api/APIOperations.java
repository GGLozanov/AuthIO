package com.example.authio.api;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface APIOperations {

    @GET("register.php")
    Call<UserModel> performRegistration(
            @Query("email") String email,
            @Query("username") String username,
            @Query("password") String password,
            @Query("description") String description
    );

    // Call is provided by Retrofit as a Java class for a direct HTTP response
    // (w/ generic type of our custom model)
    // Query annotation receives the value of the query parameter at the given script's URL

    @GET("login.php") // send a GET request to receive the result of this php script
    Call<UserModel> performLogin(
            @Query("email") String email,
            @Query("password") String password
    );


    @POST("image.php")
    @FormUrlEncoded
    Call<ImageModel> performImageUpload(
            @Field("title") String title, // title is user id (change to integer client and server side)
            @Field("image") String image
    );

    // Field annotation sends an encoded Form request in Retrofit and doesn't attach
    // the parameters to the URL (better security); used in POST requests
    // FormUrlEncoded specifies the request will use form URL encoding
    // (it encodes unreadable characters w/ percent - like in a base64 encoded image - and makes the URL readable for the Internet)
}
