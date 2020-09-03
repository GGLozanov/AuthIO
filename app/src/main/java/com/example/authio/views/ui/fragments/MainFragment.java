package com.example.authio.views.ui.fragments;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.authio.api.OnAuthStateReset;
import com.example.authio.utils.PrefConfig;

public abstract class MainFragment extends Fragment {
    protected OnAuthStateReset onAuthStateReset; // listener for performing logout

    protected PrefConfig prefConfig;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        onAuthStateReset = (OnAuthStateReset) activity;
    }
}
