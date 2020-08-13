package com.example.authio.views.ui;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.authio.R;
import com.example.authio.viewmodels.AuthFragmentViewModel;

public abstract class AuthFragment extends Fragment {

    protected EditText emailInput, passwordInput; // props here exist in both auth fragments

    protected TextView toggleText;
    protected TextView errorText;
    protected Button authButton;

    protected AuthFragmentViewModel viewModel;

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
