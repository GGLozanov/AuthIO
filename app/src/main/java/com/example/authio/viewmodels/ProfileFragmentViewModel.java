package com.example.authio.viewmodels;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.authio.models.Image;
import com.example.authio.models.Model;
import com.example.authio.models.Token;
import com.example.authio.models.User;
import com.example.authio.persistence.AppDatabase;
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

    public ProfileFragmentViewModel(@NonNull Application application) {
        super(application);
    }

    public void init() {
        super.init();
        if(mUser != null) {
            return;
        }

        Context appContext = getApplication().getApplicationContext();

        imageRepository = ImageRepository.getInstance();
        userRepository = UserRepository.getInstance(
                AppDatabase.getInstance(appContext)
                        .getUserDao(),
                (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE)
        );
    }

    public LiveData<User> fetchUser() {
        return mUser = userRepository.getUser();
    }

    public LiveData<User> setUser(User user) {
        if(mUser == null) {
            mUser = new MutableLiveData<>();
        }
        mUser.setValue(user);
        return mUser;
    }

    public LiveData<Model> updateUser(Map<String, String> body) {
        return userRepository.updateUser(body);
    }

    public LiveData<Model> uploadImage(Image image) {
        return imageRepository.uploadImage(image);
    }

    public LiveData<User> getUser() {
        return mUser;
    }
}
