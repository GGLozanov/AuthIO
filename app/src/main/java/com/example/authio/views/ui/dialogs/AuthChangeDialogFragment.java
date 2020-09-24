package com.example.authio.views.ui.dialogs;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.authio.R;
import com.example.authio.models.User;
import com.example.authio.shared.ErrorPredicates;
import com.example.authio.views.custom.ErrableEditText;

import java.util.Arrays;
import java.util.Objects;

public abstract class AuthChangeDialogFragment extends DialogFragment {
    protected ErrableEditText passwordInput, confirmPasswordInput;
    protected Button cancel, confirm;

    public AuthChangeDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    public interface OnDialogCriticalServerError {
        void onDialogCriticalServerError(String errorText);
    }

    protected OnDialogCriticalServerError onDialogCriticalServerError;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        onDialogCriticalServerError = (OnDialogCriticalServerError) getTargetFragment();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void initBaseErrableEditTexts(View view) {
        passwordInput = ((ErrableEditText) view.findViewById(R.id.password_input_field))
                .withErrorPredicate(ErrorPredicates.password)
                .withErrorText("Passwords must be the same and valid (3 to 15 characters)");
        confirmPasswordInput = ((ErrableEditText) view.findViewById(R.id.confirm_password_input_field))
                .withErrorPredicate((password) -> ErrorPredicates.password.test(password) ||
                        !password.equals(Objects.requireNonNull(passwordInput.getText()).toString()))
                .withErrorText("Passwords must be the same and valid (3 to 15 characters)");
    }

    protected void initBaseButtonListeners(View view, View.OnClickListener confirmButtonListener) {
        confirm = view.findViewById(R.id.confirm_button);
        cancel = view.findViewById(R.id.cancel_button);

        cancel.setOnClickListener((v) -> dismiss());
        confirm.setOnClickListener(confirmButtonListener);
    }
}
