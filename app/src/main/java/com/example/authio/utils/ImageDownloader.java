package com.example.authio.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;


public class ImageDownloader extends AsyncTask<String, Integer, Bitmap> {
    private MutableLiveData<Bitmap> mImageBitmap;

    public ImageDownloader(MutableLiveData<Bitmap> mImageStream) {
        this.mImageBitmap = mImageStream;
    }

    // params are passed in execute() call; make sure it's only 1 - the URL path
    @Override
    protected Bitmap doInBackground(String... strings) {
        if(strings.length > 1) {
            return null; // unsupported length; can only accept url as parameter
        }

        URL url;

        try {
            url = new URL(strings[0]);
        } catch (MalformedURLException e) {
            Log.e("WelcomeFragment", e.toString());
            return null;
        }

        InputStream content;

        // runs getContent() asynchronously within the bounds of the AsyncTask
        try {
            content = (InputStream) url.getContent(); // get image displayed
        } catch (IOException e) {
            Log.e("WelcomeFragment", e.toString());
            return null;
        }

        return BitmapFactory.decodeStream(content); // get bitmap from inputstream
    }

    @Override
    protected void onPostExecute(Bitmap imageBitmap) {
        if(mImageBitmap != null && imageBitmap != null) {
            mImageBitmap.setValue(imageBitmap);
        }
    }
}
