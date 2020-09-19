package com.example.authio.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.authio.models.Token;
import com.example.authio.repositories.TokenRepository;

public abstract class AuthFragmentViewModel extends AndroidViewModel {
    protected TokenRepository tokenRepository;
    protected MutableLiveData<Token> mToken;

    public AuthFragmentViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        if(mToken != null) {
            return;
        }

        tokenRepository = TokenRepository.getInstance();
    }
}
