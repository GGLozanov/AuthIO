package com.example.authio.utils;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageUtils {
    /**
     * Encodes a given bitmap (preferably for an image) to Base64
     * @return String — the encoded Base64 bitmap
     */
    public static String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // stream of bytes to represent the bitmap with
        bitmap.compress(Bitmap.CompressFormat.JPEG, Base64.DEFAULT, byteArrayOutputStream);
        // compress bitmap to JPEG w/ default_img base64 quality and into ByteArrayOutputStream

        byte[] imageByte = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageByte, Base64.DEFAULT);
        // encode byte array to string in Base64 w/ default_img flags
    }
}
