package com.example.authio.ui;


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

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.authio.R;
import com.example.authio.activities.MainActivity;
import com.example.authio.api.ImageModel;
import com.example.authio.api.UserModel;
import com.example.authio.api.OnAuthStateChanged;
import com.example.authio.utils.ImageUtils;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
            // TODO: add error message to be displayed
            showErrorMessage("Invalid info in fields!");
            return;
        }

        hideErrorMessage();

        // TODO: Hash password
        Call<UserModel> authResult = MainActivity
                .API_OPERATIONS
                .performRegistration(
                        email,
                        password,
                        username,
                        description
                );

        authResult.enqueue(new Callback<UserModel>() {
            @Override
            public void onResponse(Call<UserModel> call, Response<UserModel> response) {
                // handle application-level errors intended from HTTP response here...
                if(response.isSuccessful()) {
                    UserModel body = response.body();
                    String responseCode = body.getResponse();

                    if(responseCode.equals("ok")) {
                        MainActivity.PREF_CONFIG.displayToast("Registration successful...");

                        Integer userId = body.getId();

                        uploadImage(userId); // go on to upload the image if the registration was successful

                        onRegisterFormActivity.performAuthChange(
                                new UserModel(
                                        userId,
                                        responseCode,
                                        username,
                                        description,
                                        email
                                )
                        ); // switch to welcome fragment after image is uploaded
                    } else if(responseCode.equals("exists")) {
                        MainActivity.PREF_CONFIG.displayToast("User already exists...");
                    } else if(responseCode.equals("failed")) {
                        MainActivity.PREF_CONFIG.displayToast("Registration unsuccessful...");
                    }
                } else {
                    MainActivity.PREF_CONFIG.displayToast("Something went wrong...");
                }
            }

            @Override
            public void onFailure(Call<UserModel> call, Throwable t) {
                // handle failed HTTP response receiving due to server-side exception here
                MainActivity.PREF_CONFIG.displayToast(t.getMessage());
            }
        });
    }

    private void uploadImage(Integer userId) {
        if(profileImage.getDrawable() ==
             ContextCompat.getDrawable(getContext(), R.drawable.default_img)) {
            return; // don't upload picture if it's the default
        }

        String image = ImageUtils.encodeImage(bitmap);

        Call<ImageModel> imageUploadResult = MainActivity
                .API_OPERATIONS
                .performImageUpload(
                    userId.toString(),
                    image
                );

        AtomicReference<Response<ImageModel>> atomicResponse = new AtomicReference<>();

        // TODO: Convert this to asynchronous execution and have AsyncTask in WelcomeFragment wait for this thread's execution (wait/notify)
        Thread syncUploadThread = new Thread(() -> {
            try {
                atomicResponse.set(imageUploadResult.execute());
            } catch (IOException e) {
                Log.e("RegisterFragment Img", e.toString());
            }
        });

        syncUploadThread.start();

        try {
            syncUploadThread.join();
        } catch (InterruptedException e) {
            Log.e("RegisterFragment Img", e.toString());
        }
        // execute upload synchronously for the user to have image immediately rendered upon login
        // immediately join after start for synchronous execution

        Response<ImageModel> response = atomicResponse.get();

        if(response.isSuccessful()) {
            ImageModel body = response.body();
            String responseCode = body.getResponse();

            if(responseCode.equals("Image Uploaded")) {
                MainActivity.PREF_CONFIG.displayToast("Image uploaded...");
            } else if(responseCode.equals("Image Upload Failed")) {
                MainActivity.PREF_CONFIG.displayToast("Image upload failed...");
            }
        } else {
            MainActivity.PREF_CONFIG.displayToast("Something went wrong...");
        }
    }
}
