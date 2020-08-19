package com.example.authio.views.activities;

import androidx.annotation.Nullable;

import android.content.Intent;
import android.os.Bundle;

import com.example.authio.R;
import com.example.authio.models.User;
import com.example.authio.utils.PrefConfig;
import com.example.authio.views.ui.LoginFragment;
import com.example.authio.views.ui.RegisterFragment;
import com.example.authio.views.ui.ProfileFragment;

import java.lang.ref.WeakReference;

public class AuthActivity extends BaseActivity implements
        LoginFragment.OnLoginFormActivity, RegisterFragment.OnRegisterFormActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // need to instantiate prefconfig reference here with derived activity-level context
        prefConfig = new PrefConfig(this);
        PREF_CONFIG_REFERENCE = new WeakReference<>(prefConfig);

        // TODO: Safeguard fragment instances with findFragmentByTag() calls before
        if(findViewById(R.id.fragment_container) != null) {

            // check if first instance; end fragment selection otherwise
            if(savedInstanceState != null) {
                return;
            }

            if(prefConfig.readLoginStatus()) {
                // auth'd
                Intent mainActivityI = new Intent(this, MainActivity.class);
                mainActivityI.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);

                startActivity(mainActivityI);
                // add the MainActivity (which starts the Welcome fragment) to the task stack
            } else {
                // not auth'd
                replaceCurrentFragment(new LoginFragment());
                // add the Login fragment to the container (always commit transactions)
            }
        }
    }

    @Override
    public void performAuthChange(User user) {
        prefConfig.writeLoginStatus(true);

        Bundle args = new Bundle();
        args.putParcelable("user", user); // pass the user through bundle (key-value) args...

        Intent mainActivityI = new Intent(this, MainActivity.class);
        mainActivityI.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        mainActivityI.putExtras(args);

        startActivity(mainActivityI);
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