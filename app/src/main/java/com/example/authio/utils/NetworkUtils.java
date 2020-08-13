package com.example.authio.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static Map<String, Object> extractFieldsFromResponseErrorBody(Response response, List<String> fields) throws JSONException, IOException, ResponseSuccessfulException {
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
    public static String extractResponseFromResponseErrorBody(Response response, String responseField) throws JSONException, IOException, ResponseSuccessfulException {
        return (String) extractFieldsFromResponseErrorBody(response, Collections.singletonList(responseField))
                .get(responseField);
    }
}
