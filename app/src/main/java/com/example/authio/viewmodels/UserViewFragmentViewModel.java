package com.example.authio.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.authio.models.User;
import com.example.authio.repositories.UserRepository;

import java.util.List;

public class UserViewFragmentViewModel extends ViewModel {
    private UserRepository userRepository;
    private MutableLiveData<List<User>> mUsers;

    public void init() {
        if(userRepository != null) {
            return;
        }

        userRepository = UserRepository.getInstance();
    }

    public LiveData<List<User>> getUsers(String token, String refreshToken) {
        return mUsers = userRepository.getUsers(token, refreshToken);
    }

    public void setUsers(MutableLiveData<List<User>> mUsers) {
        this.mUsers = mUsers;
    }
}
