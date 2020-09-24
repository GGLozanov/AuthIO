package com.example.authio.views.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.authio.R;
import com.example.authio.utils.PrefConfig;

import java.lang.ref.WeakReference;

public abstract class BaseActivity extends AppCompatActivity {
    public static WeakReference<PrefConfig> PREF_CONFIG_REFERENCE;
        // weak reference due to requiring activity context => avoid memory leak and allow to be cleaned up by garbage collection appropriately
    protected PrefConfig prefConfig;

    public PrefConfig getPrefConfig() {
        return prefConfig;
    }

    public void setPrefConfig(PrefConfig prefConfig) {
        this.prefConfig = prefConfig;
    }

    protected void replaceCurrentFragment(Fragment fragmentReplacement) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragmentReplacement).commitNow();
    }

}
