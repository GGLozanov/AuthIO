package com.example.authio.ui;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.authio.R;
import com.example.authio.activities.MainActivity;
import com.example.authio.models.Token;
import com.example.authio.api.OnAuthStateChanged;
import com.example.authio.utils.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.util.Collections;

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
        hideErrorMessage();

        Call<Token> authResult = MainActivity
                .API_OPERATIONS
                .performLogin(
                  email,
                  password
                );

        authResult.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                Token token;
                String responseCode;

                if(response.isSuccessful() && (token = response.body()) != null) {
                    responseCode = token.getResponse();

                    if (responseCode.equals("ok")) {
                        MainActivity.PREF_CONFIG.writeToken(token.getJWT());
                        MainActivity.PREF_CONFIG.writeRefreshToken(token.getRefreshJWT());

                        onLoginFormActivity.performAuthChange(
                                null // pass in empty user and get in WelcomeFragment
                        ); // communicate w/ activity to update fragment through interface
                    }
                } else {
                    try {
                        responseCode = NetworkUtils.
                                extractResponseFromResponseErrorBody(response, "response");
                    } catch (JSONException | IOException | NetworkUtils.ResponseSuccessfulException e) {
                        Log.e("LoginFragw JSON parse", e.toString());
                        MainActivity.PREF_CONFIG.displayToast("Bad server response!");
                        return;
                    }

                    if(responseCode.equals("failed")) {
                        MainActivity.PREF_CONFIG.displayToast("Login unsuccessful!");
                    }
                    // TODO: Handle more errors from API down the line here (if they emerge)
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                // handle failed HTTP response receiving due to server-side exception here
                MainActivity.PREF_CONFIG.displayToast(t.getMessage());
            }
        });

        emailInput.setText("");
        passwordInput.setText("");
    }
}