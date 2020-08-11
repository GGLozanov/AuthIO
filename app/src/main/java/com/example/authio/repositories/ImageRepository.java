package com.example.authio.repositories;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.util.Log;

import androidx.core.os.HandlerCompat;
import androidx.lifecycle.MutableLiveData;

import com.example.authio.api.APIClient;
import com.example.authio.models.Image;
import com.example.authio.models.Model;
import com.example.authio.utils.ImageDownloader;
import com.example.authio.views.activities.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import android.os.Handler;

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

        // execute upload synchronously for the user to have image immediately rendered upon login
        // immediately join after start for synchronous execution

        return mModel;
    }

    // downloads the image asynchronously and returns the livedata reference used in the UI
    // (updates notifiers when async call completes)
    public MutableLiveData<Bitmap> downloadImage(Integer userId) {
        MutableLiveData<Bitmap> mBitmap = new MutableLiveData<>();

        new ImageDownloader(mBitmap).execute(APIClient.getBaseURL() +
                "uploads/" +
                userId +
                ".jpg");

        return mBitmap;
    }

}
