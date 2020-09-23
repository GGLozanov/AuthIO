package com.example.authio.views.ui.fragments;

import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.authio.R;
import com.example.authio.adapters.UserListViewAdapter;
import com.example.authio.shared.Constants;
import com.example.authio.viewmodels.UserViewFragmentViewModel;
import com.example.authio.views.activities.MainActivity;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserViewFragment extends MainFragment {

    private ListView usersList;

    public UserViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_user_view, container, false);

        UserViewFragmentViewModel userViewFragmentViewModel = new ViewModelProvider(requireActivity(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(UserViewFragmentViewModel.class);
        userViewFragmentViewModel.init();

        usersList = view.findViewById(R.id.users_list);

        if((prefConfig = MainActivity.PREF_CONFIG_REFERENCE.get()) != null) {
            userViewFragmentViewModel.getUsers()
                .observe(this, (users) -> {
                    if(users != null) {
                        if(users.size() >= 1 && users.get(0).getResponse() != null && users.get(0).getResponse().equals(Constants.REAUTH_FLAG)) {
                            // TODO: The only time this will reach here with caching is when the cache itself is empty or can't be fetched
                            // means failed response - something went wrong
                            Log.w("UserViewFragment", "Failed users response detected. Logging out auth user.");
                            onAuthStateReset.performAuthReset();
                            prefConfig.displayToast("Your session has expired or something might be wrong. Please login again.");
                            return;
                        }

                        // hide progress bar and show user list
                        disableProgressBar(view);
                        usersList.setVisibility(View.VISIBLE);

                        UserListViewAdapter userListViewAdapter;
                        if((userListViewAdapter = (UserListViewAdapter) usersList.getAdapter()) == null) {
                            usersList.setAdapter(new UserListViewAdapter(getContext(), R.layout.single_user_view, users));
                        } else {
                            userListViewAdapter
                                    .setUsers(users);
                        }
                        Log.i("UserViewFragment", "Updated UserViewFragment with new users: " + users);
                    } else {
                        Log.i("UserViewFragment", "No active users to observe.");
                    }
                });
        } else {
            Log.e("UserViewFragment", "Found no reference to sharedpreferences in UserViewFragment.");
        }

        return view;
    }
}