package com.example.authio.ui;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A simple {@link Fragment} subclass.
 */
public class WelcomeFragment extends Fragment {

    private TextView emailText, usernameText, descriptionText;
    private ImageView profileImage;
    private Button logoutButton;

    private OnAuthStateChanged onAuthStateChanged; // listener for performing logout

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

        setProfileImageSource();
        setTextSources();

        logoutButton = view.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener((v) -> onAuthStateChanged.performAuthReset());

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        onAuthStateChanged = (OnAuthStateChanged) activity;
    }

    private void setTextSources() {
        emailText.setText("E-mail: " + MainActivity.PREF_CONFIG.readEmail());
        usernameText.setText("Welcome, " + MainActivity.PREF_CONFIG.readUsername());
        descriptionText.setText(MainActivity.PREF_CONFIG.readDescription());
    }

    private InputStream getURLContent(String urlString) {
        URL url;

        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            Log.e("WelcomeFragment: ", e.toString());
            return null;
        }

        AtomicReference<InputStream> content = new AtomicReference<>();

        // TODO: Handle in different thread (Handler? AsyncTask?)
        // start new thread to get the content's of the URL asynchronously
        Thread networkThread = new Thread(() -> {
            try {
                content.set((InputStream) url.getContent()); // get image displayed
            } catch (IOException e) {
                Log.e("WelcomeFragment: ", e.toString());
            }
        });
        networkThread.setPriority(Thread.MIN_PRIORITY);

        networkThread.start();

        try {
            networkThread.join();
        } catch (InterruptedException e) {
            Log.e("WelcomeFragment: ", e.toString());
        }

        return content.get();
    }

    private void setProfileImageSource() {
        InputStream content = getURLContent(APIClient.getBaseURL() +
                "uploads/" +
                MainActivity.PREF_CONFIG.readUsername() +
                ".jpg");

        if(content == null) {
            return; // just stick with the default variable
        }

        Drawable drawable = Drawable.createFromStream(content , "src");
        profileImage.setImageDrawable(drawable);
    }
}
