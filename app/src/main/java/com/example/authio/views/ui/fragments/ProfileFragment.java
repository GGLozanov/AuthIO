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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.example.authio.R;
import com.example.authio.databinding.SingleUserEditBinding;
import com.example.authio.models.Image;
import com.example.authio.persistence.UserEntity;
import com.example.authio.shared.Callbacks;
import com.example.authio.shared.Constants;
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
        ProfileFragmentViewModel viewModel = new ViewModelProvider(requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
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

        // Button initialisation
        view.findViewById(R.id.change_email).setOnClickListener((v) -> {
            EmailChangeDialogFragment emailChangeDialogFragment = new EmailChangeDialogFragment();
            emailChangeDialogFragment.setTargetFragment(this, 400);
            assert getFragmentManager() != null;
            emailChangeDialogFragment.show(getFragmentManager(), "ChangeEmail");
        });
        view.findViewById(R.id.change_password).setOnClickListener((v) -> {
            PasswordChangeDialogFragment passwordChangeDialogFragment = new PasswordChangeDialogFragment();
            passwordChangeDialogFragment.setTargetFragment(this, 300);
            assert getFragmentManager() != null;
            passwordChangeDialogFragment.show(getFragmentManager(), "ChangePassword");
        });
        view.findViewById(R.id.confirm_button).setOnClickListener((v) -> {
            // if bitmap is not null, then onActivityResult was called => pfp was changed

            if(usernameEditText.setErrorTextIfError() | descriptionEditText.setErrorTextIfError()) {
                return;
            }

            if(bitmap != null) {
                viewModel.uploadImage(
                        new Image(null, ImageUtils.encodeImage(bitmap))
                );
            }

            Map<String, String> editUserBody = new HashMap<>();

            UserEntity userEntity = fetchedUser.getEntity();

            String username;
            if(!(username = Objects.requireNonNull(usernameEditText.getText()).toString())
                    .equals(userEntity.getUsername())) {
                editUserBody.put(Constants.USERNAME, username);
            }

            String description;
            if(!(description = Objects.requireNonNull(descriptionEditText.getText()).toString())
                    .equals(userEntity.getDescription())) {
                editUserBody.put(Constants.DESCRIPTION, description);
            }

            if(!editUserBody.isEmpty()) {
                viewModel.updateUser(editUserBody)
                    .observe(this, (result) -> {
                        if(result.getResponse().equals(Constants.SUCCESS_RESPONSE)) {
                            // TODO: Update user LiveData or not? Wakes up unnecessary observers but can help with possible future observers outside this fragment
                            Toast.makeText(getContext(), "User successfully updated", Toast.LENGTH_LONG).show();
                            v.setEnabled(false); // disable button (antispam)
                            v.postDelayed(() -> // append delayed message to internal handler's message queue to reenable the button
                                    v.setEnabled(true), 1000*5); // reenable after delay
                        } else {
                            Log.w("ProfileFragment", "Failed to edit user -> " + result.getResponse());
                            Toast.makeText(getContext(),
                                    "Couldn't update user! Please check your connection or try logging in again!",
                                    Toast.LENGTH_LONG).show();

                            onAuthStateReset.performAuthReset(); // TODO: Specify errors; may prove to be too ambiguous
                        }
                    });
            }
        });

        // binding lifecycle config
        binding.setLifecycleOwner(this); // important for livedata (due to it being lifecycle aware)
        binding.setViewmodel(viewModel); // set the property in the data-bound xml as to access its props there

        profileImage = view.findViewById(R.id.profile_image);

        // user fetch
        Bundle args = getArguments();
        User passedUser;

        if(args != null && (passedUser = args.getParcelable("user")) != null) {
            viewModel.setUser(passedUser)
                    .observe(this, this::handleObservedUser); // set the observer for the user set livedata method
        } else {
            viewModel.fetchUser()
                    .observe(this, this::handleObservedUser); // set the observer for the user fetch livedata method
        }

        return view;
    }

    private void handleObservedUser(User user) {
        if(user != null) {
            fetchedUser = user;

            String responseCode = user.getResponse();

            String photoUrl;
            if(responseCode.equals(Constants.SUCCESS_RESPONSE) && (photoUrl = user.getEntity().getPhotoUrl()) != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.default_img)
                        .into(profileImage); // Glide makes image loading and caching a dream - no AsyncTasks or Lrucaches here!

                profileImage.setOnClickListener((v) -> {
                    Log.i("ProfileFragment", "ProfileImageOnClickListener —> User is selecting their profile picture");

                    Intent selectImageIntent = new Intent();
                    selectImageIntent.setType("image/*");
                    selectImageIntent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(selectImageIntent, IMG_REQUEST_CODE);
                }); // set the ability for user to change their pfp now
            } else if(responseCode.equals(Constants.REAUTH_FLAG)) {
                onAuthStateReset.performAuthReset();
            } else if(responseCode.contains(Constants.FAILED_FLAG)) {
                displayErrorAndReauth(responseCode.split(Constants.FAILED_FLAG)[0]);
            }
        } else {
            Log.w("ProfileFragment", "No user found to be observed");
        }
    }

    private void displayErrorAndReauth(String error) {
        Toast.makeText(getContext(),
                error,
                Toast.LENGTH_LONG).show();
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
        Toast.makeText(getContext(),
                errorText,
                Toast.LENGTH_LONG).show();
        onAuthStateReset.performAuthReset();
    }
}
