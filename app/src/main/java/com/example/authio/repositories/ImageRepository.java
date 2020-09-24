package com.example.authio.repositories;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.authio.api.APIClient;
import com.example.authio.models.Image;
import com.example.authio.models.Model;
import com.example.authio.models.Token;
import com.example.authio.utils.NetworkUtils;
import com.example.authio.utils.PrefConfig;
import com.example.authio.views.activities.MainActivity;

import org.json.JSONException;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageRepository extends Repository { // designed to make an API call for image uploading
    private static ImageRepository instance; // singleton instance (singleton pattern per MVVM)

    public static ImageRepository getInstance() {
        if (instance == null) {
            instance = new ImageRepository();
        }

        return instance;
    }

    /**
     * Uploads a base64 encoded image to the server
     * Race condition for token is still possible here, therefore prefConfig is accessed
     * @param image - Image model to be uploaded to the server
     * @return - MutableLiveData instance of the Model class (containing just a response) from the server
     */
    public MutableLiveData<Model> uploadImage(Image image) {
        if(image == null) {
            throw new IllegalArgumentException();
        }

        PrefConfig prefConfig;
        if((prefConfig = MainActivity.PREF_CONFIG_REFERENCE.get()) != null) {
            Log.i("ImageRepository", "uploadImage —> Calling for image result from (upload) image endpoint");

            Call<Model> imageUploadResult = API_OPERATIONS
                    .performImageUpload(
                            prefConfig.readToken(),
                            image.getImage()
                    );

            final MutableLiveData<Model> mModel = new MutableLiveData<>();
            imageUploadResult.enqueue(new Callback<Model>() {
                @Override
                public void onResponse(Call<Model> call, Response<Model> response) {
                    Model model;
                    if(response.isSuccessful() && (model = response.body()) != null) {
                        Log.i("ImageRepository", "uploadImage —> Image upload was successful.");
                        mModel.setValue(model); // will notify observers once set from async call again here
                        return;
                    }

                    try {
                        NetworkUtils.handleFailedAuthorizedResponse(API_OPERATIONS, response, prefConfig.readRefreshToken(), new Callback<Token>() {
                            @Override
                            public void onResponse(Call<Token> call, Response<Token> response) {
                                String jwtToken;
                                if((jwtToken = NetworkUtils.getTokenFromRefreshResponse(response)) != null) {
                                    Log.i("UserRepository", "uploadImage —> Retrieved new token from refresh_token endpoint and retrying image request");
                                    prefConfig.writeToken(jwtToken);
                                    uploadImage(image);
                                } else {
                                    Log.i("UserRepository", "getUsers —> Retrieved new token from refresh_token endpoint but token is either invalid or expired. Reauth.");
                                    mModel.setValue(Model.asFailed(response.message()));
                                }
                            }

                            @Override
                            public void onFailure(Call<Token> call, Throwable t) {
                                Log.w("ImageRepository", "uploadImage —> Internal server error during new token retrieval.");
                                mModel.setValue(Model.asFailed(t.getMessage()));
                            }
                        });
                    } catch(JSONException | IOException |  NetworkUtils.ResponseSuccessfulException e) {
                        Log.e("ImageRepository", "uploadImage —> ImageRepository JSON parse for failed user response failed or response was successful." + e.toString());
                    } catch(NetworkUtils.InvalidTokenException e) {
                        Log.e("ImageRepository", "uploadImage —> UserRepository JSON parse for failed user response failed or response was successful." + e.toString());
                    } finally {
                        Log.w("ImageRepository", "uploadImage —> Image upload was not successful.");
                        mModel.setValue(Model.asFailed(response.message()));
                    }
                }

                @Override
                public void onFailure(Call<Model> call, Throwable t) {
                    Log.w("ImageRepository", "uploadImage —> Internal server error during image upload.");
                    mModel.setValue(Model.asFailed(t.getMessage()));
                }
            });

            // execute upload synchronously for the user to have image immediately rendered upon login
            // immediately join after start for synchronous execution

            return mModel;
        }

        Log.e("ImageRepository", "");
        return null;
    }
}
