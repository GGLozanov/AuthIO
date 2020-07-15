package com.example.authio.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.authio.R;

public abstract class AuthFragment extends Fragment {

    protected EditText emailInput, passwordInput; // exists in both auth fragments

    protected TextView toggleText;
    protected TextView errorText;
    protected Button authButton;

    public AuthFragment() {
        // Required empty public constructor
    }

    protected void initAuthFields(View view) {
        emailInput = view.findViewById(R.id.email_input_field);
        passwordInput = view.findViewById(R.id.password_input_field);

        toggleText = view.findViewById(R.id.toggle_text);
        errorText = view.findViewById(R.id.error_text);

        authButton = view.findViewById(R.id.auth_button);
    }

    protected void initListeners(View.OnClickListener toggleTextListener, View.OnClickListener authButtonListener) {
        toggleText.setOnClickListener(toggleTextListener);
        authButton.setOnClickListener(authButtonListener);
    }

    protected void showErrorMessage(String message) {
        errorText.setVisibility(View.VISIBLE);
        errorText.setText(message);
    }

    protected void hideErrorMessage() {
        errorText.setVisibility(View.GONE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        errorText.setVisibility(View.INVISIBLE);
    }
}
