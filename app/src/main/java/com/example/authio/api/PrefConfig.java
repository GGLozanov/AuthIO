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

        editor.putString(context.getString(R.string.pref_description), description).apply();
    }

    public String readEmail() {
        return sharedPreferences.getString(context.getString(R.string.pref_email), "Email");
    }

    public Integer readId() {
        return sharedPreferences.getInt(context.getString(R.string.pref_id), 0);
    }

    public void writeId(Integer id) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(context.getString(R.string.pref_id), id).apply();
    }


    public void writeEmail(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(context.getString(R.string.pref_email), email).apply();
    }

    public void writeUserPrefs(Integer id, String email, String username, String description) {
        writeId(id);
        writeEmail(email);
        writeUsername(username);
        writeDescription(description);
    }

    public void resetUserPrefs() {
        writeEmail(null);
        writeUsername(null);
        writeDescription(null);
    }

    public void displayToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
