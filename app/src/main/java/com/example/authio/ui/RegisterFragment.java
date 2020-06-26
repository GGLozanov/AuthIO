package com.example.authio.ui;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.authio.R;
import com.example.authio.activities.MainActivity;
import com.example.authio.api.APIOperations;
import com.example.authio.api.NetworkModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends AuthFragment {

    public interface OnRegisterFormActivity {
        void performRegister(String email, String username, String description);
            // TODO: Redefinition of method; optimise to call only 1 method from MainActivity

        void performToggleToLogin();
    }

    private EditText usernameInput, descriptionInput;
    private OnRegisterFormActivity onRegisterFormActivity;

    public RegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        initAuthFields(view);

        initListeners((v) -> onRegisterFormActivity.performToggleToLogin(), (v) -> performRegister());

        usernameInput = view.findViewById(R.id.username_input_field);
        descriptionInput = view.findViewById(R.id.description_input_field);

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        onRegisterFormActivity = (OnRegisterFormActivity) activity;
    }

    public void performRegister() {
        String email = emailInput.getText().toString(),
                password = passwordInput.getText().toString(),
                username = usernameInput.getText().toString(),
                description = descriptionInput.getText().toString();

        // TODO: Hash password
        Call<NetworkModel> authResult = MainActivity
                .API_OPERATIONS
                .performRegistration(
                        email,
                        password,
                        username,
                        description
                );
        authResult.enqueue(new Callback<NetworkModel>() {
            @Override
            public void onResponse(Call<NetworkModel> call, Response<NetworkModel> response) {
                // handle application-level errors intended from HTTP response here...

                String responseCode = response.body().getResponse();

                if(responseCode.equals("ok")) {
                    MainActivity.PREF_CONFIG.displayToast("Registration successful...");

                    onRegisterFormActivity.performRegister(email, username, description);
                    // TODO: Update fragment and replace with welcome fragment through interface
                } else if(responseCode.equals("failed")) {
                    MainActivity.PREF_CONFIG.displayToast("Registration unsuccessful...");
                }
            }

            @Override
            public void onFailure(Call<NetworkModel> call, Throwable t) {
                // handle failed HTTP response receiving due to server-side exception here...
            }
        });
    }
}
