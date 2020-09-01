package com.example.authio.views.ui;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.authio.R;
import com.example.authio.shared.ErrorPredicates;
import com.example.authio.viewmodels.AuthFragmentViewModel;
import com.example.authio.views.custom.ErrableEditText;

public abstract class AuthFragment extends Fragment {

    protected ErrableEditText emailInput, passwordInput; // props here exist in both auth fragments

    protected TextView toggleText;
    protected TextView errorText;
    protected Button authButton;

    protected AuthFragmentViewModel viewModel;

    public AuthFragment() {
        // Required empty public constructor
    }

    protected void initAuthFields(View view) {
        emailInput = ((ErrableEditText) view.findViewById(R.id.email_input_field))
                .withErrorPredicate(ErrorPredicates.email);
        passwordInput = ((ErrableEditText) view.findViewById(R.id.password_input_field))
                .withErrorPredicate(ErrorPredicates.password);

        toggleText = view.findViewById(R.id.toggle_text);
        errorText = view.findViewById(R.id.error_text);

        authButton = view.findViewById(R.id.auth_button);
    }

    protected void initListeners(View.OnClickListener toggleTextListener, View.OnClickListener authButtonListener) {
        toggleText.setOnClickListener(toggleTextListener);
        authButton.setOnClickListener(authButtonListener);
    }

    protected void showErrorMessage(@NonNull String message, @Nullable String emailFormError,
                                    @Nullable String passwordFormError) {
        if(emailFormError != null) {
            emailInput.setError(emailFormError);
        }

        if(passwordFormError != null) {
            passwordInput.setError(passwordFormError);
        }

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
