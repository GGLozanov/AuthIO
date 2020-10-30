package com.example.authio.fetchers;

import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.authio.executors.AppExecutors;

public abstract class NetworkBoundFetcher<NetworkModel, CacheEntity> {
    protected AppExecutors appExecutors;
    // this should be a networkModel return because viewmodel and view handles network model, not cache model
    protected MediatorLiveData<NetworkModel> results = new MediatorLiveData<>(); // final result from db after fetch checks and db interactions are completed

    public NetworkBoundFetcher(AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
        fetch();
    }

    @MainThread
    public abstract void fetch(); // main method for fetching called after constructor & overloaded by all child classes

    @MainThread // MainThread since threading is handled by Retrofit
    public abstract LiveData<NetworkModel> fetchFromNetwork(); // observe network results here and observe db once fetched

    @MainThread
    public abstract boolean shouldFetchFromNetwork();

    @MainThread
    public abstract LiveData<CacheEntity> loadFromDb();
    // upon critical failures in need of handling all the way to the views, this method should return null so that the fetch() method can invoke getCriticalFailureModel() as a result

    @WorkerThread
    public abstract void saveToDb(NetworkModel fetchedModel); // conversion to db entity done in specific implementation

    @MainThread
    public abstract NetworkModel entityToNetworkModel(CacheEntity entity);

    @MainThread
    public abstract boolean isNetworkModelInvalid(NetworkModel model);

    @MainThread
    public abstract NetworkModel getCriticalFailureModel();
    // method used to generate a NetworkModel specifically designed to generate the proper failure callbacks in the UI.
    // Used only when critical failure (like user needing to reauth immediately) and there is no way to be handled by fetchFromNetwork()
    // - therefore there is no server response to handle it back to the UI - and it needs to be handled by the NetworkBoundFetcher

    public MutableLiveData<NetworkModel> getAsLiveData() {
        return results;
    }
}
