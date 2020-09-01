package com.example.authio.repositories;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.authio.models.Token;
import com.example.authio.utils.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TokenRepository extends Repository<Token> { // designed to make an API call for auth user token
    private static TokenRepository instance; // singleton instance (singleton pattern per MVVM)

    public static TokenRepository getInstance() {
        if(instance == null) {
            instance = new TokenRepository();
        }

        return instance;
    }

    /**
     * Retrieves JWT on user register with email, username, password, and description
     * @param email - auth user's email
     * @param username - auth user's username
     * @param password - auth user's password
     * @param description - auth user's description
     * @return MutableLiveData instance with the retrieved JWT
     */
    public MutableLiveData<Token> getTokenOnRegister(String email, String username, String password, String description) {
        Log.i("TokenRepository", "getTokenOnRegister —> Calling for token result from register endpoint");

        Call<Token> authResult = API_OPERATIONS
                .performRegistration(
                        email,
                        username,
                        password,
                        description
                );

        final MutableLiveData<Token> mToken = new MutableLiveData<>();

        authResult.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                // handle application-level errors intended from HTTP response here...
                Token token;
                String responseStatus;

                if(response.isSuccessful() && (token = response.body()) != null &&
                        (responseStatus = token.getResponse()) != null && responseStatus.equals("ok")) {
                    Log.i("TokenRepository", "getTokenOnRegister —> Received token on register and setting LiveData value to new token");
                    mToken.setValue(token); // when returned livedata will still be null but observers will update after async call is finished
                    return;
                }

                if(response.code() == 406) {
                    mToken.setValue(Token.asFailed("Invalid credentials"));
                    return;
                }

                try {
                    responseStatus = NetworkUtils.
                            extractResponseFromResponseErrorBody(response, "response");
                } catch (JSONException | IOException | NetworkUtils.ResponseSuccessfulException e) {
                    Log.e("TokenRepository", "getTokenOnRegister —> TokenRepo JSON parse failed or response was successful. " + e.toString());
                    mToken.setValue(Token.asFailed("Failed to parse error response"));
                    return;
                }

                mToken.setValue(Token.asFailed(responseStatus));
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                // handle failed HTTP response receiving due to server-side exception here
                Log.e("TokenRepository", "getTokenOnRegister —> Server error on token retrieval on register");
                mToken.setValue(Token.asFailed(t.getMessage()));
            }
        });

        return mToken;
    }

    /**
     * Retrieves JWT on user login with email and password
     * @param email - auth user's email
     * @param password - auth user's password
     * @return - MutableLiveData instance with the retrieved JWT
     */
    public MutableLiveData<Token> getTokenOnLogin(String email, String password) {
        Log.i("TokenRepository", "getTokenOnLogin —> Calling for token result from login endpoint");

        Call<Token> authResult = API_OPERATIONS
                .performLogin(
                        email,
                        password
                );

        final MutableLiveData<Token> mToken = new MutableLiveData<>();
        authResult.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                Token token;
                String responseStatus;

                if(response.isSuccessful() && (token = response.body()) != null &&
                        (responseStatus = token.getResponse()) != null && responseStatus.equals("ok")) {
                    Log.i("TokenRepository", "getTokenOnLogin —> Received token on login and setting LiveData value to new token");
                    mToken.setValue(token);
                    return;
                }

                try {
                    responseStatus = NetworkUtils.
                            extractResponseFromResponseErrorBody(response, "response");
                } catch (JSONException | IOException | NetworkUtils.ResponseSuccessfulException e) {
                    Log.e("TokenRepository", "getTokenOnLogin —> TokenRepo JSON parse failed or response was successful. " + e.toString());
                    mToken.setValue(Token.asFailed("Failed to parse error response"));
                    return;
                }

                mToken.setValue(Token.asFailed(responseStatus));
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                // handle failed HTTP response receiving due to server-side exception here
                Log.e("TokenRepository", "Server error on token retrieval on login");
                mToken.setValue(Token.asFailed(t.getMessage()));
            }
        });

        return mToken;
    }
}
