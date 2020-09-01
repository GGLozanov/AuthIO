package com.example.authio.viewmodels;

import android.graphics.Bitmap;
import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.authio.models.Image;
import com.example.authio.models.Model;
import com.example.authio.models.User;
import com.example.authio.repositories.ImageRepository;
import com.example.authio.repositories.UserRepository;

import java.util.Map;

/**
 * ProfileFragment ViewModel which can be used to bind all types of cards for single_user_view layout
 */
public class ProfileFragmentViewModel extends ViewModel {
    private ImageRepository imageRepository;
    private MutableLiveData<Model> mImage; // image response represented as model (with only response)

    private UserRepository userRepository;
    private MutableLiveData<User> mUser;

    private View.OnClickListener confirmChangesButtonListener;

    public void init() {
        if(mUser != null) {
            return;
        }

        imageRepository = ImageRepository.getInstance();
        userRepository = UserRepository.getInstance();
    }

    public LiveData<User> fetchUser(String token, String refreshToken) {
        return mUser = userRepository.getUser(token, refreshToken);
    }

    public LiveData<User> setUser(User user) {
        if(mUser == null) {
            mUser = new MutableLiveData<>();
        }
        mUser.setValue(user);
        return mUser;
    }

    public LiveData<Model> updateUser(String token, String refreshToken, Map<String, String> body) {
        return userRepository.updateUser(token, refreshToken, body);
    }

    public LiveData<Model> uploadImage(String token, String refreshToken, Image image) {
        return mImage = imageRepository.uploadImage(token, refreshToken, image);
    }

    public LiveData<User> getUser() {
        return mUser;
    }

    public View.OnClickListener getConfirmChangesButtonListener() {
        return confirmChangesButtonListener;
    }

    public void setConfirmChangesButtonListener(View.OnClickListener confirmChangesListener) {
        this.confirmChangesButtonListener = confirmChangesListener;
    }

}
