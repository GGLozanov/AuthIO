package com.example.authio.viewmodels;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.authio.models.User;
import com.example.authio.repositories.ImageRepository;
import com.example.authio.repositories.UserRepository;

/**
 * ProfileFragment ViewModel which can be used to bind all types of cards for single_user layout
 */
public class ProfileFragmentViewModel extends ViewModel {
    private UserRepository userRepository;
    private MutableLiveData<User> mUser;

    public void init() {
        if(mUser != null) {
            return;
        }

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
}
