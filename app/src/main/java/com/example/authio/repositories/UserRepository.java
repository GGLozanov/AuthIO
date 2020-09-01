package com.example.authio.repositories;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.authio.api.APIOperations;
import com.example.authio.models.Model;
import com.example.authio.models.Token;
import com.example.authio.models.User;
import com.example.authio.utils.NetworkUtils;
import com.example.authio.utils.PrefConfig;
import com.example.authio.views.activities.MainActivity;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    /**
     *
     * @param token - initial user JWT (saved in sharedprefs usually)
     * @param refreshToken - refresh JWT used when initial JWT is expired
     * @return - MutableliveData instance with the currently authenticated user
     */
    public MutableLiveData<User> getUser(String token, String refreshToken) {
        Log.i("UserRepository", "getUser —> Calling for new user from get_user endpoint.");

        Call<User> userFetchResult = API_OPERATIONS
                .getUser(token);

        final MutableLiveData<User> mUser = new MutableLiveData<>();
        userFetchResult.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User user;

                if(response.isSuccessful() && (user = response.body()) != null) {
                    Log.i("UserRepository", "getUser —> Retrieved current auth user and setting them to the LiveData instance.");
                    mUser.setValue(user);
                    return;
                }

                Log.w("UserRepository", "getUser —> Couldn't retrieve current auth user (response not successful or body is null). Handling failed user response");
                // if the user is null, their token might have expired => get it back w/ refresh token or reauth
                try {
                    NetworkUtils.handleFailedAuthorizedResponse(API_OPERATIONS, response, refreshToken, new Callback<Token>() {
                        @Override
                        public void onResponse(Call<Token> call, Response<Token> response) {
                            String jwtToken;
                            if((jwtToken =  NetworkUtils.getTokenFromRefreshResponse(response)) != null) {
                                Log.i("UserRepository", "getUser —> Retrieved new token from refresh_token endpoint and retrying get_user request");
                                getUser(jwtToken, refreshToken);
                            } else {
                                // entering here means the refresh token has expired and/or can't be read
                                Log.i("UserRepository", "getUser —> Retrieved new token from refresh_token endpoint but token is either invalid or expired. Reauth.");
                                mUser.setValue(User.asFailed("Reauth")); // if message = "Reauth", just reauth
                            }
                        }

                        @Override
                        public void onFailure(Call<Token> call, Throwable t) {
                            Log.e("UserRepository", "getUser —> Server error on new token retrieval. Reauth + error message.");
                            mUser.setValue(User.asFailed("Failed: " + t.getMessage())); // if message = "Failed", displayErrorAndReauth()
                        }
                    });
                } catch (JSONException | IOException | NetworkUtils.ResponseSuccessfulException e) {
                    Log.e("UserRepository", "getUser —> UserRepository JSON parse for failed user response failed or response was successful. " + e.toString());
                    mUser.setValue(User.asFailed("Failed: Internal server error. Could not fetch response!"));
                } catch (NetworkUtils.InvalidTokenException e) {
                    Log.e("UserRepository", "getUser —> Response status from failed user response was invalid (server-side response for invalid token or expiry). Reauth.");
                    mUser.setValue(User.asFailed("Reauth")); // if message = "Reauth", just reauth
                    // if the token hasn't expired but is corrupted, then just log them out
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("UserRepository", "getUser —> Server error on auth user retrieval. Reauth + error message.");
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
        Log.i("UserRepository", "getUsers —> Calling for all new users from get_users endpoint.");

        Call<Map<String, User>> getUsersResult = API_OPERATIONS
                .getUsers(token);

        MutableLiveData<List<User>> mUsers = new MutableLiveData<>();
        getUsersResult.enqueue(new Callback<Map<String, User>>() {
            @Override
            public void onResponse(Call<Map<String, User>> call, Response<Map<String, User>> response) {
                Map<String, User> idUsers;
                if(response.isSuccessful() && (idUsers = response.body()) != null) {
                    Log.i("UserRepository", "getUsers —> Retrieved users and setting them to the LiveData instance.");
                    mUsers.setValue(new ArrayList<>(idUsers.values()));
                    return;
                }

                Log.w("UserRepository", "getUsers —> Couldn't retrieve other users (response not successful or body is null). Handling failed user response");

                try {
                    NetworkUtils.handleFailedAuthorizedResponse(API_OPERATIONS, response, refreshToken, new Callback<Token>() {
                        @Override
                        public void onResponse(Call<Token> call, Response<Token> response) {
                            String jwtToken;
                            if((jwtToken = NetworkUtils.getTokenFromRefreshResponse(response)) != null) {
                                Log.i("UserRepository", "getUsers —> Retrieved new token from refresh_token endpoint and retrying get_users request");
                                getUsers(jwtToken, refreshToken);
                            } else {
                                Log.i("UserRepository", "getUsers —> Retrieved new token from refresh_token endpoint but token is either invalid or expired. Reauth.");
                                mUsers.setValue(new ArrayList<>(
                                        Collections.singletonList(User.asFailed("Reauth"))
                                ));
                            }
                        }

                        @Override
                        public void onFailure(Call<Token> call, Throwable t) {
                            Log.e("UserRepository", "getUsers —> Server error on new token retrieval. Reauth + error message.");
                            mUsers.setValue(new ArrayList<>(
                                    Collections.singletonList(User.asFailed("Reauth"))
                            ));
                        }
                    });
                } catch (JSONException | IOException | NetworkUtils.ResponseSuccessfulException e) {
                    Log.e("UserRepository", "getUsers —> UserRepository JSON parse for failed get_users response failed or response was successful. " + e.toString());
                    mUsers.setValue(new ArrayList<>(
                            Collections.singletonList(User.asFailed("Failed: Internal server error. Could not fetch response!"))
                    ));
                } catch(NetworkUtils.InvalidTokenException e) {
                    Log.e("UserRepository", "getUsers —> Response status from failed user response was invalid (server-side response for invalid token or expiry). Reauth.");
                    mUsers.setValue(new ArrayList<>(
                            Collections.singletonList(User.asFailed("Reauth"))
                    ));
                }
            }

            @Override
            public void onFailure(Call<Map<String, User>> call, Throwable t) {
                Log.e("UserRepository", "getUsers —> Server error on other users retrieval. Reauth + error message.");
                mUsers.setValue(new ArrayList<>(
                        Collections.singletonList(User.asFailed("Reauth"))
                ));
            }
        });

        return mUsers;
    }

    public MutableLiveData<Model> updateUser(String token, String refreshToken, Map<String, String> body) {
        Log.i("UserRepository", "updateUser —> Calling for user update from edit_user endpoint.");

        Call<Model> editUserResult = API_OPERATIONS.editUser(
                token,
                body
        );

        final MutableLiveData<Model> mModel = new MutableLiveData<>();
        editUserResult.enqueue(new Callback<Model>() {
            @Override
            public void onResponse(Call<Model> call, Response<Model> response) {
                Model model;
                if(response.isSuccessful() && (model = response.body()) != null) {
                    Log.i("UserRepository", "updateUser —> Retrieved response from backend for user update and setting it to LiveData instance.");
                    mModel.setValue(model);
                    return;
                }

                Log.w("UserRepository", "updateUser —> Couldn't update user (response not successful or body is null). Handling failed edit response");

                try {
                    NetworkUtils.handleFailedAuthorizedResponse(API_OPERATIONS, response, refreshToken, new Callback<Token>() {
                        @Override
                        public void onResponse(Call<Token> call, Response<Token> response) {
                            String jwtToken;
                            if((jwtToken = NetworkUtils.getTokenFromRefreshResponse(response)) != null) {
                                Log.i("UserRepository", "updateUser —> Retrieved new token from refresh_token endpoint and retrying get_users request");
                                updateUser(jwtToken, refreshToken, body);
                            } else {
                                Log.i("UserRepository", "updateUser —> Retrieved new token from refresh_token endpoint but token is either invalid or expired. Reauth.");
                                mModel.setValue(Model.asFailed(response.message()));
                            }
                        }

                        @Override
                        public void onFailure(Call<Token> call, Throwable t) {
                            Log.e("UserRepository", "updateUser —> Server error on new token retrieval. Reauth + error message.");
                            mModel.setValue(Model.asFailed(t.getMessage()));
                        }
                    });
                } catch (JSONException | IOException | NetworkUtils.ResponseSuccessfulException e) {
                    Log.e("UserRepository", "updateUser —> UserRepository JSON parse for failed get_users response failed or response was successful. " + e.toString());
                } catch(NetworkUtils.InvalidTokenException e) {
                    Log.e("UserRepository", "updateUser —> Response status from failed user response was invalid (server-side response for invalid token or expiry). Reauth.");
                } finally {
                    mModel.setValue(Model.asFailed(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Model> call, Throwable t) {
                Log.e("UserRepository", "updateUser —> Server error on edit user attempt. Reauth + error message.");
                mModel.setValue(Model.asFailed(t.getMessage()));
            }
        });

        return mModel;
    }

}
