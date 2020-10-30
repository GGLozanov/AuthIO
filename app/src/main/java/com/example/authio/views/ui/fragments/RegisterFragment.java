package com.example.authio.views.ui.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.Toast;

import com.example.authio.R;
import com.example.authio.api.APIClient;
import com.example.authio.shared.Callbacks;
import com.example.authio.shared.Constants;
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

        viewModel = new ViewModelProvider(requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
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
                        String responseCode = token.getResponse();

                        if(responseCode.equals(Constants.SUCCESS_RESPONSE)) {
                            Log.i("RegisterFragment", "performRegister —> Writing user JWT and refresh JWT to shraredpreferences.");

                            int userId = TokenUtils.getTokenUserIdFromPayload(token.getJWT()); // this NEVER throws ExpiredJWTException

                            User user = new User(
                                    userId,
                                    responseCode,
                                    username,
                                    description,
                                    email
                            );

                            // don't upload picture if it's the default
                            if(((BitmapDrawable) profileImage.getDrawable()).getBitmap() !=
                                    ((BitmapDrawable) Objects.requireNonNull(ContextCompat.getDrawable(
                                            Objects.requireNonNull(getContext()), R.drawable.default_img))).getBitmap()) {
                                Log.i("RegisterFragment", "performRegister —> User has chosen custom picture => proceed to upload to server.");
                                user.getEntity().setPhotoUrl(APIClient.getBaseURL() +
                                        "uploads/" +
                                        userId +
                                        ".jpg");
                                uploadImageAndAuth(user); // go on to upload the image if the registration was successful
                            } else {
                                Log.i("RegisterFragment", "performRegister —> User has chosen default picture => authenticate.");

                                onRegisterFormActivity.performAuthChange(
                                        user
                                ); // switch to new activity if no image left to upload

                                // can't extract auth trigger from if/else block because the other call is inside async callback closure
                            }
                        } else if(responseCode.equals(Constants.EXISTS_RESPONSE)) {
                            Log.w("RegisterFragment", "performRegister —> User resource already exists.");
                            Toast.makeText(getContext(), "User already exists!", Toast.LENGTH_LONG).show();
                        } else if(responseCode.equals(Constants.FAILED_RESPONSE)) {
                            Log.w("RegisterFragment", "performRegister —> User resource could not be created (invalid info).");
                            Toast.makeText(getContext(), "Registration unsuccessful!", Toast.LENGTH_LONG).show();
                        } else {
                            // internal server error
                            Log.w("RegisterFragment", "performRegister —> Failed to create user resource to unknown issues (server error).");
                            Toast.makeText(getContext(), "Registration failed! Please check your connection and try again!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e("RegisterFragment", "performRegister —> Found no reference to sharedpreferences in RegisterFragment.");
                    }
                });
    }

    private void uploadImageAndAuth(User user) {
        ((RegisterFragmentViewModel) viewModel).uploadUserImage(
                new Image(null, ImageUtils.encodeImage(bitmap)))
                .observe(this, (response) -> {
                    // TODO: Extract handling in viewmodel (leave this to just update the UI)?
                if(response != null) {
                    String responseCode = response.getResponse();

                    if(responseCode.equals(Constants.IMAGE_UPLOAD_SUCCESS_RESPONSE)) {
                        Log.i("RegisterFragment", "uploadImageAndAuth —> Uploaded user image. Authenticating.");
                    } else if(responseCode.equals(Constants.IMAGE_UPLOAD_FAILED_RESPONSE)) {
                        Log.w("RegisterFragment", "uploadImageAndAuth —> Failed to upload user image. Authenticating.");
                        Toast.makeText(getContext(), "Image upload failed.", Toast.LENGTH_LONG).show();
                    } else {
                        // internal server error
                        Log.w("RegisterFragment", "uploadImageAndAuth —> Failed to upload user image due to unknown issues (server error). Authenticating.");
                        Toast.makeText(getContext(), "Couldn't upload image. " + responseCode, Toast.LENGTH_LONG).show();
                    }

                    onRegisterFormActivity.performAuthChange(
                            user
                    ); // switch to new activity after image is received (uploaded or failed)
                }
        });
    }
}
