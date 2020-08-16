package com.example.authio.viewmodels;

import androidx.lifecycle.LiveData;

import com.example.authio.models.Token;

public class LoginFragmentViewModel extends AuthFragmentViewModel {

    public LiveData<Token> getLoginToken(String email, String password) {
        return mToken = tokenRepo.getTokenOnLogin(email, password);
    }
}
