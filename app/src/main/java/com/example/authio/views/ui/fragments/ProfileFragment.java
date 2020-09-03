package com.example.authio.views.ui.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.example.authio.R;
import com.example.authio.databinding.SingleUserEditBinding;
import com.example.authio.models.Image;
import com.example.authio.shared.Callbacks;
import com.example.authio.utils.ImageUtils;
import com.example.authio.viewmodels.ProfileFragmentViewModel;
import com.example.authio.views.activities.MainActivity;
import com.example.authio.models.User;
import com.example.authio.views.custom.ErrableEditText;
import com.example.authio.views.ui.dialogs.AuthChangeDialogFragment;
import com.example.authio.views.ui.dialogs.EmailChangeDialogFragment;
import com.example.authio.views.ui.dialogs.PasswordChangeDialogFragment;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends MainFragment implements AuthChangeDialogFragment.OnDialogCriticalServerError {

    private ImageView profileImage;

    // FIXME grr, code duplication in ProfileFragment and RegisterFragment (where are mixins when you need them...)
    private static int IMG_REQUEST_CODE = 555;
    private Bitmap bitmap;
    private User fetchedUser;

    private ErrableEditText usernameEditText, descriptionEditText;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ProfileFragmentViewModel viewModel = new ViewModelProvider(requireActivity())
                .get(ProfileFragmentViewModel.class);
        viewModel.init();

        SingleUserEditBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.single_user_edit, container, false);

        View view = binding.getRoot();
            // always inflate this layout instead of the default one
            // when using data binding in a fragment -> this one includes the databinding config
            // otherwise your UI components will become unresponsive due to finding views in a mismatching view (not that from the data binding)

        usernameEditText = ((ErrableEditText) view.findViewById(R.id.username_edit))
                            .asUsername();
        descriptionEditText = ((ErrableEditText) view.findViewById(R.id.description_edit))
                            .asDescription();

        viewModel.setChangeEmailButtonListener((v) -> {
            EmailChangeDialogFragment emailChangeDialogFragment = new EmailChangeDialogFragment();
            emailChangeDialogFragment.setTargetFragment(this, 400);
            assert getFragmentManager() != null;
            emailChangeDialogFragment.show(getFragmentManager(), "ChangeEmail");
        });

        viewModel.setChangePasswordButtonListener((v) -> {
            PasswordChangeDialogFragment passwordChangeDialogFragment = new PasswordChangeDialogFragment();
            passwordChangeDialogFragment.setTargetFragment(this, 300);
            assert getFragmentManager() != null;
            passwordChangeDialogFragment.show(getFragmentManager(), "ChangePassword");
        });

        viewModel.setConfirmChangesButtonListener((v) -> {
            // if bitmap is not null, then onActivityResult was called => pfp was changed
            if((prefConfig = MainActivity.PREF_CONFIG_REFERENCE.get()) != null) {
                String token = prefConfig.readToken();
                String refreshToken = prefConfig.readRefreshToken();

                if(usernameEditText.setErrorTextIfError() | descriptionEditText.setErrorTextIfError()) {
                    return;
                }

                if(bitmap != null) {
                    viewModel.uploadImage(
                            token,
                            refreshToken,
                            new Image(null, ImageUtils.encodeImage(bitmap))
                    );
                }

                Map<String, String> editUserBody = new HashMap<>();

                String username;
                if(!(username = Objects.requireNonNull(usernameEditText.getText()).toString())
                        .equals(fetchedUser.getUsername())) {
                    editUserBody.put("username", username);
                }

                String description;
                if(!(description = Objects.requireNonNull(descriptionEditText.getText()).toString())
                        .equals(fetchedUser.getDescription())) {
                    editUserBody.put("description", description);
                }

                if(!editUserBody.isEmpty()) {
                    viewModel.updateUser(token, refreshToken, editUserBody)
                        .observe(this, (result) -> {
                            if(result.getResponse().equals("ok")) {
                                // TODO: Update user LiveData or not? Wakes up unnecessary observers but can help with possible future observers outside this fragment
                                prefConfig.displayToast("User successfully updated!");
                                v.setEnabled(false); // disable button (antispam)
                                v.postDelayed(() -> // append delayed message to internal handler's message queue to reenable the button
                                    v.setEnabled(true), 1000*5); // reenable after delay
                            } else {
                                Log.w("ProfileFragment", "Failed to edit user -> " + result.getResponse());
                                prefConfig.displayToast("Couldn't update user! Please login again!");
                                onAuthStateReset.performAuthReset(); // TODO: Specify errors; may prove to be too ambiguous
                            }
                        });
                }
            } else {
                Log.e("ProfileFragment", "Found no reference to sharedpreferences in WelcomeFragment.");
                onAuthStateReset.performAuthReset();
            }
        });

        binding.setLifecycleOwner(this); // important for livedata (due to it being lifecycle aware)
        binding.setViewmodel(viewModel); // set the property in the data-bound xml as to access its props there

        profileImage = view.findViewById(R.id.profile_image);

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
            Log.e("ProfileFragment", "Found no reference to sharedpreferences in WelcomeFragment.");
            onAuthStateReset.performAuthReset();
        }

        return view;
    }

    private void handleObservedUser(User user) {
        if(user != null) {
            fetchedUser = user;

            String responseCode = user.getResponse();

            String photoUrl;
            if(responseCode.equals("ok") && (photoUrl = user.getPhotoUrl()) != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .signature(new ObjectKey(System.currentTimeMillis() / 60 * 10 * 1000))
                            // objectkey to give different signature to each cached image (differentiate updated pfps)
                            // objectkey that uses different signature each 10 minutes
                        .into(profileImage); // Glide makes image loading and caching a dream - no AsyncTasks or Lrucaches here!

                profileImage.setOnClickListener((v) -> {
                    Log.i("ProfileFragment", "ProfileImageOnClickListener —> User is selecting their profile picture");

                    Intent selectImageIntent = new Intent();
                    selectImageIntent.setType("image/*");
                    selectImageIntent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(selectImageIntent, IMG_REQUEST_CODE);
                }); // set the ability for user to change their pfp now
            } else if(responseCode.equals("Reauth")) {
                onAuthStateReset.performAuthReset();
            } else if(responseCode.contains("Failed: ")) {
                displayErrorAndReauth(responseCode.split("Failed: ")[0]);
            }
        } else {
            Log.w("ProfileFragment", "No user found to be observed");
        }
    }

    private void displayErrorAndReauth(String error) {
        prefConfig.displayToast(error);
        onAuthStateReset.performAuthReset(); // logout upon failure
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if((bitmap = Callbacks.getBitmapFromImageOnActivityResult(
                getActivity(),
                IMG_REQUEST_CODE,
                requestCode,
                resultCode,
                data)) != null) {
            profileImage.setImageBitmap(
                    bitmap
            ); // set the new image resource to be decoded from the bitmap
        }
    }

    @Override
    public void onDialogCriticalServerError(String errorText) {
        prefConfig.displayToast(errorText);
        onAuthStateReset.performAuthReset();
    }
}
