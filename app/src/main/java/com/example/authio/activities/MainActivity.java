package com.example.authio.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

import com.example.authio.R;
import com.example.authio.api.PrefConfig;
import com.example.authio.ui.LoginFragment;
import com.example.authio.ui.WelcomeFragment;

public class MainActivity extends AppCompatActivity {

    private static PrefConfig prefConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefConfig = new PrefConfig(this); // this current activity's context

        if(findViewById(R.id.fragment_container) != null) {

            // check if first instance; end fragment selection otherwise
            if(savedInstanceState != null) {
                return;
            }

            FragmentManager fragmentManager = getSupportFragmentManager();

            if(prefConfig.readLoginStatus()) {
                // auth'd
                fragmentManager
                        .beginTransaction()
                        .add(R.id.fragment_container, new WelcomeFragment());
                    // add the Welcome fragment to the container
            } else {
                // not auth'd
                fragmentManager
                        .beginTransaction()
                        .add(R.id.fragment_container, new LoginFragment());
                    // add the Login fragment to the container
            }
        }
    }
}
