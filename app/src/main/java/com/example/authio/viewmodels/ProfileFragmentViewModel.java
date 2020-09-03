package com.example.authio.viewmodels;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.authio.models.Image;
import com.example.authio.models.Model;
import com.example.authio.models.Token;
import com.example.authio.models.User;
import com.example.authio.repositories.ImageRepository;
import com.example.authio.repositories.TokenRepository;
import com.example.authio.repositories.UserRepository;
import com.example.authio.views.ui.dialogs.EmailChangeDialogFragment;
import com.example.authio.views.ui.fragments.ProfileFragment;

import java.util.Map;

/**
 * ProfileFragment ViewModel which can be used to bind all types of cards for single_user_view layout
 * Also capable of login authentication interactions (extending LoginFragmentViewModel)
 * TODO: Change from extending this class; semantically and probably progmatically dumb
 */
public class ProfileFragmentViewModel extends LoginFragmentViewModel {
    private UserRepository userRepository;
    private MutableLiveData<User> mUser;

    private ImageRepository imageRepository;

    private View.OnClickListener confirmChangesButtonListener;
    private View.OnClickListener changeEmailButtonListener;
    private View.OnClickListener changePasswordButtonListener;

    public void init() {
        super.init();
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
        return imageRepository.uploadImage(token, refreshToken, image);
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

    public View.OnClickListener getChangeEmailButtonListener() {
        return changeEmailButtonListener;
    }

    public void setChangeEmailButtonListener(View.OnClickListener changeEmailButtonListener) {
        this.changeEmailButtonListener = changeEmailButtonListener;
    }

    public View.OnClickListener getChangePasswordButtonListener() {
        return changePasswordButtonListener;
    }

    public void setChangePasswordButtonListener(View.OnClickListener changePasswordButtonListener) {
        this.changePasswordButtonListener = changePasswordButtonListener;
    }
}
