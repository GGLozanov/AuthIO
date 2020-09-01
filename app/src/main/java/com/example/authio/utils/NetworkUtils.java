package com.example.authio.utils;

import android.util.Log;

import com.example.authio.api.APIOperations;
import com.example.authio.models.Token;
import com.example.authio.views.activities.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetworkUtils {
    public static class ResponseSuccessfulException extends Exception {
        public ResponseSuccessfulException() {
            super();
        }
    }

    /**
     *
     * @param response - Retrofit2 response received from server upon API request
     * @param fields - desired fields from which to decode from the JSON body (JSON keys)
     * @return Map<String, Object> result - a fully decoded JSON response if all fields match all keys in the given JSON response
     * @throws JSONException - if there is no matching key in the JSON body for a given field
     * @throws IOException - if the errorBody cannot be converted to a string
     * @throws ResponseSuccessfulException - if the given response is actually successful instead of failed (no errorBody)
     */
    public static<T> Map<String, Object> extractFieldsFromResponseErrorBody(Response<T> response, List<String> fields)
            throws JSONException, IOException, ResponseSuccessfulException {
        if(response.isSuccessful()) {
            throw new ResponseSuccessfulException();
        }

        JSONObject failedResponseBody; // body of request as JSON (used for extraction)

        Map<String, Object> result = new HashMap<>();

        failedResponseBody = new JSONObject(response.errorBody().string());

        for(String field : fields) {
            result.put(field, failedResponseBody.get(field));
        }

        return result;
    }

    /**
     *
     * @param response - Retrofit2 response received from server upon API request
     * @param responseField - field containing the desired custom API response as a JSON field
     * @return - String. Custom response message from API.
     * @throws JSONException - if there is no matching key in the JSON body for a given field
     * @throws IOException - if the errorBody cannot be converted to a string
     * @throws ResponseSuccessfulException - if the given response is actually successful instead of failed (no errorBody)
     */
    public static<T> String extractResponseFromResponseErrorBody(Response<T> response, String responseField)
            throws JSONException, IOException, ResponseSuccessfulException {
        Log.w("NetworkError", "Extracting response body from response '" + response.message() + "'");
        return (String) extractFieldsFromResponseErrorBody(response, Collections.singletonList(responseField))
                .get(responseField);
    }

    public static class InvalidTokenException extends Exception {
        public InvalidTokenException() {
            super();
        }

        public InvalidTokenException(String message) {
            super(message);
        }
    }

    /**
     *
     * @param response - Failed response
     * @param refreshToken - Refresh token used for refresh token fetch endpoint
     * @param refreshTokenResponseCallback - Retrofit callback for refresh token endpoint response
     * @param <T> - generic response type
     * @return - void
     * @throws JSONException - if there is no matching key in the JSON body for a given field
     * @throws IOException - if the errorBody cannot be converted to a string
     * @throws NetworkUtils.ResponseSuccessfulException - if the given response is actually successful instead of failed (no errorBody)
     * @throws InvalidTokenException - if the original token is invalid and not expired
     */
    public static<T> void handleFailedAuthorizedResponse(APIOperations apiOperations, Response<T> response,
                                                   String refreshToken,
                                                   Callback<Token> refreshTokenResponseCallback)
            throws JSONException, IOException, NetworkUtils.ResponseSuccessfulException, InvalidTokenException {
        String responseCode = NetworkUtils.
                extractResponseFromResponseErrorBody(response, "response");

        if(responseCode == null ||
                !responseCode.equals("Expired token. Get refresh token.")) {
            Log.e("Repository", "handleFailedAuthorizedResponse —> Invalid token error message in failed response body or error message is null");
            throw new InvalidTokenException();
        }

        Log.i("Repository", "handleFailedAuthorizedResponse —> Calling for new token result from refresh token endpoint");
        Call<Token> tokenResult = apiOperations.refreshToken(
                refreshToken
        ); // fetch new token from refresh token

        tokenResult.enqueue(refreshTokenResponseCallback);
    }

    /**
     *
     * @param response - Token response containing new JWT token
     * @return - New JWT token if response is valid
     */
    public static String getTokenFromRefreshResponse(Response<Token> response) {
        Token token;
        if(response.isSuccessful() &&
                (token = response.body()) != null) {
            String responseCode = token.getResponse();
            Log.i("Repository", "getTokenFromRefreshResponse —> Retrieved new token from refresh_token endpoint.");

            if(responseCode.equals("ok")) {
                Log.i("Repository", "getTokenFromRefreshResponse —> New token from refresh_token endpoint is valid ('ok' status).");

                String jwtToken = token.getJWT(); // new JWT

                PrefConfig prefConfig;

                // TODO: Try to find a way to extract sharedprefs from here and modularise app (this is an exception but try to change that)
                if((prefConfig = MainActivity.PREF_CONFIG_REFERENCE.get()) != null) {
                    prefConfig.writeToken(jwtToken); // refresh jwt is null here! (request doesn't contain it)
                } else {
                    Log.e("UserRepository", "getTokenFromRefreshResponse —> Couldn't access sharedpreferences from UserRepository");
                    return null;
                }

                Log.i("UserRepository", "getTokenFromRefreshResponse —> New token from refresh_token endpoint is saved into sharedprefrences.");

                return jwtToken;
            }
        }

        Log.e("UserRepository", "getTokenFromRefreshResponse —> refresh_token endpoint response unsuccessful or body is null. Returning null.");

        return null;
    }
}
