package com.example.authio.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.authio.models.Image;
import com.example.authio.models.Model;
import com.example.authio.models.Token;
import com.example.authio.repositories.ImageRepository;

public class RegisterFragmentViewModel extends AuthFragmentViewModel {
    private ImageRepository imageRepository;
    private MutableLiveData<Model> mImage; // image response represented as model (with only response)

    public RegisterFragmentViewModel(@NonNull Application application) {
        super(application);
    }

    @Override
    public void init() {
        super.init();
        if(mImage != null) {
            return;
        }

        imageRepository = ImageRepository.getInstance();
    }

    public LiveData<Token> getRegisterToken(String email, String username, String password, String description) {
        return mToken = tokenRepository.getTokenOnRegister(email, username, password, description);
    }

    public LiveData<Model> uploadUserImage(Image image) {
        // refreshToken can be null here since whenever this method is called from RegisterFragment, there is ALWAYS a new token
        return mImage = imageRepository.uploadImage(image);
    }
}
