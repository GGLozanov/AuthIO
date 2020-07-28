package com.example.authio.repositories;

import androidx.lifecycle.MutableLiveData;

import com.example.authio.models.Image;
import com.example.authio.models.Model;
import com.example.authio.views.activities.MainActivity;

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

    public MutableLiveData<Model> uploadImage(Image image) {
        if(image == null) {
            throw new IllegalArgumentException();
        }

        Call<Model> imageUploadResult = API_OPERATIONS
                .performImageUpload(
                        image.getTitle(),
                        image.getImage()
                );

        final MutableLiveData<Model> mModel = new MutableLiveData<>();
        imageUploadResult.enqueue(new Callback<Model>() {
            @Override
            public void onResponse(Call<Model> call, Response<Model> response) {
                Model model;
                if(response.isSuccessful() && (model = response.body()) != null) {
                    mModel.setValue(model); // will notify observers once set from async call again here
                } else {
                    mModel.setValue(Model.asFailed(response.message()));
                }
            }

            @Override
            public void onFailure(Call<Model> call, Throwable t) {
                mModel.setValue(Model.asFailed(t.getMessage()));
            }
        });

        // TODO: Convert this to asynchronous execution and have AsyncTask in WelcomeFragment wait for this thread's execution (wait/notify)
        // execute upload synchronously for the user to have image immediately rendered upon login
        // immediately join after start for synchronous execution

        return mModel;
    }
}
