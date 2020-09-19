package com.example.authio.resources;

import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.authio.executors.AppExecutors;
import com.example.authio.models.CacheModel;
import com.example.authio.persistence.BaseEntity;
import com.example.authio.shared.Constants;

/**
 * . This class takes two type parameters.
 * They are correlating models - one Domain and one Database - and the class uses them to retrieve the necessary data
 * from either the Room cache or the network
 * The MediatorLiveData instance always returns the initial network model containing the response and Room entity
 * @param <NetworkModel>
 * @param <CacheEntity>
 */
public abstract class NetworkBoundModelFetcher<NetworkModel extends CacheModel<CacheEntity>, CacheEntity extends BaseEntity> {
    private AppExecutors appExecutors;
    // this should be a networkModel return because viewmodel and view handles network model, not cache model
    private MediatorLiveData<NetworkModel> results = new MediatorLiveData<>(); // final result from db after fetch checks and db interactions are completed

    public NetworkBoundModelFetcher(AppExecutors appExecutorsInstance) {
        this.appExecutors = appExecutorsInstance;

        results.setValue(null); // set initial state to loading (fetching) = null
        LiveData<CacheEntity> dbResult = loadFromDb();

        if(shouldFetch()) {
            LiveData<NetworkModel> fetchResult = fetchFromNetwork();
            results.addSource(fetchResult, (networkModel) -> {
                results.removeSource(fetchResult); // stop observing the network\

                if(networkModel.getResponse().equals(Constants.FAILED_RESPONSE)) {
                    results.setValue(
                            (NetworkModel) CacheModel.asFailed(Constants.FAILED_RESPONSE));
                    results.addSource(dbResult, (dbEntity) -> {
                        // TODO: There might be needless overloading here
                        results.removeSource(dbResult); // stop listening to the db (request has failed, no need to listen for updates)
                        results.setValue((NetworkModel) new CacheModel<>(Constants.SUCCESS_RESPONSE, dbEntity)); // reload from cache after user has received network error
                    });
                    return;
                }

                // save fetched results to db
                appExecutors.executeOnDiskIO(() -> {
                    saveToDb(networkModel.getEntity()); // entity should be initialised by Retrofit from GSON conversion
                });

                // observe db again with new method call (new information). . .
                // wakes up once fetching has finished on I/O thread
                results.addSource(loadFromDb(), (dbEntity) -> {
                    results.setValue(
                            (NetworkModel) new CacheModel<>(Constants.SUCCESS_RESPONSE, dbEntity)
                    ); // unsafe cast; might crash
                });
            });
        } else {
            results.addSource(dbResult, (dbEntity) -> {
                results.setValue((NetworkModel) new CacheModel<>(Constants.SUCCESS_RESPONSE, dbEntity)); // continue observing the db for changes. . .
            });
        }
    }

    @WorkerThread
    public abstract LiveData<NetworkModel> fetchFromNetwork(); // observe network results here and observe db once fetched

    @MainThread
    public abstract boolean shouldFetch();

    @MainThread
    public abstract LiveData<CacheEntity> loadFromDb();

    @WorkerThread
    public abstract void saveToDb(CacheEntity dbEntity);

    public LiveData<NetworkModel> getAsLiveData() {
        return results;
    }
}
