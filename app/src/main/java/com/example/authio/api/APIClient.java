package com.example.authio.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {
    private static final String BASE_URL = "http://78.90.172.159:8080/AuthIO-Service/";
        // localhost is replaced with default_img dev machine's WAN IP address for the wireless connection
        // change if hosted on a server or connection is changed
        // WAN IP allows connection from different networks (didn't work with LAN IP for other networks)
        // FireWall rules were also set up to allow connection from port 8080 (not default_img because it's disallowed from host ISP)
    private static Retrofit retrofit = null; // retrofit instance

    public static Retrofit getAPIClient() {
        if(retrofit == null) {
            retrofit = new Retrofit.Builder() // builder pattern
                    .baseUrl(BASE_URL) // add the base url
                    .addConverterFactory(
                        GsonConverterFactory.create(
                            new GsonBuilder().setLenient().create()
                        ) // set the builder to be more lenient for 'malformed' JSON
                    ) // add a converter for HTTP responses in JSON
                    .build(); // build the retrofit instance
        }
        return retrofit;
    }

    public static String getBaseURL() {
        return APIClient.BASE_URL;
    }
}
