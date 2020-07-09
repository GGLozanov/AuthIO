package com.example.authio.utils;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class NetworkUtils {
    public static InputStream getURLContent(String urlString) {
        URL url;

        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            Log.e("WelcomeFragment: ", e.toString());
            return null;
        }

        InputStream content;

        // runs getContent() asynchronously in the bounds of the AsyncTask
        try {
            content = (InputStream) url.getContent(); // get image displayed
        } catch (IOException e) {
            Log.e("WelcomeFragment: ", e.toString());
            return null;
        }

        return content;
    }
}
