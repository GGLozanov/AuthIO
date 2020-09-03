package com.example.authio.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.authio.models.Token;
import com.example.authio.repositories.TokenRepository;

public abstract class AuthFragmentViewModel extends ViewModel  {
    protected TokenRepository tokenRepository;
    protected MutableLiveData<Token> mToken;

    public void init() {
        if(mToken != null) {
            return;
        }

        tokenRepository = TokenRepository.getInstance();
    }
}
