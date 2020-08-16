package com.example.authio.views.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.authio.R;
import com.example.authio.api.OnAuthStateReset;
import com.example.authio.databinding.SingleUserBinding;
import com.example.authio.utils.PrefConfig;
import com.example.authio.viewmodels.ProfileFragmentViewModel;
import com.example.authio.views.activities.MainActivity;
import com.example.authio.models.User;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    private OnAuthStateReset onAuthStateReset; // listener for performing logout

    private PrefConfig prefConfig;

    private ImageView profileImage;
    private Button logoutButton;

    private ProfileFragmentViewModel viewModel;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewModel = new ViewModelProvider(requireActivity())
                .get(ProfileFragmentViewModel.class);
        viewModel.init();

        SingleUserBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_profile, container, false);

        View view = binding.getRoot();
            // always inflate this layout instead of the default one
            // when using data binding in a fragment -> this one includes the databinding config
            // otherwise your UI components will become unresponsive due to finding views in a mismatching view (not that from the data binding)

        binding.setLifecycleOwner(this); // important for livedata (due to it being lifecycle aware)
        binding.setViewmodel(viewModel); // set the property in the data-bound xml as to access its props there

        profileImage = view.findViewById(R.id.profile_image);

        logoutButton = view.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener((v) -> onAuthStateReset.performAuthReset());

        if((prefConfig = MainActivity.PREF_CONFIG_REFERENCE.get()) != null) {
            Bundle args = getArguments();
            User passedUser;

            if(args != null && (passedUser = args.getParcelable("user")) != null) {
                viewModel.setUser(passedUser)
                        .observe(this, this::handleObservedUser); // set the observer for the user set livedata method
            } else {
                viewModel.fetchUser(prefConfig.readToken(), prefConfig.readRefreshToken())
                        .observe(this, this::handleObservedUser); // set the observer for the user fetch livedata method
            }
        } else {
            Log.e("No reference", "Found no reference to sharedpreferences in WelcomeFragment.");
            onAuthStateReset.performAuthReset();
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        onAuthStateReset = (OnAuthStateReset) activity;
    }

    private void handleObservedUser(User user) {
        if(user != null) {
            String responseCode = user.getResponse();

            if(responseCode.equals("ok")) {
                viewModel.getImageBitmap(user.getId())
                        .observe(this, (bitmap) ->
                                profileImage.setImageBitmap(bitmap));
            } else if(responseCode.equals("Reauth")) {
                onAuthStateReset.performAuthReset();
            } else if(responseCode.contains("Failed: ")) {
                displayErrorAndReauth(responseCode.split("Failed: ")[0]);
            }
        } else {
            Log.w("WelcomeFragment", "No user found to be observed");
        }
    }

    private void displayErrorAndReauth(String error) {
        prefConfig.displayToast(error);
        onAuthStateReset.performAuthReset(); // logout upon failure
    }
}
