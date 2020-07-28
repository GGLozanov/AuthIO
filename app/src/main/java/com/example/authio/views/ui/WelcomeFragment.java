package com.example.authio.views.ui;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.authio.R;
import com.example.authio.databinding.FragmentWelcomeBinding;
import com.example.authio.utils.PrefConfig;
import com.example.authio.viewmodels.WelcomeFragmentViewModel;
import com.example.authio.views.activities.BaseActivity;
import com.example.authio.views.activities.MainActivity;
import com.example.authio.api.APIClient;
import com.example.authio.api.OnAuthStateChanged;
import com.example.authio.models.Token;
import com.example.authio.models.User;
import com.example.authio.utils.ImageDownloader;
import com.example.authio.utils.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class WelcomeFragment extends Fragment {
    private ImageView profileImage;
    private Button logoutButton;

    private OnAuthStateChanged onAuthStateChanged; // listener for performing logout

    private PrefConfig prefConfig;

    public WelcomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);

        WelcomeFragmentViewModel viewModel = new ViewModelProvider(requireActivity())
                .get(WelcomeFragmentViewModel.class);

        FragmentWelcomeBinding binding = DataBindingUtil.setContentView(getActivity(), R.layout.fragment_welcome); // autogenerated class, hooray!
        binding.setViewmodel(viewModel); // set the property in the data-bound xml as to access its props there
        binding.setLifecycleOwner(this); // important

        profileImage = view.findViewById(R.id.profile_image);

        logoutButton = view.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener((v) -> onAuthStateChanged.performAuthReset());


        if((prefConfig = BaseActivity.PREF_CONFIG_REFERENCE.get()) != null) {
            Bundle args = getArguments();
            User passedUser;

            if(args != null && (passedUser = args.getParcelable("user")) != null) {
                viewModel.setUser(passedUser).observe(this, (user) -> {
                    // TODO: Error check here -> displayAndReauth use here
                    renderProfileImage(user);
                }); // set the observer for the user set livedata method
            } else {
                viewModel.fetchUser(prefConfig.readToken(), prefConfig.readRefreshToken()).observe(this, (user) -> {
                    // TODO: Error check here -> displayAndReauth use here
                    renderProfileImage(user);
                }); // set the observer for the user fetch livedata method
            }
        } else {
            // TODO: Handle error
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        onAuthStateChanged = (OnAuthStateChanged) activity;
    }

    private void renderProfileImage(User user) {
        new ImageDownloader(profileImage).execute(APIClient.getBaseURL() +
                "uploads/" +
                user.getId() +
                ".jpg"); // start AsyncTask to asynchronously download and render image upon completion
    }

    private void displayErrorAndReauth(String error) {
        prefConfig.displayToast(error);
        onAuthStateChanged.performAuthReset(); // logout upon failure
    }
}
