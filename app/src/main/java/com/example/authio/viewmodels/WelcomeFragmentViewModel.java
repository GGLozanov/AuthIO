package com.example.authio.viewmodels;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.authio.models.User;
import com.example.authio.repositories.ImageRepository;
import com.example.authio.repositories.UserRepository;

import java.io.InputStream;

public class WelcomeFragmentViewModel extends ViewModel {
    private UserRepository userRepository;
    private MutableLiveData<User> mUser;

    private ImageRepository imageRepository;
    private MutableLiveData<Bitmap> mImageBitmap;

    public void init() {
        if(mUser != null && mImageBitmap != null) {
            return;
        }

        imageRepository = ImageRepository.getInstance();
        userRepository = UserRepository.getInstance();
    }

    public LiveData<User> fetchUser(String token, String refreshToken) {
        return mUser = userRepository.getUser(token, refreshToken);
    }

    public LiveData<User> setUser(User user) {
        if(mUser == null) {
            mUser = new MutableLiveData<>();
        }
        mUser.setValue(user);
        return mUser;
    }

    public LiveData<User> getUser() {
        return mUser;
    }

    public User getUserValue() {
        return mUser != null ? mUser.getValue() : null;
    }

    public LiveData<Bitmap> getImageBitmap(Integer userId) {
        return mImageBitmap = imageRepository.downloadImage(userId);
    }

    public LiveData<Bitmap> setImageBitmap(Bitmap bitmap) {
        if(mImageBitmap == null) {
            mImageBitmap = new MutableLiveData<>();
        }
        mImageBitmap.setValue(bitmap);
        return mImageBitmap;
    }

    public Bitmap getImageBitmapValue() {
        return mImageBitmap != null ? mImageBitmap.getValue() : null;
    }
}
