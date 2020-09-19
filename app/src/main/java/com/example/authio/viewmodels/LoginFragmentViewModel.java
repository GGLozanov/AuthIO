package com.example.authio.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.authio.models.Token;

public class LoginFragmentViewModel extends AuthFragmentViewModel {

    public LoginFragmentViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Token> getLoginToken(String email, String password) {
        return mToken = tokenRepository.getTokenOnLogin(email, password);
    }
}
