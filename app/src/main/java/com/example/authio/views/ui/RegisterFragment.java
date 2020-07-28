package com.example.authio.views.ui;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.authio.R;
import com.example.authio.utils.PrefConfig;
import com.example.authio.viewmodels.RegisterFragmentViewModel;
import com.example.authio.views.activities.BaseActivity;
import com.example.authio.models.Image;
import com.example.authio.models.User;
import com.example.authio.api.OnAuthStateChanged;
import com.example.authio.utils.ImageUtils;
import com.example.authio.views.activities.MainActivity;

import java.io.IOException;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends AuthFragment {

    public interface OnRegisterFormActivity extends OnAuthStateChanged {
        void performToggleToLogin();
    }

    private static int IMG_REQUEST_CODE = 777; // request code for activities
    private Bitmap bitmap; // image represented as a bitmap to be decoded from intent result

    private EditText usernameInput, descriptionInput;
    private ImageView profileImage;
    private OnRegisterFormActivity onRegisterFormActivity;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(RegisterFragmentViewModel.class);
        viewModel.init();

        initAuthFields(view);
        initListeners((v) -> onRegisterFormActivity.performToggleToLogin(), (v) -> performRegister());

        usernameInput = view.findViewById(R.id.username_input_field);
        descriptionInput = view.findViewById(R.id.description_input_field);
        profileImage = view.findViewById(R.id.profile_image);

        profileImage.setOnClickListener((v) -> {
            // handle onClick for profile image by rendering new intent w/ "image/*" and GET_CONTENT type
            Intent selectImageIntent = new Intent();
            selectImageIntent.setType("image/*"); // set MIME data type to all images
            selectImageIntent.setAction(Intent.ACTION_GET_CONTENT); // set the desired action to get image
            startActivityForResult(selectImageIntent, IMG_REQUEST_CODE); // start activity and await result
        });

        return view;
    }

    // get image from upload here
    // upload it to server by encoding it to base64
    // add onClickListener for ImageView
    // handle if image is never clicked (profile pic is never changed), then use default_img image in query and data
    // download default_img image and put it into uploads

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // receive result from other activity after it finishes. . .
        Uri imagePath; // Uri for image

        if(requestCode == IMG_REQUEST_CODE &&
                resultCode == RESULT_OK &&
                data != null && (imagePath = data.getData()) != null) {
            ContentResolver contentResolver = getActivity()
                    .getContentResolver(); // provides access to content model (class used to interface and access the data)

            try {
                if(Build.VERSION.SDK_INT < 28) {
                    bitmap = MediaStore.Images.Media.getBitmap(
                            contentResolver,
                            imagePath
                    );
                } else {
                    bitmap = ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            contentResolver,
                            imagePath
                        )
                    );
                }
                profileImage.setImageBitmap(bitmap); // set the new image resource to be decoded from the bitmap
            } catch(IOException e) {
                Log.e("RegisterFragment: ", e.toString());
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        onRegisterFormActivity = (OnRegisterFormActivity) activity;
    }

    private void performRegister() {
        String email = emailInput.getText().toString(),
                password = passwordInput.getText().toString(),
                username = usernameInput.getText().toString(),
                description = descriptionInput.getText().toString();

        if(email.isEmpty() || password.isEmpty() || username.isEmpty() || description.isEmpty()) {
            showErrorMessage("Invalid info in fields!");
            return;
        }

        hideErrorMessage();


        ((RegisterFragmentViewModel) viewModel).getRegisterToken(email, username, password, description)
                .observe(this, (token) -> {
                    if(token != null) {
                        PrefConfig prefConfig;

                        if((prefConfig = BaseActivity.PREF_CONFIG_REFERENCE.get()) != null) {
                            String responseCode = token.getResponse();

                            if(responseCode.equals("ok")) {
                                prefConfig.writeToken(token.getJWT()); // write & save token
                                prefConfig.writeRefreshToken(token.getRefreshJWT()); // write & save refresh token

                                Integer userId = token.getUserId();

                                uploadImageAndAuth(new User(
                                        userId,
                                        responseCode,
                                        username,
                                        description,
                                        email
                                )); // go on to upload the image if the registration was successful
                            } else if(responseCode.equals("exists")) {
                                prefConfig.displayToast("User already exists!");
                            } else if(responseCode.equals("failed")) {
                                prefConfig.displayToast("Registration unsuccessful!");
                            } else {
                                // internal server error
                                prefConfig.displayToast(responseCode);
                            }
                        } else {
                            // TODO: Handle error
                        }
                    }
                });
    }

    private void uploadImageAndAuth(User user) {
        if(profileImage.getDrawable() ==
             ContextCompat.getDrawable(getContext(), R.drawable.default_img)) {
            return; // don't upload picture if it's the default
        }

        ((RegisterFragmentViewModel) viewModel).uploadUserImage(
                new Image(user.getId().toString(), null, ImageUtils.encodeImage(bitmap)))
                .observe(this, (response) -> {
                    // TODO: Extract handling in viewmodel (leave this to just update the UI)?
                if(response != null) {
                    PrefConfig prefConfig;

                    if((prefConfig = BaseActivity.PREF_CONFIG_REFERENCE.get()) != null) {
                        String responseCode = response.getResponse();

                        if(responseCode.equals("Image Uploaded")) {
                            prefConfig.displayToast("Image uploaded...");
                        } else if(responseCode.equals("Image Upload Failed")) {
                            prefConfig.displayToast("Image upload failed...");
                        } else {
                            // internal server error
                            prefConfig.displayToast("Image: Something went wrong... " + responseCode);
                        }

                        onRegisterFormActivity.performAuthChange(
                                user
                        ); // switch to welcome fragment after image is received (uploaded or failed)
                    }
                }
        });



    }
}
