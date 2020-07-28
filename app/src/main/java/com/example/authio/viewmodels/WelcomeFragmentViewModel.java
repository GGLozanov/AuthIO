package com.example.authio.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.authio.models.User;
import com.example.authio.repositories.UserRepository;

public class WelcomeFragmentViewModel extends ViewModel {
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

    public User getUserValue() {
        return mUser != null ? mUser.getValue() : null;
    }
}
