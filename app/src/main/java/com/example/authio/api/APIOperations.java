package com.example.authio.api;

import com.example.authio.models.Model;
import com.example.authio.models.Token;
import com.example.authio.models.User;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface APIOperations {

    /**
     * FormUrlEncoded POST request to create a new User resource with given credentials
     * @param email - auth user's email address
     * @param username - auth user's username
     * @param password - auth user's password (hashed server-side)
     * @param description - auth user's description
     * @return - a Token model containing a JWT and refresh JWT on success and a failed response from the server on failure
     */
    @POST("api/auth/register.php")
    @FormUrlEncoded
    Call<Token> performRegistration(
            @Field("email") String email,
            @Field("username") String username,
            @Field("password") String password,
            @Field("description") String description
    );

    /**
     * GET request to retrieve a new JWT upon requested login
     * @param email - auth user's email
     * @param password - auth user's password
     * @return - a Token model containing a JWT and refresh JWT on success and a failed response from the server on failure
     */
    @GET("api/auth/login.php") // send a GET request to receive the result of this php script
    Call<Token> performLogin(
            @Query("email") String email,
            @Query("password") String password
    );

    /**
     * GET request to retrieve a new token using the refresh token
     * @param refreshJWT
     * @return - a Token model containing a new JWT on success and a failed response from the server on failure
     */
    @GET("api/auth/refresh_token.php")
    Call<Token> refreshToken(
            @Header("Authorization") String refreshJWT
    );

    /**
     * GET request to retrieve a single user and their info from the server
     * @param token - JWT for the given auth user used to validate requests to secure endpoints
     * @return - a User model containing all the necessary information and the appropriate response from server
     */
    @GET("api/service/user_info.php")
    Call<User> getUser(
            @Header("Authorization") String token // username inside token for DB query; token inside auth header
    );

    /**
     * FormUrlEncoded POST request to upload a given image with a given title to the server
     * @param title - image's title as a string
     * @param image - the Base64 image
     * @return - a Model containing a response from the server based on success or failure
     */
    @POST("api/service/image.php")
    @FormUrlEncoded
    Call<Model> performImageUpload(
            @Field("title") String title, // title is user id (change to integer client and server side)
            @Field("image") String image
    );

    /**
     * GET request to fetch all other users apart from the authenticated (auth'd) one
     * @param token - JWT for the given auth user used to validate requests to secure endpoints
     * @param id - auth'd user's id (used to exclude from DB fetch)
     * @return - a map of string ids and user models (no string response; if something goes awry, blame it on the user's connection)
     */
    @GET("api/service/get_users.php")
    Call<Map<String, User>> getUsers(
            @Header("Authorization") String token,
            @Query("auth_id") int id
    );

    /*
     * Dev notes:
     * Call is provided by Retrofit as a Java class for a direct HTTP response
     * (w/ generic type of our custom model)
     * {@link Query @Query} annotation receives the value of the query parameter at the given script's URL
     * Field annotation sends an encoded Form request in Retrofit and doesn't attach
     * the parameters to the URL (better security); used in POST requests
     * FormUrlEncoded specifies the request will use form URL encoding
     * (it encodes unreadable characters w/ percent - like in a base64 encoded image - and makes the URL readable for the Internet)
     */
}
