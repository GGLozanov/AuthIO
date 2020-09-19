package com.example.authio.fetchers;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.authio.executors.AppExecutors;

/**
 * Class used to fetch data for models that have both a remote and local source. Acts as a mediator between the two data sources.
 * This class takes two type parameters.
 * They are correlating models - one Domain and one Database - and the class uses them to retrieve the necessary data
 * from either the Room cache or the network
 * The MediatorLiveData instance always returns the initial network model containing the response and Room entity
 * @param <NetworkModel>
 * @param <CacheEntity>
 */
public abstract class NetworkBoundModelFetcher<NetworkModel, CacheEntity>
        extends NetworkBoundFetcher<NetworkModel, CacheEntity> {

    public NetworkBoundModelFetcher(AppExecutors appExecutorsInstance) {
        super(appExecutorsInstance);
    }

    @Override
    public void fetch() {
        Log.i("NetworkBoundMFetcher", "fetch —> Beginning to fetch model (network bound-ly)");

        results.setValue(null); // set initial state to loading (fetching) = null
        LiveData<CacheEntity> dbResult = loadFromDb();

        // TODO: might be useless if loadfromdb is async
        if(dbResult == null) { // if the livedata is utterly null, there is an error which needs to be handled by the top layer of the app
            Log.w("NetworkBoundMFetcher", "fetch —> Critical error required to be handled on top architecture layer found (most likely expired JWT). Suspend all fetch operations");
            results.setValue(getCriticalFailureModel());
            return;
        }

        if(shouldFetchFromNetwork()) {
            Log.i("NetworkBoundMFetcher", "fetch —> shouldFetch() check passed. Fetching API resource");
            LiveData<NetworkModel> fetchResult = fetchFromNetwork();
            results.addSource(fetchResult, networkModel -> {
                Log.i("NetworkBoundMFetcher", "fetch —> networkFetchResult —> Received result from network & saving it to db");
                results.removeSource(fetchResult); // stop observing the network source

                if(isNetworkModelInvalid(networkModel)) {
                    // network model is invalid (response might have failed)
                    // load data from cache and don't listen to db or network anymore
                    Log.i("NetworkBoundMFetcher", "fetch —> networkFetchResult —> Received result from network is invalid. Loading cache instead.");
                    results.addSource(dbResult, (dbEntity) -> {
                        // TODO: There might be needless overloading here
                        results.removeSource(dbResult); // stop listening to the db (request has failed, no need to listen for updates)

                        Log.i("NetworkBoundMFetcher", "fetch —> networkFetchResult —> Fetched cache from Room. Converting to network model & sending to ViewModel");

                        results.setValue(entityToNetworkModel(dbEntity)); // reload from cache after user has received network error
                    });
                    return;
                }

                Log.i("NetworkBoundMFetcher", "fetch —> networkFetchResult —> Received result from network is valid. Saving to Room & listening to db for changes.");
                // save fetched results to db
                appExecutors.executeOnDiskIO(() -> {
                    // these should always be castable (User -> UserEntity; List<User> -> List<UserEntity)
                    saveToDb(networkModel); // entity should be initialised by Retrofit from GSON conversion

                    appExecutors.executeOnMainThread(() -> {
                        // observe db again with new method call (new information). . .
                        // done when everything has been saved to db

                        LiveData<CacheEntity> updatedDbResult = loadFromDb();

                        if(updatedDbResult == null) {
                            Log.w("NetworkBoundMFetcher", "fetch —> networkFetchResult —> saveToDb call —> Critical error required to be handled on top architecture layer found (most likely expired JWT). Suspend all fetch operations");
                            results.setValue(getCriticalFailureModel());
                            return;
                        }

                        results.addSource(updatedDbResult, (dbEntity) -> {
                            // continue listening to changes to the db. . .
                            Log.i("NetworkBoundMFetcher", "fetch —> networkFetchResult —> cacheResult —> Received result from db loading & using new cache as SSOT.");
                            results.setValue(
                                    entityToNetworkModel(dbEntity)
                            );
                        });
                    });
                });
            }); // suppress workerthread for this use case because retrofit
        } else {
            Log.i("NetworkBoundMFetcher", "fetch —> shouldFetch() check not passed. Loading old cached data.");
            results.addSource(dbResult, (dbEntity) -> {
                Log.i("NetworkBoundMFetcher", "fetch —> Fetched cache from Room. Converting to network model & sending to ViewModel");
                results.setValue(entityToNetworkModel(dbEntity)); // continue observing the db for changes. . .
            });
        }
    }
}
