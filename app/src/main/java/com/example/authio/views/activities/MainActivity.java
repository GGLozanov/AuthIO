package com.example.authio.views.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.authio.R;
import com.example.authio.api.OnAuthStateChanged;
import com.example.authio.utils.PrefConfig;
import com.example.authio.models.User;
import com.example.authio.views.ui.LoginFragment;
import com.example.authio.views.ui.RegisterFragment;
import com.example.authio.views.ui.WelcomeFragment;

import java.lang.ref.WeakReference;

// TODO: Separate into AuthActivity and MainActivity
public class MainActivity extends AppCompatActivity implements
        LoginFragment.OnLoginFormActivity, RegisterFragment.OnRegisterFormActivity, OnAuthStateChanged {

    public static WeakReference<PrefConfig> PREF_CONFIG_REFERENCE; // weak reference due to requiring activity context => avoid memory leak
    PrefConfig prefConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PREF_CONFIG_REFERENCE = new WeakReference<>(new PrefConfig(this));

        // TODO: Safeguard fragment instances with findFragmentByTag() calls before
        if(findViewById(R.id.fragment_container) != null) {

            // check if first instance; end fragment selection otherwise
            if(savedInstanceState != null) {
                return;
            }

            if((prefConfig = PREF_CONFIG_REFERENCE.get()) != null) {
                if(prefConfig.readLoginStatus()) {
                    // auth'd
                    replaceCurrentFragment(new WelcomeFragment());
                    // add the Welcome fragment to the container
                } else {
                    // not auth'd
                    replaceCurrentFragment(new LoginFragment());
                    // add the Login fragment to the container (always commit transactions)
                }
            } else {
                Log.e("No reference", "Found no reference to sharedpreferences in MainActivity.");
                replaceCurrentFragment(new LoginFragment());
            }
        }
    }

    private void replaceCurrentFragment(Fragment fragmentReplacement) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragmentReplacement).commitNow();
    }


    @Override
    public void performAuthChange(User user) {
        prefConfig.writeLoginStatus(true);

        Bundle args = new Bundle();
        args.putParcelable("user", user);

        WelcomeFragment welcomeFragment = new WelcomeFragment();
        welcomeFragment.setArguments(args); // pass the user through bundle (key-value) args...

        replaceCurrentFragment(welcomeFragment);
    }

    @Override
    public void performAuthReset() {
        prefConfig.writeLoginStatus(false);
        prefConfig.writeToken(null);

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
