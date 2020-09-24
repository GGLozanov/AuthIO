package com.example.authio.shared;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.authio.utils.PrefConfig;
import com.example.authio.views.activities.MainActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

// TODO: Maybe rename; dumb name
public class Callbacks {
    public static Bitmap getBitmapFromImageOnActivityResult(Activity activity,
                                                            int requiredRequestCode,
                                                            int requestCode, int resultCode, @Nullable Intent data) {
        Uri imagePath; // Uri for image

        if(requestCode == requiredRequestCode &&
                resultCode == Activity.RESULT_OK && data != null &&
                (imagePath = data.getData()) != null) {
            ContentResolver contentResolver = activity
                    .getContentResolver(); // provides access to content model (class used to interface and access the data)
            try {
                if(Build.VERSION.SDK_INT < 28) {
                    return MediaStore.Images.Media.getBitmap(
                            contentResolver,
                            imagePath
                    );
                } else {
                    return ImageDecoder.decodeBitmap(
                            ImageDecoder.createSource(
                                    contentResolver,
                                    imagePath
                            )
                    );
                }
            } catch(IOException e) {
                Log.e("Callbacks.imageCallback", "onActivityResult (image retrieval) " +
                        "with required request code " + requiredRequestCode + " —> " + e.toString());
            }
        }

        return null;
    }

   // TODO: Add DialogFragment loginToken & updateUser callback here
}
