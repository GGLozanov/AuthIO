package com.example.authio.views.ui.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.authio.R;
import com.example.authio.shared.Constants;
import com.example.authio.utils.PrefConfig;
import com.example.authio.viewmodels.LoginFragmentViewModel;
import com.example.authio.api.OnAuthStateChanged;
import com.example.authio.views.activities.AuthActivity;

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

        viewModel = new ViewModelProvider(requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication())) // pass in application for application context
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

        if(emailInput.setErrorTextIfError() | passwordInput.setErrorTextIfError()) {
            showErrorMessage("Invalid info in fields!");
            return;
        }

        hideErrorMessage();

        ((LoginFragmentViewModel) viewModel).getLoginToken(email, password)
                .observe(this, (token) -> {
                    if(token != null) {
                        String responseCode = token.getResponse();

                        if(responseCode.equals(Constants.SUCCESS_RESPONSE)) {

                            onLoginFormActivity.performAuthChange(
                                    null // pass in empty user and get in WelcomeFragment
                            ); // communicate w/ activity to update fragment through interface
                        } else if(responseCode.equals(Constants.FAILED_RESPONSE)) {
                            Toast.makeText(getContext(), "Login unsuccessful!", Toast.LENGTH_LONG).show();
                        } else {
                            // internal server error
                            Toast.makeText(getContext(), "Login failed! Please check your connection and try again!", Toast.LENGTH_LONG).show();
                        }
                        // TODO: Handle more errors from API down the line here (if they emerge)
                    } else {
                        Log.e("No reference", "Found no reference to sharedpreferences in LoginFragment.");
                    }
                });

        emailInput.setText("");
        passwordInput.setText("");
    }
}
