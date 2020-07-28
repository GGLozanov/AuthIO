package com.example.authio.views.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.authio.utils.PrefConfig;

import java.lang.ref.WeakReference;

public abstract class BaseActivity extends AppCompatActivity {
    public static WeakReference<PrefConfig> PREF_CONFIG_REFERENCE; // weak reference due to requiring activity context => avoid memory leak

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PREF_CONFIG_REFERENCE = new WeakReference<>(new PrefConfig(this));
    }
}
