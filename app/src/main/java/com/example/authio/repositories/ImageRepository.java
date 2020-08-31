package com.example.authio.repositories;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.authio.api.APIClient;
import com.example.authio.models.Image;
import com.example.authio.models.Model;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageRepository extends Repository<Image> { // designed to make an API call for image uploading
    private static ImageRepository instance; // singleton instance (singleton pattern per MVVM)

    public static ImageRepository getInstance() {
        if (instance == null) {
            instance = new ImageRepository();
        }

        return instance;
    }

    /**
     * Uploads a base64 encoded image to the server
     * @param image - Image model to be uploaded to the server
     * @return - MutableLiveData instance of the Model class (containing just a response) from the server
     */
    public MutableLiveData<Model> uploadImage(String token, Image image) {
        if(image == null) {
            throw new IllegalArgumentException();
        }

        Log.i("ImageRepository", "uploadImage —> Calling for image result from (upload) image endpoint");

        Call<Model> imageUploadResult = API_OPERATIONS
                .performImageUpload(
                        token,
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
                } else {
                    Log.i("ImageRepository", "uploadImage —> Image upload was not successful.");
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
}
