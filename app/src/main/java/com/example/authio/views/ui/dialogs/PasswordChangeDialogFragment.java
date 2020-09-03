package com.example.authio.views.ui.dialogs;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.ViewModelProvider;

import com.example.authio.R;
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
                    Objects.requireNonNull(viewModel.getUser().getValue()).getEmail(),
                    password
            ).observe(this, (token) -> {
                if(token != null) {
                    PrefConfig prefConfig;

                    // TODO: Optimise code repetition. . .
                    if((prefConfig = MainActivity.PREF_CONFIG_REFERENCE.get()) != null) {
                        String responseCode = token.getResponse();

                        if(responseCode.equals("ok")) {
                            String jwt = token.getJWT();
                            String refreshJwt = token.getRefreshJWT();

                            prefConfig.writeToken(jwt);
                            prefConfig.writeRefreshToken(refreshJwt);

                            String newPassword = Objects.requireNonNull(newPasswordInput.getText()).toString();

                            viewModel.updateUser(jwt, refreshJwt, new HashMap<String, String>(){{
                                put("password", newPassword);
                            }}).observe(this, (result) -> {
                                if(result.getResponse().equals("ok")) {
                                    Log.i("PasswordChangeDFragment", "confirmButton closure —> Auth user email successfuly changed");
                                    prefConfig.displayToast("Password successfully changed!");
                                    dismiss();
                                } else {
                                    Log.w("PasswordChangeDFragment", "Failed to edit user -> " + result.getResponse());
                                    onDialogCriticalServerError.onDialogCriticalServerError("Couldn't change email! Please login again!");
                                }
                            });
                        } else if(responseCode.equals("failed")) {
                            prefConfig.displayToast("Invalid credentials! Initial password may not be correct!");
                            dismiss();
                        } else {
                            // internal server error
                            prefConfig.displayToast("Something went wrong. Internal server error" + responseCode);
                            onDialogCriticalServerError.onDialogCriticalServerError("Internal server error. Connection suspended.");
                        }
                    } else {
                        Log.e("PasswordChangeDFragment", "No Reference to SharedPreferences found");
                        onDialogCriticalServerError.onDialogCriticalServerError("Couldn't resolve internal app error.");
                    }
                }
            });
        });

        return view;
    }
}
