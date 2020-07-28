package com.example.authio.repositories;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.authio.models.Token;
import com.example.authio.models.User;
import com.example.authio.utils.NetworkUtils;
import com.example.authio.utils.PrefConfig;
import com.example.authio.views.activities.BaseActivity;
import com.example.authio.views.activities.MainActivity;

import org.json.JSONException;

import java.io.IOException;

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

    public MutableLiveData<User> getUser(String token, String refreshToken) {
        Call<User> userFetchResult = API_OPERATIONS.getUser(
                token
        );

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

                String responseCode;
                try {
                    responseCode = NetworkUtils.
                            extractResponseFromResponseErrorBody(response, "response");
                } catch (JSONException | IOException | NetworkUtils.ResponseSuccessfulException e) {
                    Log.e("WelcomeFrag JSON parse", e.toString());
                    // displayErrorAndReauth("Internal server error. Could not fetch response!");
                    mUser.setValue((User) User.asFailed("Internal server error. Could not fetch reponse!"));
                    return;
                }

                if(responseCode != null &&
                        responseCode.equals("Expired token. Get refresh token.")) {
                    Call<Token> tokenResult = API_OPERATIONS.refreshToken(
                            refreshToken
                    ); // fetch new token from refresh token

                    tokenResult.enqueue(new Callback<Token>() {
                        @Override
                        public void onResponse(Call<Token> call, Response<Token> response) {
                            Token token;
                            if(response.isSuccessful() &&
                                    (token = response.body()) != null) {
                                String responseCode = token.getResponse();

                                if(responseCode.equals("ok")) {
                                    String jwtToken = token.getJWT(); // new JWT

                                    PrefConfig prefConfig;

                                    // TODO: Try to find a way to extract sharedprefs from here and modularise app (this is an exception but try to change that)
                                    if((prefConfig = BaseActivity.PREF_CONFIG_REFERENCE.get()) != null) {
                                        prefConfig.writeToken(jwtToken); // refresh jwt is null here! (request doesn't contain it)
                                    } else {
                                        // TODO: Handle error
                                        Log.e("TokenRepo Refresh", "Couldn't access sharedpreferences from Token Repository");
                                        return;
                                    }

                                    getUser(jwtToken, refreshToken); // get user again with new token
                                }
                            } else {
                                // entering here means the refresh token has expired and/or can't be read
                                mUser.setValue((User) User.asFailed("Reauth")); // if message = "Reauth", just reauth
                            }
                        }

                        @Override
                        public void onFailure(Call<Token> call, Throwable t) {
                            mUser.setValue((User) User.asFailed("Failed: " + t.getMessage())); // if message = "Failed", displayErrorAndReauth()
                        }
                    });
                } else {
                    mUser.setValue((User) User.asFailed("Reauth")); // if message = "Reauth", just reauth

                    // if the token hasn't expired but is corrupted, then just log them out
                    // onAuthStateChanged.performAuthReset();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                mUser.setValue((User) User.asFailed("Failed: " + t.getMessage()));
            }
        });

        return mUser;
    }
}
