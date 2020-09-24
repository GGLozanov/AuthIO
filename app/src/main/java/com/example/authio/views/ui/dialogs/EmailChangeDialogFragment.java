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

        ProfileFragmentViewModel viewModel = new ViewModelProvider(requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(ProfileFragmentViewModel.class); // same viewmodel since it belongs to the same "screen", per se, and same LiveData instance is used/needed
        viewModel.init();

        view.setClipToOutline(true);

        initBaseErrableEditTexts(view);

        newEmailInput = ((ErrableEditText) view.findViewById(R.id.new_email_input_field))
                .withErrorPredicate((email) -> ErrorPredicates.email.test(email) ||
                        email.equals(Objects.requireNonNull(
                                        viewModel.getUser().getValue()).getEntity().getEmail()))
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
                    Objects.requireNonNull(
                            viewModel.getUser().getValue()).getEntity().getEmail(), // get user's actual email from the livedata instance
                    password
            ).observe(this, (token) -> {
                if(token != null) {
                    String responseCode = token.getResponse();

                    if(responseCode.equals(Constants.SUCCESS_RESPONSE)) {

                        String email = Objects.requireNonNull(newEmailInput.getText()).toString();

                        viewModel.updateUser(new HashMap<String, String>(){{
                            put(Constants.EMAIL, email);
                        }}).observe(this, (result) -> {
                            if(result.getResponse().equals(Constants.SUCCESS_RESPONSE)) {
                                Log.i("EmailChangeDFragment", "confirmButton closure —> Auth user email successfuly changed");
                                Toast.makeText(getContext(), "Email successfully changed!", Toast.LENGTH_LONG).show();
                                dismiss();
                            } else {
                                Log.w("EmailChangeDFragment", "Failed to edit user -> " + result.getResponse());
                                onDialogCriticalServerError.onDialogCriticalServerError("Couldn't change email! Please login again!");
                            }
                        });
                    } else if(responseCode.equals(Constants.FAILED_RESPONSE)) {
                        Toast.makeText(getContext(), "Invalid credentials! Password may be incorrect!", Toast.LENGTH_LONG).show();
                        dismiss();
                    } else {
                        // internal server error
                        Toast.makeText(getContext(), "Something went wrong. Internal server error", Toast.LENGTH_LONG).show();
                        onDialogCriticalServerError.onDialogCriticalServerError("Internal server error. Connection suspended.");
                    }
                } else {
                    Log.e("EmailChangeDFragment", "No Reference to SharedPreferences found");
                    onDialogCriticalServerError.onDialogCriticalServerError("Couldn't resolve internal app error.");
                }
            });
        });

        return view;
    }
}
