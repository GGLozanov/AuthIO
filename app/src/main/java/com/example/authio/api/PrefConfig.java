package com.example.authio.api;

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

    public String readUsername() {
        return sharedPreferences.getString(context.getString(R.string.pref_username), "User");
    }

    public void writeUsername(String username) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(context.getString(R.string.pref_username), username).apply();
    }

    public String readDescription() {
        return sharedPreferences.getString(context.getString(R.string.pref_description), "Description");
    }

    public void writeDescription(String description) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(context.getString(R.string.pref_username), description).apply();
    }

    public String readEmail() {
        return sharedPreferences.getString(context.getString(R.string.pref_email), "Description");
    }

    public void writeEmail(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(context.getString(R.string.pref_email), email).apply();
    }

    public void displayToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
