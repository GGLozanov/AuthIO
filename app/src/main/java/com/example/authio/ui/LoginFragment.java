package com.example.authio.ui;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.authio.R;
import com.example.authio.activities.MainActivity;
import com.example.authio.api.NetworkModel;

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
    public interface OnLoginFormActivity {
        void performLogin(String email, String username, String description);
            // arguments to be written into sharedprefs

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

        Call<NetworkModel> authResult = MainActivity
                .API_OPERATIONS
                .performLogin(
                  email,
                  password
                );

        authResult.enqueue(new Callback<NetworkModel>() {
            @Override
            public void onResponse(Call<NetworkModel> call, Response<NetworkModel> response) {
                NetworkModel body = response.body();
                String responseCode = body.getResponse();

                if(responseCode.equals("ok")) {
                    MainActivity.PREF_CONFIG.writeLoginStatus(true); // set the status of logged in user to true

                    onLoginFormActivity.performLogin(email, body.getUsername(), body.getDescription());
                    // communicate w/ activity to update fragment through interface
                } else if(responseCode.equals("failed")) {
                    MainActivity.PREF_CONFIG.displayToast("Registration unsuccessful...");
                }
            }

            @Override
            public void onFailure(Call<NetworkModel> call, Throwable t) {

            }
        });


    }
}
