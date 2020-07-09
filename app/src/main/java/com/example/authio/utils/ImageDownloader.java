package com.example.authio.utils;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;


public class ImageDownloader extends AsyncTask<String, Integer, InputStream> {

    private WeakReference<ImageView> imageViewReference;
        // weak reference because this obj's parent class may not be garbage collected
        // if this async task keeps running

    public ImageDownloader(ImageView imageView) {
        imageViewReference = new WeakReference<>(imageView);
    }

    // params are passed in execute() call; make sure it's only 1
    @Override
    protected InputStream doInBackground(String... strings) {
        if(strings.length > 1) {
            return null; // unsupported length; can only accept url as parameter
        }

        return NetworkUtils.getURLContent(strings[0]); // use url to download image
    }

    @Override
    protected void onPostExecute(InputStream inputStream) {
        if(imageViewReference != null && inputStream != null) {
            final ImageView imageView = imageViewReference.get();
            if(imageView != null) {
                imageView.setImageDrawable(Drawable.createFromStream(inputStream, "src"));
            }
        }
    }

}
