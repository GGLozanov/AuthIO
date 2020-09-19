package com.example.authio.viewmodels;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.authio.models.User;
import com.example.authio.persistence.AppDatabase;
import com.example.authio.repositories.UserRepository;

import java.util.List;

public class UserViewFragmentViewModel extends AndroidViewModel {
    private UserRepository userRepository;
    private LiveData<List<User>> mUsers;

    public UserViewFragmentViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        if(userRepository != null) {
            return;
        }

        Context appContext = getApplication().getApplicationContext();

        userRepository = UserRepository.getInstance(
                AppDatabase.getInstance(appContext)
                .getUserDao(),
                (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE));
    }

    public LiveData<List<User>> getUsers() {
        return mUsers = userRepository.getUsers();
    }

    public void setUsers(LiveData<List<User>> mUsers) {
        this.mUsers = mUsers;
    }
}
