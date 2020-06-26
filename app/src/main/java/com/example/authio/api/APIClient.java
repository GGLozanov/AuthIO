package com.example.authio.api;

import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {
    private static String BASE_URL = "http://10.0.2.2/AuthIO-Service";
        // 10.0.2.2 is the default way for Android apps to connect to localhost
    private static Retrofit retrofit = null; // retrofit instance

    public static Retrofit getAPIClient() {
        if(retrofit == null) {
            retrofit = new Retrofit.Builder() // builder pattern
                    .baseUrl(BASE_URL) // add the base url
                    .addConverterFactory(GsonConverterFactory.create()) // add a converter for HTTP responses
                    .build(); // build the retrofit
        }
        return retrofit;
    }


}
