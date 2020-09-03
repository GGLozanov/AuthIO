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

public class EmailChangeDialogFragment extends AuthChangeDialogFragment {
    private ErrableEditText newEmailInput;

    public EmailChangeDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.d_fragment_email_change, container, false);

        ProfileFragmentViewModel viewModel = new ViewModelProvider(requireActivity())
                .get(ProfileFragmentViewModel.class); // same viewmodel since it belongs to the same "screen", per se, and same LiveData instance is used/needed
        viewModel.init();

        view.setClipToOutline(true);

        initBaseErrableEditTexts(view);

        newEmailInput = ((ErrableEditText) view.findViewById(R.id.new_email_input_field))
                .withErrorPredicate((email) -> ErrorPredicates.email.test(email) ||
                        email.equals(Objects.requireNonNull(viewModel.getUser().getValue()).getEmail()))
                .withErrorText("Enter a valid AND different E-mail");

        initBaseButtonListeners(view, (v) -> {
            if(newEmailInput.setErrorTextIfError() | passwordInput.setErrorTextIfError()
                    | confirmPasswordInput.setErrorTextIfError() |
                    !Objects.requireNonNull(passwordInput.getText()).toString()
                            .equals(Objects.requireNonNull(confirmPasswordInput.getText()).toString())) {
                return;
            }

            String password = Objects.requireNonNull(passwordInput.getText()).toString();

            viewModel.getLoginToken(
                    Objects.requireNonNull(viewModel.getUser().getValue()).getEmail(), // get user's actual email from the livedata instance
                    password
            ).observe(this, (token) -> {
                if(token != null) {
                    PrefConfig prefConfig;

                    if((prefConfig = MainActivity.PREF_CONFIG_REFERENCE.get()) != null) {
                        String responseCode = token.getResponse();

                        if(responseCode.equals("ok")) {
                            String jwt = token.getJWT();
                            String refreshJwt = token.getRefreshJWT();

                            prefConfig.writeToken(jwt);
                            prefConfig.writeRefreshToken(refreshJwt);

                            String email = Objects.requireNonNull(newEmailInput.getText()).toString();

                            viewModel.updateUser(jwt, refreshJwt, new HashMap<String, String>(){{
                                put("email", email);
                            }}).observe(this, (result) -> {
                                if(result.getResponse().equals("ok")) {
                                    Log.i("EmailChangeDFragment", "confirmButton closure —> Auth user email successfuly changed");
                                    prefConfig.displayToast("Email successfully changed!");
                                    dismiss();
                                } else {
                                    Log.w("EmailChangeDFragment", "Failed to edit user -> " + result.getResponse());
                                    onDialogCriticalServerError.onDialogCriticalServerError("Couldn't change email! Please login again!");
                                }
                            });
                        } else if(responseCode.equals("failed")) {
                            prefConfig.displayToast("Invalid credentials! Password may be incorrect!");
                            dismiss();
                        } else {
                            // internal server error
                            prefConfig.displayToast("Something went wrong. Internal server error" + responseCode);
                            onDialogCriticalServerError.onDialogCriticalServerError("Internal server error. Connection suspended.");
                        }
                    } else {
                        Log.e("EmailChangeDFragment", "No Reference to SharedPreferences found");
                        onDialogCriticalServerError.onDialogCriticalServerError("Couldn't resolve internal app error.");
                    }
                }
            });

        });

        return view;
    }
}
