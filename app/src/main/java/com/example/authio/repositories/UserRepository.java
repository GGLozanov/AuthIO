package com.example.authio.repositories;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.authio.models.Model;
import com.example.authio.models.Token;
import com.example.authio.models.User;
import com.example.authio.utils.NetworkUtils;
import com.example.authio.utils.PrefConfig;
import com.example.authio.views.activities.MainActivity;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository extends Repository<User> { // designed to make an API call for one user's info
    private static UserRepository instance;

    public static UserRepository getInstance() {
        if(instance == null) {
            instance = new UserRepository();
        }

        return instance;
    }

    private static class InvalidTokenException extends Exception {
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
     * @return
     * @throws JSONException - if there is no matching key in the JSON body for a given field
     * @throws IOException - if the errorBody cannot be converted to a string
     * @throws NetworkUtils.ResponseSuccessfulException - if the given response is actually successful instead of failed (no errorBody)
     * @throws InvalidTokenException - if the original token is invalid and not expired
     */
    private<T> void handleFailedUserResponse(Response<T> response,
                                            String refreshToken,
                                            Callback<Token> refreshTokenResponseCallback)
            throws JSONException, IOException, NetworkUtils.ResponseSuccessfulException, InvalidTokenException {
        String responseCode = NetworkUtils.
                extractResponseFromResponseErrorBody(response, "response");

        if(responseCode == null ||
                !responseCode.equals("Expired token. Get refresh token.")) {
            throw new InvalidTokenException();
        }

        Call<Token> tokenResult = API_OPERATIONS.refreshToken(
                refreshToken
        ); // fetch new token from refresh token

        tokenResult.enqueue(refreshTokenResponseCallback);
    }

    /**
     *
     * @param response - Token response containing new JWT token
     * @return - New JWT token if response is valid
     */
    private String getTokenFromRefreshResponse(Response<Token> response) {
        Token token;
        if(response.isSuccessful() &&
                (token = response.body()) != null) {
            String responseCode = token.getResponse();

            if(responseCode.equals("ok")) {
                String jwtToken = token.getJWT(); // new JWT

                PrefConfig prefConfig;

                // TODO: Try to find a way to extract sharedprefs from here and modularise app (this is an exception but try to change that)
                if((prefConfig = MainActivity.PREF_CONFIG_REFERENCE.get()) != null) {
                    prefConfig.writeToken(jwtToken); // refresh jwt is null here! (request doesn't contain it)
                } else {
                    Log.e("TokenRepo Refresh", "Couldn't access sharedpreferences from User Repository");
                    return null;
                }

                return jwtToken;
            }
        }

        return null;
    }

    /**
     *
     * @param token - initial user JWT (saved in sharedprefs usually)
     * @param refreshToken - refresh JWT used when initial JWT is expired
     * @return - MutableliveData instance with the currently authenticated user
     */
    public MutableLiveData<User> getUser(String token, String refreshToken) {
        Call<User> userFetchResult = API_OPERATIONS
                .getUser(token);

        final MutableLiveData<User> mUser = new MutableLiveData<>();
        userFetchResult.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User user;

                if(response.isSuccessful() && (user = response.body()) != null) {
                    mUser.setValue(user);
                    return;
                }

                // if the user is null, their token might have expired => get it back w/ refresh token or reauth
                try {
                    handleFailedUserResponse(response, refreshToken, new Callback<Token>() {
                        @Override
                        public void onResponse(Call<Token> call, Response<Token> response) {
                            String jwtToken;
                            if((jwtToken = getTokenFromRefreshResponse(response)) != null) {
                                getUser(jwtToken, refreshToken);
                            } else {
                                // entering here means the refresh token has expired and/or can't be read
                                mUser.setValue(User.asFailed("Reauth")); // if message = "Reauth", just reauth
                            }
                        }

                        @Override
                        public void onFailure(Call<Token> call, Throwable t) {
                            mUser.setValue(User.asFailed("Failed: " + t.getMessage())); // if message = "Failed", displayErrorAndReauth()
                        }
                    });
                } catch (JSONException | IOException | NetworkUtils.ResponseSuccessfulException e) {
                    Log.e("WelcomeFrag JSON parse", e.toString());
                    mUser.setValue(User.asFailed("Internal server error. Could not fetch reponse!"));
                } catch (InvalidTokenException e) {
                    mUser.setValue(User.asFailed("Reauth")); // if message = "Reauth", just reauth
                    // if the token hasn't expired but is corrupted, then just log them out
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                mUser.setValue(User.asFailed("Failed: " + t.getMessage()));
            }
        });

        return mUser;
    }

    /**
     *
     * @param token - initial user JWT (saved in sharedprefs usually)
     * @param refreshToken - refresh JWT used when initial JWT is expired
     * @return - MutableliveData instance with a map containing a relation IDs-User models (per JSON response)
     */
    public MutableLiveData<List<User>> getUsers(String token, String refreshToken) {
        Call<Map<String, User>> getUsersResult = API_OPERATIONS
                .getUsers(token);

        MutableLiveData<List<User>> mUsers = new MutableLiveData<>();
        getUsersResult.enqueue(new Callback<Map<String, User>>() {
            @Override
            public void onResponse(Call<Map<String, User>> call, Response<Map<String, User>> response) {
                Map<String, User> idUsers;
                if(response.isSuccessful() && (idUsers = response.body()) != null) {
                    mUsers.setValue(new ArrayList<>(idUsers.values()));
                    return;
                }

                try {
                    handleFailedUserResponse(response, refreshToken, new Callback<Token>() {
                        @Override
                        public void onResponse(Call<Token> call, Response<Token> response) {
                            String jwtToken;
                            if((jwtToken = getTokenFromRefreshResponse(response)) != null) {
                                getUsers(jwtToken, refreshToken);
                            } else {
                                mUsers.setValue(null);
                            }
                        }

                        @Override
                        public void onFailure(Call<Token> call, Throwable t) {
                            mUsers.setValue(null);
                        }
                    });
                } catch (JSONException | IOException | NetworkUtils.ResponseSuccessfulException | InvalidTokenException e) {
                    mUsers.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<Map<String, User>> call, Throwable t) {
                mUsers.setValue(null);
            }
        });

        return mUsers;
    }
}
