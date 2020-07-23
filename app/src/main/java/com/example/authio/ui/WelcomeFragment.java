package com.example.authio.ui;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.authio.R;
import com.example.authio.activities.MainActivity;
import com.example.authio.api.APIClient;
import com.example.authio.api.OnAuthStateChanged;
import com.example.authio.models.Token;
import com.example.authio.models.User;
import com.example.authio.utils.ImageDownloader;
import com.example.authio.utils.NetworkUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class WelcomeFragment extends Fragment {

    private TextView emailText, usernameText, descriptionText;
    private ImageView profileImage;
    private Button logoutButton;

    private OnAuthStateChanged onAuthStateChanged; // listener for performing logout

    private User user; // current fetched user

    public WelcomeFragment(User user) {
        this.user = user;
    }

    public WelcomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);

        emailText = view.findViewById(R.id.email_text);
        usernameText = view.findViewById(R.id.username_text);
        descriptionText = view.findViewById(R.id.description_text);

        profileImage = view.findViewById(R.id.profile_image);

        logoutButton = view.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener((v) -> onAuthStateChanged.performAuthReset());

        // TODO: Extract into other class/function (this is why you have architecture! ViewModels...)

        if(user == null) {
            fetchUser();
        } else {
            // need to call here again & in else because of potential fallthrough from background thread
            setProfileImageSource();
            setTextSources();
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        onAuthStateChanged = (OnAuthStateChanged) activity;
    }

    private void setTextSources() {
        // TODO: Replace with API call if user is null or pass user from Login to fragment
        emailText.setText("E-mail: " + user.getEmail());
        usernameText.setText("Welcome, " + user.getUsername());
        descriptionText.setText(user.getDescription());
    }

    private void setProfileImageSource() {
        new ImageDownloader(profileImage).execute(APIClient.getBaseURL() +
                "uploads/" +
                user.getId() +
                ".jpg"); // start AsyncTask to asynchronously download and render image upon completion
    }

    private void fetchUser() {
        // TODO: Optimise this abomination and shorten w/ error handling functions

        Call<User> userFetchResult = MainActivity.API_OPERATIONS.getUser(
                MainActivity.PREF_CONFIG.readToken()
        );

        userFetchResult.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                if(response.isSuccessful() && (user = response.body()) != null) {
                    setProfileImageSource();
                    setTextSources();
                    return;
                }

                // if the user is null, their token might have expired => get it back w/ refresh token or reauth

                String responseCode;
                try {
                    responseCode = NetworkUtils.
                            extractResponseFromResponseErrorBody(response, "response");
                } catch (JSONException | IOException | NetworkUtils.ResponseSuccessfulException e) {
                    Log.e("WelcomeFrag JSON parse", e.toString());
                    displayErrorAndReauth("Internal server error. Could not fetch response!");
                    return;
                }

                if(responseCode != null &&
                        responseCode.equals("Expired token. Get refresh token.")) {
                    Call<Token> tokenResult = MainActivity.API_OPERATIONS.refreshToken(
                            MainActivity.PREF_CONFIG.readRefreshToken()
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

                                    MainActivity.PREF_CONFIG.writeToken(jwtToken); // refresh jwt is null here! (request doesn't contain it)

                                    Call<User> userFetchResult = MainActivity.API_OPERATIONS.getUser(
                                            jwtToken
                                    );

                                    userFetchResult.enqueue(new Callback<User>() {
                                        @Override
                                        public void onResponse(Call<User> call, Response<User> response) {
                                            if((user = response.body()) == null) {
                                                displayErrorAndReauth("Could not refetch user! " + response.code());
                                                return;
                                            }

                                            String responseCode = user.getResponse();

                                            if(response.isSuccessful() && responseCode.equals("ok")) {
                                                setProfileImageSource();
                                                setTextSources();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<User> call, Throwable t) {
                                            displayErrorAndReauth("Internal server error! Could not refetch user! " + response.code());
                                        }
                                    });
                                }
                            } else {
                                // entering here means the refresh token has expired and/or can't be read
                                onAuthStateChanged.performAuthReset();
                            }
                        }

                        @Override
                        public void onFailure(Call<Token> call, Throwable t) {
                            displayErrorAndReauth("Couldn't get new token! " + response.code());
                        }
                    });
                } else {
                    // if the token hasn't expired but is corrupted, then just log them out
                    onAuthStateChanged.performAuthReset();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                displayErrorAndReauth("Oops! " + t.getMessage());
            }
        });
    }

    private void displayErrorAndReauth(String error) {
        MainActivity.PREF_CONFIG.displayToast(error);
        onAuthStateChanged.performAuthReset(); // logout upon failure
    }
}
