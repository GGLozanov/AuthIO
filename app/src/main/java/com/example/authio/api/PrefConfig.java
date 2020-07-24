package com.example.authio.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.authio.R;

// TODO: Tear down this config and make WelcomeFragment retrieve user with API call each time (important)
// this cannot scale well so replace it

public class PrefConfig {
    private SharedPreferences sharedPreferences;
    private Context context; // context used for sharedpreferences init and access operations for string resources

    public PrefConfig(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(context.getString(R.string.pref_file), Context.MODE_PRIVATE);
    }

    public boolean readLoginStatus() {
        return sharedPreferences.getBoolean(context.getString(R.string.pref_login_status), false);
    }

    public void writeLoginStatus(boolean status) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean(context.getString(R.string.pref_login_status), status).apply();
    }

    public void writeToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(context.getString(R.string.pref_token), token).apply();
    }

    public String readToken() {
        return sharedPreferences.getString(context.getString(R.string.pref_token), "invalid");
    }

    public void writeRefreshToken(String refreshToken) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(context.getString(R.string.pref_refresh_token), refreshToken).apply();
    }

    public String readRefreshToken() {
        return sharedPreferences.getString(context.getString(R.string.pref_refresh_token), "invalid");
    }

    public void displayToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
