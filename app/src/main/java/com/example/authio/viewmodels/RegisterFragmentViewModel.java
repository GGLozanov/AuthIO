package com.example.authio.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.authio.models.Image;
import com.example.authio.models.Model;
import com.example.authio.models.Token;
import com.example.authio.repositories.ImageRepository;
import com.example.authio.repositories.TokenRepository;

public class RegisterFragmentViewModel extends AuthFragmentViewModel {
    private ImageRepository imageRepository;
    private MutableLiveData<Model> mImage; // image response represented as model (with only response)

    public LiveData<Token> getRegisterToken(String email, String username, String password, String description) {
        return mToken = tokenRepo.getTokenOnRegister(email, username, password, description);
    }

    @Override
    public void init() {
        super.init();
        if(mImage != null) {
            return;
        }

        imageRepository = ImageRepository.getInstance();
    }

    public LiveData<Model> uploadUserImage(Image image) {
        return mImage = imageRepository.uploadImage(image);
    }
}
