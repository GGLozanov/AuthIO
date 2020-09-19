package com.example.authio.views.ui.dialogs;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;

import com.example.authio.R;
import com.example.authio.shared.Constants;
import com.example.authio.shared.ErrorPredicates;
import com.example.authio.utils.PrefConfig;
import com.example.authio.viewmodels.ProfileFragmentViewModel;
import com.example.authio.views.activities.MainActivity;
import com.example.authio.views.custom.ErrableEditText;

import java.util.HashMap;
import java.util.Objects;

public class PasswordChangeDialogFragment extends AuthChangeDialogFragment {
    private ErrableEditText newPasswordInput;

    public PasswordChangeDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.d_fragment_password_change, container, false);

        ProfileFragmentViewModel viewModel = new ViewModelProvider(requireActivity())
                .get(ProfileFragmentViewModel.class); // same viewmodel since it belongs to the same "screen", per se, and same LiveData instance is used/needed
        viewModel.init();

        view.setClipToOutline(true);

        initBaseErrableEditTexts(view);

        newPasswordInput = ((ErrableEditText) view.findViewById(R.id.new_password_input_field))
                .withErrorPredicate(ErrorPredicates.password)
                .withErrorText("Passwords must be the same and valid (3 to 15 characters)"); // TODO: Refactor these string literals into source files or static properties of holder classes

        // preliminary fragment-specific changes
        passwordInput.asPassword();
        confirmPasswordInput.setErrorPredicate((password) ->
                ErrorPredicates.password.test(password) || !password.equals(Objects.requireNonNull(newPasswordInput.getText()).toString()));

        // NOTE: ConfirmPasswordInput is used to confirm the NEW password here
        initBaseButtonListeners(view, (v) -> {
            if(newPasswordInput.setErrorTextIfError() | passwordInput.setErrorTextIfError()
                    | confirmPasswordInput.setErrorTextIfError()) {
                return;
            }

            String password = Objects.requireNonNull(passwordInput.getText()).toString();

            viewModel.getLoginToken(
                    Objects.requireNonNull(viewModel.getUser().getValue())
                            .getEntity().getEmail(),
                    password
            ).observe(this, (token) -> {
                if(token != null) {
                    String responseCode = token.getResponse();

                    if(responseCode.equals(Constants.SUCCESS_RESPONSE)) {

                        String newPassword = Objects.requireNonNull(newPasswordInput.getText()).toString();

                        viewModel.updateUser(new HashMap<String, String>(){{
                            put(Constants.PASSWORD, newPassword);
                        }}).observe(this, (result) -> {
                            if(result.getResponse().equals(Constants.SUCCESS_RESPONSE)) {
                                Log.i("PasswordChangeDFragment", "confirmButton closure —> Auth user email successfuly changed");
                                Toast.makeText(getContext(), "Password successfully changed!", Toast.LENGTH_LONG).show();
                                dismiss();
                            } else {
                                Log.w("PasswordChangeDFragment", "Failed to edit user -> " + result.getResponse());
                                onDialogCriticalServerError.onDialogCriticalServerError("Couldn't change email! Please login again!");
                            }
                        });
                    } else if(responseCode.equals(Constants.FAILED_RESPONSE)) {
                        Toast.makeText(getContext(), "Invalid credentials! Initial password may not be correct!", Toast.LENGTH_LONG).show();
                        dismiss();
                    } else {
                        // internal server error
                        Toast.makeText(getContext(), "Something went wrong. Internal server error", Toast.LENGTH_LONG).show();
                        onDialogCriticalServerError.onDialogCriticalServerError("Internal server error. Connection suspended.");
                    }
                } else {
                    Log.e("PasswordChangeDFragment", "No Reference to SharedPreferences found");
                    onDialogCriticalServerError.onDialogCriticalServerError("Couldn't resolve internal app error.");
                }
            });
        });

        return view;
    }
}
