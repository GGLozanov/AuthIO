package com.example.authio.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;

import com.example.authio.R;
import com.example.authio.api.APIClient;
import com.example.authio.api.APIOperations;
import com.example.authio.api.PrefConfig;
import com.example.authio.ui.LoginFragment;
import com.example.authio.ui.RegisterFragment;
import com.example.authio.ui.WelcomeFragment;

public class MainActivity extends AppCompatActivity implements
        LoginFragment.OnLoginFormActivity, RegisterFragment.OnRegisterFormActivity {

    public static PrefConfig PREF_CONFIG;
    public static APIOperations API_OPERATIONS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PREF_CONFIG = new PrefConfig(this); // this current activity's context
        API_OPERATIONS = APIClient
                .getAPIClient()
                .create(APIOperations.class);
            // create new instance of APIOperations through a retrofit instance to receive HTTP responses

        if(findViewById(R.id.fragment_container) != null) {

            // check if first instance; end fragment selection otherwise
            if(savedInstanceState != null) {
                return;
            }

            FragmentManager fragmentManager = getSupportFragmentManager();

            if(PREF_CONFIG.readLoginStatus()) {
                // auth'd
                fragmentManager
                        .beginTransaction()
                        .add(R.id.fragment_container, new WelcomeFragment()).commit();
                    // add the Welcome fragment to the container
            } else {
                // not auth'd
                fragmentManager
                        .beginTransaction()
                        .add(R.id.fragment_container, new LoginFragment()).commit();
                    // add the Login fragment to the container (always commit transactions)
            }
        }
    }

    private void replaceCurrentFragment(Fragment fragmentReplacement) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragmentReplacement).commitNow();
    }


    @Override
    public void performAuthChange(Integer id, String email, String username, String description) {
        // TODO: Replace PREF_CONFIG for information and use API calls (keep for login status)

        PREF_CONFIG.writeLoginStatus(true);
        PREF_CONFIG.writeUserPrefs(id, email, username, description);

        replaceCurrentFragment(new WelcomeFragment());
    }

    @Override
    public void performAuthReset() {
        // TODO: Replace PREF_CONFIG for information and use API calls (keep for login status)

        PREF_CONFIG.writeLoginStatus(false);
        PREF_CONFIG.resetUserPrefs();

        replaceCurrentFragment(new LoginFragment());
    }

    @Override
    public void performToggleToRegister() {
        replaceCurrentFragment(new RegisterFragment());
    }

    @Override
    public void performToggleToLogin() {
        replaceCurrentFragment(new LoginFragment());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
