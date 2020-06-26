package com.example.authio.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface APIOperations {

    @GET("register.php")
    Call<NetworkModel> performRegistration(
            @Query("email") String email,
            @Query("username") String username,
            @Query("password") String password,
            @Query("description") String description
    );

    // Call is provided by Retrofit as a Java class for a direct HTTP response
    // (w/ generic type of our custom model)
    // Query annotation receives the value of the query parameter at the given script's URL

    @GET("login.php") // send a GET request to receive the result of this php script
    Call<NetworkModel> performLogin(
            @Query("email") String email,
            @Query("password") String password
    );

}
