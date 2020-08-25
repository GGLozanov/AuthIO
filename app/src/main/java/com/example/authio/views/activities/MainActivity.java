package com.example.authio.views.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.authio.R;
import com.example.authio.adapters.FragmentPagerAdapter;
import com.example.authio.api.OnAuthStateReset;
import com.example.authio.utils.PrefConfig;
import com.example.authio.views.ui.ProfileFragment;
import com.example.authio.views.ui.UserViewFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.lang.ref.WeakReference;

public class MainActivity extends BaseActivity implements OnAuthStateReset,
        BottomNavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener {

    private BottomNavigationView bottomNavigationView;
    private MenuItem currentItem;
    private ViewPager viewPager;
    private FragmentPagerAdapter fragmentPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        viewPager = findViewById(R.id.view_pager);

        viewPager.setOffscreenPageLimit(2); // 2 pages max with saved state in-between
        viewPager.addOnPageChangeListener(this);

        // need to instantiate prefconfig reference here with derived activity-level context
        prefConfig = new PrefConfig(this);
        PREF_CONFIG_REFERENCE = new WeakReference<>(prefConfig);

        // TODO: Safeguard fragment instances with findFragmentByTag() calls before
        if(findViewById(R.id.fragment_container) != null) {

            // check if first instance; end fragment selection otherwise
            if(savedInstanceState != null) {
                return;
            }

            fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager(), 0);

            Bundle bundle = getIntent().getExtras();

            ProfileFragment profileFragment = new ProfileFragment();
            profileFragment.setArguments(bundle); // contains user instance; set the intent extras as fragment args

            fragmentPagerAdapter.addFragment(profileFragment);
            fragmentPagerAdapter.addFragment(new UserViewFragment());

            viewPager.setAdapter(fragmentPagerAdapter);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageScrollStateChanged(int state) {}

    @Override
    public void onPageSelected(int position) {
        Menu bottomNavMenu = bottomNavigationView.getMenu();

        if(currentItem != null) { // if there is an existing item chosen
            currentItem.setChecked(false); // deselect it
        } else {
            bottomNavMenu.getItem(0).setChecked(false); // deselect the default one (first)
        }

        currentItem = bottomNavMenu.getItem(position);

        currentItem.setChecked(true);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_view_self:
                viewPager.setCurrentItem(0);
                break;
            case R.id.action_view_users:
                viewPager.setCurrentItem(1);
                break;
        }

        return false;
    }

    @Override
    public void performAuthReset() {
        prefConfig.writeLoginStatus(false);
        prefConfig.writeToken(null);
        prefConfig.writeRefreshToken(null);

        Intent authActivityI = new Intent(this, AuthActivity.class);
        authActivityI.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // don't add the activity to the back stack through this flag
        startActivity(authActivityI);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // necessary override for fragment onActiviyResult. . .
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_appbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) { // common convention to use switch w/ menus. . .
            case R.id.action_logout:
                performAuthReset();
                break;
        }

        return true;
    }
}
