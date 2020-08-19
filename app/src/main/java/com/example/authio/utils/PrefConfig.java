package com.example.authio.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.authio.R;

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

    public int readAuthUserId() {
        return sharedPreferences.getInt(context.getString(R.string.pref_id), -1);
    }

    public void writeAuthUserId(Integer id) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if(id == null) {
            editor.remove(context.getString(R.string.pref_id)).apply();
        } else {
            editor.putInt(context.getString(R.string.pref_id), id).apply();
        }
    }

    public void displayToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
