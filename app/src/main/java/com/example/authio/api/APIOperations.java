package com.example.authio.api;

import com.example.authio.models.Model;
import com.example.authio.models.Token;
import com.example.authio.models.User;
import com.example.authio.shared.Constants;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface APIOperations {
    // TODO: Update requests (& backend) with header maps (Content-Type, etc.)

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
            @Field(Constants.EMAIL) String email,
            @Field(Constants.USERNAME) String username,
            @Field(Constants.PASSWORD) String password,
            @Field(Constants.DESCRIPTION) String description
    );

    /**
     * GET request to retrieve a new JWT upon requested login
     * @param email - auth user's email
     * @param password - auth user's password
     * @return - a Token model containing a JWT and refresh JWT on success and a failed response from the server on failure
     */
    @GET("api/auth/login.php")
    Call<Token> performLogin(
            @Query(Constants.EMAIL) String email,
            @Query(Constants.PASSWORD) String password
    );

    /**
     * GET request to retrieve a new token using the refresh token
     * @param refreshJWT - refresh JWT with a long expiry date used to retrieve new user JWTs
     * @return - a Token model containing a new JWT on success and a failed response from the server on failure
     */
    @GET("api/auth/refresh_token.php")
    Call<Token> refreshToken(
            @Header(Constants.AUTH_HEADER) String refreshJWT
    );

    /**
     * GET request to retrieve a single user and their info from the server
     * @param token - JWT for the given auth user used to validate requests to secure endpoints
     * @return - a User model containing all the necessary information and the appropriate response from server
     */
    @GET("api/service/user_info.php")
    Call<User> getUser(
            @Header(Constants.AUTH_HEADER) String token // username inside token for DB query; token inside auth header
    );

    /**
     * FormUrlEncoded POST request to upload a given image with a given title to the server (image's title is user's id garnered from token)
     * @param token - JWT for the given auth user used to validate requests to secure endpoints
     * @param image - the Base64 image
     * @return - a Model containing a response from the server based on success or failure
     */
    @POST("api/service/image.php")
    @FormUrlEncoded
    Call<Model> performImageUpload(
            @Header(Constants.AUTH_HEADER) String token, // title is user id (change to integer client and server side)
            @Field("image") String image
    );

    /**
     * GET request to fetch all other users apart from the authenticated (auth'd) one
     * @param token - JWT for the given auth user used to validate requests to secure endpoints (contains auth user's id)
     * @return - a map of "user" string (+ their id - i.e. "user1") and user models (no string response; if something goes awry, blame it on the user's connection)
     */
    @GET("api/service/get_users.php")
    Call<Map<String, User>> getUsers(
            @Header(Constants.AUTH_HEADER) String token
    );

    /**
     * POST request to update a given user with the params specified in the body
     * @param token - JWT for the given auth user used to validate requests to secure endpoints (contains auth user's id)
     * @param body - POST body fields containing key-value pairs on fields to be updated in the backend
     * @return - a Model containing a response from the server based on success or failure
     */
    @POST("api/service/edit_user.php") // should semantically be PATCH but w/e (for now) FIXME potentially in backend
    @FormUrlEncoded
    Call<Model> editUser(
            @Header(Constants.AUTH_HEADER) String token,
            @FieldMap Map<String, String> body // variable body parameters (which is why a map is used)
    );

    @DELETE("api/service/delete_user.php")
    Call<Model> deleteUser(
            @Header(Constants.AUTH_HEADER) String token
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
