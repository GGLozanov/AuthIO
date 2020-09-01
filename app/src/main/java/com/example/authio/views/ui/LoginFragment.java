package com.example.authio.views.ui;


import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.authio.R;
import com.example.authio.utils.PrefConfig;
import com.example.authio.utils.TokenUtils;
import com.example.authio.viewmodels.LoginFragmentViewModel;
import com.example.authio.api.OnAuthStateChanged;
import com.example.authio.views.activities.AuthActivity;
import com.example.authio.views.activities.MainActivity;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends AuthFragment {

    // interface designed to communicate with MainActivity
    // through implementation in MainActivity and implicit cast
    // in onAttach() method here
    public interface OnLoginFormActivity extends OnAuthStateChanged {
        void performToggleToRegister();
    }

    private OnLoginFormActivity onLoginFormActivity;

    public LoginFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        viewModel = new ViewModelProvider(requireActivity())
                .get(LoginFragmentViewModel.class);
        viewModel.init();

        initAuthFields(view);
        initListeners((v) -> onLoginFormActivity.performToggleToRegister(),
                (v) -> performLogin());

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context; // explicit downcasting
        onLoginFormActivity = (OnLoginFormActivity) activity;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void performLogin() {
        String email = Objects.requireNonNull(emailInput.getText()).toString(),
                password = Objects.requireNonNull(passwordInput.getText()).toString();

        if(emailInput.isInvalid() | passwordInput.isInvalid()) {
            showErrorMessage("Invalid info in fields!", emailInput.wasInvalid() ?
                            "Enter a valid e-mail" : null,
                    passwordInput.wasInvalid() ? "Enter a (longer) password" : null);
            return;
        }

        hideErrorMessage();

        ((LoginFragmentViewModel) viewModel).getLoginToken(email, password)
                .observe(this, (token) -> {
                    if(token != null) {
                        PrefConfig prefConfig;

                        if((prefConfig = AuthActivity.PREF_CONFIG_REFERENCE.get()) != null) {
                            String responseCode = token.getResponse();

                            if(responseCode.equals("ok")) {
                                String jwt = token.getJWT();

                                prefConfig.writeToken(jwt);
                                prefConfig.writeRefreshToken(token.getRefreshJWT());

                                onLoginFormActivity.performAuthChange(
                                        null // pass in empty user and get in WelcomeFragment
                                ); // communicate w/ activity to update fragment through interface
                            } else if(responseCode.equals("failed")) {
                                prefConfig.displayToast("Login unsuccessful!");
                            } else {
                                // internal server error
                                prefConfig.displayToast("Login: Something went wrong... " + responseCode);
                            }
                            // TODO: Handle more errors from API down the line here (if they emerge)
                        } else {
                            Log.e("No reference", "Found no reference to sharedpreferences in LoginFragment.");
                        }
                    }
                });

        emailInput.setText("");
        passwordInput.setText("");
    }
}
