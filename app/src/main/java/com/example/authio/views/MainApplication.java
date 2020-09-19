package com.example.authio.views;

import android.content.Context;

public class MainApplication extends android.app.Application {
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
    }
}
