package com.example.authio.ui;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.authio.R;
import com.example.authio.activities.MainActivity;
import com.example.authio.api.UserModel;
import com.example.authio.api.OnAuthStateChanged;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        initAuthFields(view);
        initListeners((v) -> onLoginFormActivity.performToggleToRegister(), (v) -> performLogin());

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // implicit casting
        Activity activity = (Activity) context;
        onLoginFormActivity = (OnLoginFormActivity) activity;
    }

    public void performLogin() {
        String email = emailInput.getText().toString(),
                password = passwordInput.getText().toString();

        if(email.isEmpty() || password.isEmpty()) {
            showErrorMessage("Invalid info in forms!");
            return;
        }
        hideErorrMessage();


        Call<UserModel> authResult = MainActivity
                .API_OPERATIONS
                .performLogin(
                  email,
                  password
                );

        authResult.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                if(response.isSuccessful()) {
                    UserModel body = response.body();
                    String responseCode = body.getResponse();

                    if (responseCode.equals("ok")) {
                        MainActivity.PREF_CONFIG.displayToast("Login successful...");

                        onLoginFormActivity.performAuthChange(
                                body.getEmail(),
                                body.getUsername(),
                                body.getDescription()
                        );
                        // communicate w/ activity to update fragment through interface
                    } else if (responseCode.equals("failed")) {
                        MainActivity.PREF_CONFIG.displayToast("Login unsuccessful...");
                    }
                } else {
                    MainActivity.PREF_CONFIG.displayToast("Something went wrong...");
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                // handle failed HTTP response receiving due to server-side exception here
                MainActivity.PREF_CONFIG.displayToast(t.getMessage());
            }
        });

        emailInput.setText("");
        passwordInput.setText("");
    }
}
