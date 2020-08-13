package com.example.authio.api;

import com.example.authio.models.Image;
import com.example.authio.models.Model;
import com.example.authio.models.Token;
import com.example.authio.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface APIOperations {

    @POST("api/auth/register.php")
    @FormUrlEncoded
    Call<Token> performRegistration(
            @Field("email") String email,
            @Field("username") String username,
            @Field("password") String password,
            @Field("description") String description
    ); // returns jwt on successful register

    // Call is provided by Retrofit as a Java class for a direct HTTP response
    // (w/ generic type of our custom model)
    // Query annotation receives the value of the query parameter at the given script's URL

    @GET("api/auth/login.php") // send a GET request to receive the result of this php script
    Call<Token> performLogin(
            @Query("email") String email,
            @Query("password") String password
    );

    @GET("api/auth/refresh_token.php")
    Call<Token> refreshToken(
            @Header("Authorization") String refreshJWT
    );

    @GET("api/service/user_info.php")
    Call<User> getUser(
            @Header("Authorization") String token // username inside token for DB query; token inside auth header
    );


    @POST("api/service/image.php")
    @FormUrlEncoded
    Call<Model> performImageUpload(
            @Field("title") String title, // title is user id (change to integer client and server side)
            @Field("image") String image
    );

    // Field annotation sends an encoded Form request in Retrofit and doesn't attach
    // the parameters to the URL (better security); used in POST requests
    // FormUrlEncoded specifies the request will use form URL encoding
    // (it encodes unreadable characters w/ percent - like in a base64 encoded image - and makes the URL readable for the Internet)
}
