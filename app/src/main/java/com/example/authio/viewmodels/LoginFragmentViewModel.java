package com.example.authio.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.authio.models.Token;

import com.example.authio.repositories.TokenRepository;

public class LoginFragmentViewModel extends AuthFragmentViewModel {

    public LiveData<Token> getLoginToken(String email, String password) {
        return mToken = tokenRepo.getTokenOnLogin(email, password);
    }
}
