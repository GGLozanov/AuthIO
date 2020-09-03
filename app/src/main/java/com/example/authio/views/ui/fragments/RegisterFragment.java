package com.example.authio.views.ui.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.authio.R;
import com.example.authio.api.APIClient;
import com.example.authio.shared.Callbacks;
import com.example.authio.utils.PrefConfig;
import com.example.authio.utils.TokenUtils;
import com.example.authio.viewmodels.RegisterFragmentViewModel;
import com.example.authio.models.Image;
import com.example.authio.models.User;
import com.example.authio.api.OnAuthStateChanged;
import com.example.authio.utils.ImageUtils;
import com.example.authio.views.activities.AuthActivity;
import com.example.authio.views.custom.ErrableEditText;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends AuthFragment {

    public interface OnRegisterFormActivity extends OnAuthStateChanged {
        void performToggleToLogin();
    }

    private static int IMG_REQUEST_CODE = 777; // request code for get_image activity (used in callback)
    private Bitmap bitmap; // image represented as a bitmap to be decoded from intent result

    private ErrableEditText usernameInput, descriptionInput;
    private ImageView profileImage;
    private OnRegisterFormActivity onRegisterFormActivity;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        viewModel = new ViewModelProvider(requireActivity())
                .get(RegisterFragmentViewModel.class);
        viewModel.init();

        initAuthFields(view);
        initListeners((v) -> onRegisterFormActivity.performToggleToLogin(),
                (v) -> performRegister());

        usernameInput = ((ErrableEditText) view.findViewById(R.id.username_input_field))
                .asUsername();
        descriptionInput = ((ErrableEditText) view.findViewById(R.id.description_input_field))
                .asDescription();
        profileImage = view.findViewById(R.id.profile_image);

        profileImage.setOnClickListener((v) -> {
            Log.i("RegisterFragment", "ProfileImageOnClickListener —> User is selecting their profile picture");

            // handle onClick for profile image by rendering new intent w/ "image/*" and GET_CONTENT type
            Intent selectImageIntent = new Intent();
            selectImageIntent.setType("image/*"); // set MIME data type to all images
            selectImageIntent.setAction(Intent.ACTION_GET_CONTENT); // set the desired action to get image
            startActivityForResult(selectImageIntent, IMG_REQUEST_CODE); // start activity and await result
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // receive result from other activity after it finishes. . .

        if((bitmap = Callbacks.getBitmapFromImageOnActivityResult(
                getActivity(),
                IMG_REQUEST_CODE,
                requestCode,
                resultCode,
                data)) != null) {
            profileImage.setImageBitmap(
                    bitmap
            ); // set the new image resource to be decoded from the bitmap
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        onRegisterFormActivity = (OnRegisterFormActivity) activity;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void performRegister() {
        String email = Objects.requireNonNull(emailInput.getText()).toString(),
                password = Objects.requireNonNull(passwordInput.getText()).toString(),
                username = Objects.requireNonNull(usernameInput.getText()).toString(),
                description = Objects.requireNonNull(descriptionInput.getText()).toString();

        if(emailInput.setErrorTextIfError() | passwordInput.setErrorTextIfError()
                | usernameInput.setErrorTextIfError() | descriptionInput.setErrorTextIfError()) { // disable lazy eval; fucks over predicate testing
            showErrorMessage("Invalid info in fields!");
            return;
        }

        hideErrorMessage();

        // TODO: Need to handle different responses in viewmodel and not here (breaks MVVM a bit)
        ((RegisterFragmentViewModel) viewModel).getRegisterToken(email, username, password, description)
                .observe(this, (token) -> {
                    if(token != null) {
                        PrefConfig prefConfig;

                        if((prefConfig = AuthActivity.PREF_CONFIG_REFERENCE.get()) != null) {
                            String responseCode = token.getResponse();

                            if(responseCode.equals("ok")) {
                                String jwt = token.getJWT();

                                Log.i("RegisterFragment", "performRegister —> Writing user JWT and refresh JWT to shraredpreferences.");

                                prefConfig.writeToken(jwt); // write & save token
                                prefConfig.writeRefreshToken(token.getRefreshJWT()); // write & save refresh token

                                int userId = TokenUtils.getTokenUserIdFromPayload(jwt);

                                // don't upload picture if it's the default
                                User user = new User(
                                        userId,
                                        responseCode,
                                        username,
                                        description,
                                        email
                                );

                                if(profileImage.getDrawable() !=
                                        ContextCompat.getDrawable(
                                                Objects.requireNonNull(getContext()), R.drawable.default_img)) {
                                    Log.i("RegisterFragment", "performRegister —> User has chosen custom picture => proceed to upload to server.");
                                    user.setPhotoUrl(APIClient.getBaseURL() +
                                            "uploads/" +
                                            userId +
                                            ".jpg");
                                    uploadImageAndAuth(jwt, user); // go on to upload the image if the registration was successful
                                } else {
                                    Log.i("RegisterFragment", "performRegister —> User has chosen default picture => authenticate.");

                                    onRegisterFormActivity.performAuthChange(
                                            user
                                    ); // switch to new activity if no image left to upload

                                    // can't extract auth trigger from if/else block because the other call is inside async callback closure
                                }
                            } else if(responseCode.equals("exists")) {
                                Log.w("RegisterFragment", "performRegister —> User resource already exists.");
                                prefConfig.displayToast("User already exists!");
                            } else if(responseCode.equals("failed")) {
                                Log.w("RegisterFragment", "performRegister —> User resource could not be created (invalid info).");
                                prefConfig.displayToast("Registration unsuccessful!");
                            } else {
                                // internal server error
                                Log.w("RegisterFragment", "performRegister —> Failed to create user resource to unknown issues (server error).");
                                prefConfig.displayToast(responseCode);
                            }
                        } else {
                            Log.e("RegisterFragment", "performRegister —> Found no reference to sharedpreferences in RegisterFragment.");
                        }
                    }
                });
    }

    private void uploadImageAndAuth(String token, User user) {
        ((RegisterFragmentViewModel) viewModel).uploadUserImage(
                token,
                new Image(null, ImageUtils.encodeImage(bitmap)))
                .observe(this, (response) -> {
                    // TODO: Extract handling in viewmodel (leave this to just update the UI)?
                if(response != null) {
                    PrefConfig prefConfig;

                    if((prefConfig = AuthActivity.PREF_CONFIG_REFERENCE.get()) != null) {
                        String responseCode = response.getResponse();

                        if(responseCode.equals("Image Uploaded")) {
                            Log.i("RegisterFragment", "uploadImageAndAuth —> Uploaded user image. Authenticating.");
                        } else if(responseCode.equals("Image Upload Failed")) {
                            Log.w("RegisterFragment", "uploadImageAndAuth —> Failed to upload user image. Authenticating.");
                            prefConfig.displayToast("Image upload failed.");
                        } else {
                            // internal server error
                            Log.w("RegisterFragment", "uploadImageAndAuth —> Failed to upload user image due to unknown issues (server error). Authenticating.");
                            prefConfig.displayToast("Couldn't upload image. " + responseCode);
                        }

                        onRegisterFormActivity.performAuthChange(
                                user
                        ); // switch to new activity after image is received (uploaded or failed)
                    }
                }
        });
    }
}
