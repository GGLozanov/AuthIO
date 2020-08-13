package com.example.authio.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.authio.models.Token;
import com.example.authio.repositories.TokenRepository;

public class AuthFragmentViewModel extends ViewModel  {
    protected MutableLiveData<Token> mToken;
    protected TokenRepository tokenRepo;

    public void init() {
        if(mToken != null) {
            return;
        }

        tokenRepo = TokenRepository.getInstance();
    }
}
