package com.example.authio.repositories;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;

import com.example.authio.models.Model;
import com.example.authio.persistence.BaseDao;
import com.example.authio.shared.Constants;

import java.util.Collection;

public abstract class CacheRepository<T extends BaseDao> extends Repository {
    protected T dao;
    protected ConnectivityManager connectivityManager;

    public CacheRepository(T dao, ConnectivityManager connectivityManager) {
        this.dao = dao; // model in superclass isn't initialised here. . .
        this.connectivityManager = connectivityManager;
    }

    protected boolean isConnected() {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
