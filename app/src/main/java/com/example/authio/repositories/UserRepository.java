package com.example.authio.repositories;

import android.net.ConnectivityManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.authio.executors.AppExecutors;
import com.example.authio.fetchers.NetworkBoundModelFetcher;
import com.example.authio.models.Model;
import com.example.authio.models.Token;
import com.example.authio.models.User;
import com.example.authio.persistence.UserDao;
import com.example.authio.persistence.UserEntity;
import com.example.authio.shared.Constants;
import com.example.authio.utils.CacheUtils;
import com.example.authio.utils.NetworkUtils;
import com.example.authio.utils.PrefConfig;
import com.example.authio.utils.TokenUtils;
import com.example.authio.views.activities.MainActivity;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.jsonwebtoken.ExpiredJwtException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository extends CacheRepository<UserDao> { // designed to make an API call for one user's info
    private static UserRepository instance;

    private UserRepository(UserDao userDao, ConnectivityManager connectivityManager) {
        super(userDao, connectivityManager);
    }

    public static UserRepository getInstance(UserDao userDao, ConnectivityManager connectivityManager) {
        if(instance == null) {
            instance = new UserRepository(userDao, connectivityManager);
        }

        return instance;
    }

    // TODO: Try and find a way to generify handleFailedAuthorizedResponse while keeping recursive implementation w/ different methods

    /**
     * The below methods are used for fetching Users from the backend.
     * They don't receive JWTs as arguments but instead get them directly from sharedpreferences.
     * This is done to avoid any possible mismatches with the tokens used in simultaneous requests (race conditions)
     */

    /**
     *
     * @return - MutableliveData instance with the currently authenticated user
     */
    public MutableLiveData<User> getUser() {
        PrefConfig prefConfig; // separate variable without class scope because of activity-level context requirements
        if((prefConfig = MainActivity.PREF_CONFIG_REFERENCE.get()) != null) {
            return new NetworkBoundModelFetcher<User, UserEntity>(AppExecutors.getInstance()) {
                @Override
                public LiveData<User> fetchFromNetwork() {
                    Log.i("UserRepository", "getUser —> Calling for new user from get_user endpoint.");

                    final MutableLiveData<User> mUser = new MutableLiveData<>();

                    Call<User> userFetchResult = API_OPERATIONS
                            .getUser(prefConfig.readToken());

                    userFetchResult.enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            User user;

                            if(response.isSuccessful() && (user = response.body()) != null) {
                                Log.i("UserRepository", "getUser —> Retrieved current auth user and setting them to the LiveData instance + updating lastFetchTime.");

                                prefConfig.writeLastUserFetchTimeNow();

                                mUser.setValue(user);
                                return;
                            }

                            Log.w("UserRepository", "getUser —> Couldn't retrieve current auth user (response not successful or body is null). Handling failed user response");
                            // if the user is null, their token might have expired => get it back w/ refresh token or reauth
                            try {
                                NetworkUtils.handleFailedAuthorizedResponse(API_OPERATIONS, response, prefConfig.readRefreshToken(), new Callback<Token>() {
                                    @Override
                                    public void onResponse(Call<Token> call, Response<Token> response) {
                                        String jwtToken;
                                        if((jwtToken =  NetworkUtils.getTokenFromRefreshResponse(response)) != null) {
                                            Log.i("UserRepository", "getUser —> Retrieved new token from refresh_token endpoint. Saving token and retrying get_user request");
                                            prefConfig.writeToken(jwtToken);
                                            getUser();
                                        } else {
                                            // entering here means the refresh token has expired and/or can't be read
                                            Log.i("UserRepository", "getUser —> Retrieved new token from refresh_token endpoint but token is either invalid or expired. Reauth.");
                                            mUser.setValue(User.asFailed(Constants.REAUTH_FLAG)); // if message = "Reauth", just reauth
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Token> call, Throwable t) {
                                        Log.e("UserRepository", "getUser —> Server error on new token retrieval. Reauth + error message.");
                                        mUser.setValue(User.asFailed(Constants.FAILED_FLAG + t.getMessage())); // if message = "Failed", displayErrorAndReauth()
                                    }
                                });
                            } catch (JSONException | IOException | NetworkUtils.ResponseSuccessfulException e) {
                                Log.e("UserRepository", "getUser —> UserRepository JSON parse for failed user response failed or response was successful. " + e.toString());
                                mUser.setValue(User.asFailed(Constants.FAILED_FLAG + "Internal server error. Could not fetch response!"));
                            } catch (NetworkUtils.InvalidTokenException e) {
                                Log.e("UserRepository", "getUser —> Response status from failed user response was invalid (server-side response for invalid token or expiry). Reauth.");
                                mUser.setValue(User.asFailed(Constants.REAUTH_FLAG)); // if message = "Reauth", just reauth
                                // if the token hasn't expired but is corrupted, then just log them out
                            }
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable t) {
                            Log.e("UserRepository", "getUser —> Server error on auth user retrieval. Reauth + error message.");
                            mUser.setValue(User.asFailed(Constants.FAILED_FLAG + t.getMessage()));
                        }
                    });

                    return mUser;
                }

                @Override
                public boolean shouldFetchFromNetwork() {
                    PrefConfig prefConfig;
                    if((prefConfig = MainActivity.PREF_CONFIG_REFERENCE.get()) != null) {
                        // lastUserFetchTimekeeps track of the last time the networkBoundModelFetcher has completed its GetUser request successfully;
                        // it's used in shouldFetch()
                        long lastUserFetchTime = prefConfig.readLastUserFetchTime();
                        return lastUserFetchTime == 0 || ((System.currentTimeMillis() / 1000) - lastUserFetchTime) <= Constants.CACHE_TIMEOUT
                                || isConnected();
                        // i.e. if there has beeen no cache prior, the cache has expired, or the user is connected
                    }
                    Log.w("UserRepository", "getUser —> NetworkBoundFetcher —> shouldFetch —> No sharedprefs value found in references");
                    return false;
                }

                @Override
                public LiveData<UserEntity> loadFromDb() {
                    int authId = TokenUtils.getTokenUserIdFromStoredTokens(prefConfig); // prefconfig is injected either way; might as well do it in the util function
                    return authId != 0 ? dao.getUser(authId) : null;
                }

                @Override
                public void saveToDb(User fetchedModel) {
                    UserEntity userEntity;
                    if((userEntity = fetchedModel.getEntity()) != null) {
                        dao.insert(userEntity);
                    } else {
                        Log.w("UserRepository", "getUser —> Fetched result is null. Don't save in db.");
                    }
                }

                @Override
                public User entityToNetworkModel(UserEntity userEntity) {
                    return new User(userEntity);
                }

                @Override
                public boolean isNetworkModelInvalid(User user) {
                    String response = user.getResponse();
                    return response.equals(Constants.FAILED_RESPONSE) || response.equals(Constants.REAUTH_FLAG);
                }

                @Override
                public User getCriticalFailureModel() {
                    return User.asFailed(Constants.REAUTH_FLAG);
                }
            }.getAsLiveData();
        }

        Log.e("UserRepository", "getUser —> No sharedprefs reference found in MainActivity. Aborting any and all fetching operations");
        return null; // TODO: Update with User.asFailed() to trigger failure handlers
    }

    /**
     *
     * @return - MutableliveData instance with a map containing a relation IDs-User models (per JSON response)
     */
    public LiveData<List<User>> getUsers() {
        PrefConfig prefConfig;
        if((prefConfig = MainActivity.PREF_CONFIG_REFERENCE.get()) != null) { // TODO: maybe refactor to guard clause for better readibility?
            return new NetworkBoundModelFetcher<List<User>, List<UserEntity>>(AppExecutors.getInstance()) {
                @Override
                public LiveData<List<User>> fetchFromNetwork() {
                    Log.i("UserRepository", "getUsers —> Calling for all new users from get_users endpoint.");

                    Call<Map<String, User>> getUsersResult = API_OPERATIONS
                            .getUsers(prefConfig.readToken());

                    MutableLiveData<List<User>> mUsers = new MutableLiveData<>();
                    getUsersResult.enqueue(new Callback<Map<String, User>>() {
                        @Override
                        public void onResponse(Call<Map<String, User>> call, Response<Map<String, User>> response) {
                            Map<String, User> idUsers;
                            if(response.isSuccessful() && (idUsers = response.body()) != null) {
                                Log.i("UserRepository", "getUsers —> Retrieved users and setting them to the LiveData instance + updating lastFetchTime.");

                                prefConfig.writeLastUsersFetchTimeNow();

                                mUsers.setValue(new ArrayList<>(idUsers.values()));
                                return;
                            }

                            Log.w("UserRepository", "getUsers —> Couldn't retrieve other users (response not successful or body is null). Handling failed user response");

                            try {
                                NetworkUtils.handleFailedAuthorizedResponse(API_OPERATIONS, response, prefConfig.readRefreshToken(), new Callback<Token>() {
                                    @Override
                                    public void onResponse(Call<Token> call, Response<Token> response) {
                                        // refresh jwt is null here! (request doesn't contain it)
                                        String jwtToken;
                                        if((jwtToken = NetworkUtils.getTokenFromRefreshResponse(response)) != null) {
                                            Log.i("UserRepository", "getUsers —> Retrieved new token from refresh_token endpoint. Saving new token and retrying get_users request");
                                            prefConfig.writeToken(jwtToken);
                                            getUsers();
                                        } else {
                                            Log.i("UserRepository", "getUsers —> Retrieved new token from refresh_token endpoint but token is either invalid or expired. Reauth.");
                                            mUsers.setValue(new ArrayList<>(
                                                    Collections.singletonList(User.asFailed(Constants.REAUTH_FLAG))
                                            ));
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Token> call, Throwable t) {
                                        Log.e("UserRepository", "getUsers —> Server error on new token retrieval. Reauth + error message.");
                                        mUsers.setValue(new ArrayList<>(
                                                Collections.singletonList(User.asFailed(Constants.REAUTH_FLAG))
                                        ));
                                    }
                                });
                            } catch (JSONException | IOException | NetworkUtils.ResponseSuccessfulException e) {
                                Log.e("UserRepository", "getUsers —> UserRepository JSON parse for failed get_users response failed or response was successful. " + e.toString());
                                mUsers.setValue(new ArrayList<>(
                                        Collections.singletonList(User.asFailed(Constants.FAILED_FLAG + "Internal server error. Could not fetch response!"))
                                ));
                            } catch(NetworkUtils.InvalidTokenException e) {
                                Log.e("UserRepository", "getUsers —> Response status from failed user response was invalid (server-side response for invalid token or expiry). Reauth.");
                                mUsers.setValue(new ArrayList<>(
                                        Collections.singletonList(User.asFailed(Constants.REAUTH_FLAG))
                                ));
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, User>> call, Throwable t) {
                            Log.e("UserRepository", "getUsers —> Server error on other users retrieval. Reauth + error message." + t.getMessage());
                            mUsers.setValue(new ArrayList<>(
                                    Collections.singletonList(User.asFailed(Constants.REAUTH_FLAG))
                            ));
                        }
                    });

                    return mUsers;
                }

                @Override
                public boolean shouldFetchFromNetwork() {
                    PrefConfig prefConfig;
                    if((prefConfig = MainActivity.PREF_CONFIG_REFERENCE.get()) != null) {
                        // lastUsersFetchTime keeps track of the last time the networkBoundModelFetcher has completed its GetUsers request successfully;
                        // it's also used in shouldFetch()
                        long lastUsersFetchTime = prefConfig.readLastUsersFetchTime();
                        return lastUsersFetchTime == 0 || ((System.currentTimeMillis() / 1000) - lastUsersFetchTime) <= Constants.CACHE_TIMEOUT
                                || isConnected();
                    }
                    Log.w("UserRepository", "getUsers —> NetworkBoundFetcher —> shouldFetch —> No sharedprefs value found in references");
                    return false;
                }

                @Override
                public LiveData<List<UserEntity>> loadFromDb() {
                    int authId = TokenUtils.getTokenUserIdFromStoredTokens(prefConfig);
                    return authId != 0 ? dao.getUsers(authId) : null;
                }

                @Override
                public void saveToDb(List<User> fetchedModel) {
                    if(fetchedModel != null) {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            dao.insert(fetchedModel.stream()
                                    .filter(user -> !user.isInvalid()) // filter out only invalid users. . .
                                    .map(User::getEntity)
                                    .collect(Collectors.toList()));
                        } else { // cover both API levels. . .
                            List<UserEntity> userEntities = new ArrayList<>();
                            for(User user : fetchedModel) {
                                if(!user.isInvalid()) {
                                    userEntities.add(user.getEntity());
                                }
                            }
                            dao.insert(userEntities);
                        } // save only valid users (filter out invalid ones)
                    } else {
                        Log.w("UserRepository", "getUsers —> Fetched result is null. Don't save in db.");
                    }
                }

                @Override
                public List<User> entityToNetworkModel(List<UserEntity> userEntities) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        return userEntities.stream()
                                .map(User::new)
                                .collect(Collectors.toList());
                    } else { // cover both API levels. . .
                        List<User> users = new ArrayList<>();
                        for(UserEntity entity : userEntities) {
                            users.add(new User(entity));
                        }
                        return users;
                    }
                }

                @Override
                public boolean isNetworkModelInvalid(List<User> users) {
                    String firstUserResponse;
                    if(!users.isEmpty() && (firstUserResponse = users.get(0).getResponse()) != null) {
                        return firstUserResponse.equals(Constants.REAUTH_FLAG) ||
                                firstUserResponse.contains(Constants.FAILED_FLAG);
                    }

                    return true;
                }

                @Override
                public List<User> getCriticalFailureModel() {
                    return new ArrayList<>(
                            Collections.singletonList(User.asFailed(Constants.REAUTH_FLAG))
                    );
                }
            }.getAsLiveData();
        }

        Log.w("UserRepository", "getUsers —> No sharedprefs reference found in MainActivity. Aborting any and all fetch operations.");
        return null; // TODO: Update with User failed empty list to trigger failure handlers
    }

    /**
     *
     * @param body
     * @return
     */
    public MutableLiveData<Model> updateUser(Map<String, String> body) {
        PrefConfig prefConfig;
        if((prefConfig = MainActivity.PREF_CONFIG_REFERENCE.get()) != null) {
            Log.i("UserRepository", "updateUser —> Calling for user update from edit_user endpoint.");

            Call<Model> editUserResult = API_OPERATIONS.editUser(
                    prefConfig.readToken(),
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

                        int authId = TokenUtils.getTokenUserIdFromStoredTokens(prefConfig);
                        if(authId != 0) {
                            Log.i("UserRepository", "updateUser —> Updating user cache with new response.");
                            CacheUtils.updateCacheUserFromRequestMap(dao, authId, body);
                        } else { // shouldn't happen due to token always being recently fetched for auth fields (might happen for other fields)
                            Log.w("UserRepository", "updateUser —> Failed to update cache. Couldn't extract id from stored token => no knowledge of auth user whom to update");
                        }

                        return;
                    }

                    Log.w("UserRepository", "updateUser —> Couldn't update user (response not successful or body is null). Handling failed edit response");

                    try {
                        NetworkUtils.handleFailedAuthorizedResponse(API_OPERATIONS, response, prefConfig.readRefreshToken(), new Callback<Token>() {
                            @Override
                            public void onResponse(Call<Token> call, Response<Token> response) {
                                String jwtToken;
                                if((jwtToken = NetworkUtils.getTokenFromRefreshResponse(response)) != null) {
                                    Log.i("UserRepository", "updateUser —> Retrieved new token from refresh_token endpoint. Saving new token and retrying update_user request");
                                    prefConfig.writeToken(jwtToken);
                                    updateUser(body);
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

        Log.w("UserRepository", "updateUser —> No sharedprefs reference found in MainActivity. Aborting any and all fetch operations.");
        return null;
    }

    /**
     *
     */
    // TODO: Fix code repetition in functions with only Model returns
    public MutableLiveData<Model> deleteUser() {
        PrefConfig prefConfig;
        if((prefConfig = MainActivity.PREF_CONFIG_REFERENCE.get()) != null) {
            Call<Model> deleteUserResult = API_OPERATIONS.deleteUser(prefConfig.readToken());

            final MutableLiveData<Model> mModel = new MutableLiveData<>();
            deleteUserResult.enqueue(new Callback<Model>() {
                @Override
                public void onResponse(Call<Model> call, Response<Model> response) {
                    Model model;
                    if(response.isSuccessful() && (model = response.body()) != null) {
                        Log.i("UserRepository", "deleteUser —> Retrieved response from backend for user deletion and setting it to LiveData instance.");
                        mModel.setValue(model);
                        return;
                    }

                    Log.w("UserRepository", "deleteUser —> Couldn't delete user (response not successful or body is null). Handling failed delete response");

                    try {
                        NetworkUtils.handleFailedAuthorizedResponse(API_OPERATIONS, response, prefConfig.readRefreshToken(), new Callback<Token>() {
                            @Override
                            public void onResponse(Call<Token> call, Response<Token> response) {
                                String jwtToken;
                                if((jwtToken = NetworkUtils.getTokenFromRefreshResponse(response)) != null) {
                                    Log.i("UserRepository", "deleteUser —> Retrieved new token from refresh_token endpoint. Saving new token and retrying delete_user request");
                                    prefConfig.writeToken(jwtToken);
                                    deleteUser();
                                } else {
                                    Log.i("UserRepository", "deleteUser —> Retrieved new token from refresh_token endpoint but token is either invalid or expired. Reauth.");
                                    mModel.setValue(Model.asFailed(response.message()));
                                }
                            }

                            @Override
                            public void onFailure(Call<Token> call, Throwable t) {
                                Log.e("UserRepository", "deleteUser —> Server error on new token retrieval. Reauth + error message.");
                                mModel.setValue(Model.asFailed(t.getMessage()));
                            }
                        });
                    } catch (JSONException | IOException | NetworkUtils.ResponseSuccessfulException e) {
                        Log.e("UserRepository", "deleteUser —> UserRepository JSON parse for failed get_users response failed or response was successful. " + e.toString());
                    } catch(NetworkUtils.InvalidTokenException e) {
                        Log.e("UserRepository", "deleteUser —> Response status from failed user response was invalid (server-side response for invalid token or expiry). Reauth.");
                    } finally {
                        mModel.setValue(Model.asFailed(response.message()));
                    }
                }

                @Override
                public void onFailure(Call<Model> call, Throwable t) {
                    Log.e("UserRepository", "deleteUser —> Server error on delete user attempt. Reauth + error message.");
                    mModel.setValue(Model.asFailed(t.getMessage()));
                }
            });

            return mModel;
        }

        Log.w("UserRepository", "deleteUser —> No sharedprefs reference found in MainActivity. Aborting any and all fetch operations.");
        return null;
    }
}
