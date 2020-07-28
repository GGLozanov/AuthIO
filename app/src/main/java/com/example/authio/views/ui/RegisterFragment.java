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
import com.example.authio.models.Image;
import com.example.authio.models.Token;
import com.example.authio.models.User;
import com.example.authio.api.OnAuthStateChanged;
import com.example.authio.utils.ImageUtils;
import com.example.authio.utils.NetworkUtils;

import java.io.IOException;

import org.json.JSONException;

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
            showErrorMessage("Invalid info in fields!");
            return;
        }

        hideErrorMessage();

        Call<Token> authResult = MainActivity
                .API_OPERATIONS
                .performRegistration(
                        email,
                        username,
                        password,
                        description
                );

        authResult.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                // handle application-level errors intended from HTTP response here...
                Token token;
                String responseCode;

                if(response.isSuccessful() && (token = response.body()) != null) {
                    responseCode = token.getResponse();

                    if(responseCode.equals("ok")) {
                        MainActivity.PREF_CONFIG.writeToken(token.getJWT()); // write & save token
                        MainActivity.PREF_CONFIG.writeRefreshToken(token.getRefreshJWT()); // write & save refresh token

                        Integer userId = token.getUserId();

                        uploadImageAndAuth(new User(
                                userId,
                                responseCode,
                                username,
                                description,
                                email
                        )); // go on to upload the image if the registration was successful

                    }
                } else {
                    try {
                        responseCode = NetworkUtils.
                                extractResponseFromResponseErrorBody(response, "response");
                    } catch (JSONException | IOException | NetworkUtils.ResponseSuccessfulException e) {
                        Log.e("RegisterFrag JSON parse", e.toString());
                        MainActivity.PREF_CONFIG.displayToast("Bad server response!");
                        return;
                    }

                    if(responseCode.equals("exists")) {
                        MainActivity.PREF_CONFIG.displayToast("User already exists!");
                    } else if(responseCode.equals("failed")) {
                        MainActivity.PREF_CONFIG.displayToast("Registration unsuccessful!");
                    }
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {
                // handle failed HTTP response receiving due to server-side exception here
                MainActivity.PREF_CONFIG.displayToast(t.getMessage());
            }
        });
    }

    private void uploadImageAndAuth(User user) {
        if(profileImage.getDrawable() ==
             ContextCompat.getDrawable(getContext(), R.drawable.default_img)) {
            return; // don't upload picture if it's the default
        }

        String image = ImageUtils.encodeImage(bitmap);

        Call<Image> imageUploadResult = MainActivity
                .API_OPERATIONS
                .performImageUpload(
                    user.getId().toString(),
                    image
                );


        imageUploadResult.enqueue(new Callback<Image>() {
            @Override
            public void onResponse(Call<Image> call, Response<Image> response) {
                Image image;
                if(response.isSuccessful() && (image = response.body()) != null) {
                    String responseCode = image.getResponse();

                    if(responseCode.equals("Image Uploaded")) {
                        MainActivity.PREF_CONFIG.displayToast("Image uploaded...");
                    } else if(responseCode.equals("Image Upload Failed")) {
                        MainActivity.PREF_CONFIG.displayToast("Image upload failed...");
                    }

                    onRegisterFormActivity.performAuthChange(
                            user
                    ); // switch to welcome fragment after image is received (uploaded or failed)
                } else {
                    MainActivity.PREF_CONFIG.displayToast("Image: Something went wrong... " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Image> call, Throwable t) {
                MainActivity.PREF_CONFIG.displayToast("Something went wrong " + t.toString());
            }
        });

        // TODO: Convert this to asynchronous execution and have AsyncTask in WelcomeFragment wait for this thread's execution (wait/notify)
        // execute upload synchronously for the user to have image immediately rendered upon login
        // immediately join after start for synchronous execution

    }
}
