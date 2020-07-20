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
import com.example.authio.models.User;
import com.example.authio.utils.ImageDownloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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


    // render image here on onAttach
    // sql query with path to image (from sharedprefs?) on different thread
    // render image with path from server

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

        if(user == null) {
            Call<User> userFetchResult = MainActivity.API_OPERATIONS.getUser(
                    MainActivity.PREF_CONFIG.readToken()
            );

            userFetchResult.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    // TODO: Handle refresh token saving and reissuing request here
                    if(response.isSuccessful() && (user = response.body()) != null) {
                        setProfileImageSource();
                        setTextSources();
                    } else {
                        MainActivity.PREF_CONFIG.displayToast("Something went wrong. " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    MainActivity.PREF_CONFIG.displayToast("Oops! " + t.getMessage());
                    onAuthStateChanged.performAuthReset(); // logout upon failure
                }
            });
        } else {
            // need to call here again because of potential fallthrough from background thread
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

        // async login => logout if it fails

        new ImageDownloader(profileImage).execute(APIClient.getBaseURL() +
                "uploads/" +
                user.getId() + // TODO: Replace with user instance and API call
                ".jpg"); // start AsyncTask to asynchronously download and render image upon completion
    }
}
