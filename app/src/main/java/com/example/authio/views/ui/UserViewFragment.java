package com.example.authio.views.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.authio.R;
import com.example.authio.adapters.UserListViewAdapter;
import com.example.authio.models.User;
import com.example.authio.utils.PrefConfig;
import com.example.authio.viewmodels.UserViewFragmentViewModel;
import com.example.authio.views.activities.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserViewFragment extends Fragment {

    private ListView usersList;

    public UserViewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_user_view, container, false);

        UserViewFragmentViewModel userViewFragmentViewModel = new ViewModelProvider(requireActivity())
                .get(UserViewFragmentViewModel.class);
        userViewFragmentViewModel.init();

        usersList = view.findViewById(R.id.users_list);

        PrefConfig prefConfig;
        if((prefConfig = MainActivity.PREF_CONFIG_REFERENCE.get()) != null) {
            userViewFragmentViewModel.getUsers(
                    prefConfig.readToken(), prefConfig.readRefreshToken(),
                    prefConfig.readAuthUserId())
                .observe(this, (users) -> {
                    if(users != null) {
                        UserListViewAdapter userListViewAdapter;
                        if((userListViewAdapter = (UserListViewAdapter) usersList.getAdapter()) == null) {
                            usersList.setAdapter(new UserListViewAdapter(getContext(), R.layout.single_user, users));
                        } else {
                            userListViewAdapter
                                    .setUsers(users);
                        }
                    } // TODO: Add some sort of error handling here (and some way to specify errors apart from null)
                });

        } else {
            Log.e("No reference", "Found no reference to sharedpreferences in UserViewFragment.");
        }

        return view;
    }
}