package com.example.authio.executors;


import android.os.HandlerThread;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.os.Handler;

/**
 * Defined by a relatively common pattern for Android, this class makes use of the Executors library defined in normal Java
 * to make multithreading and the execution of background tasks easier and bundled in a nice little package.
 * This class contains the global executors for the app.
 * This class follows the singleton pattern.
 * Only need diskIO and mainThread executors; network asynchronous operations are handled by Retrofit
 */
public class AppExecutors {
    private static AppExecutors instance;

    private MainThreadExecutor mainThread; // rarely used due to everything already being on the main thread and using the main Handler
    private Executor diskIO;

    public AppExecutors(MainThreadExecutor mainThreadExecutor, Executor diskIO) {
        this.mainThread = mainThreadExecutor;
        this.diskIO = diskIO;
    }

    public synchronized static AppExecutors getInstance() { // allow only one thread to access an instance of the object at a time (lock it from mishandled use from other threads)
        if(instance == null) {
            instance = new AppExecutors(
                    new MainThreadExecutor(),
                    Executors.newSingleThreadExecutor()); // diskIO works on a single thread
        }

        return instance;
    }

    public void executeOnDiskIO(Runnable command) {
        diskIO.execute(command);
    }

    public void executeOnMainThread(Runnable command) {
        mainThread.execute(command);
    }

    public void executeOnMainThreadDelayed(Runnable command, long time) {
        mainThread.executeDelayed(command, time);
    }

    private static class MainThreadExecutor implements Executor {
        private Handler handler = new Handler(Looper.getMainLooper()); // get the main handler (using the main looper)

        @Override
        public void execute(Runnable command) {
            handler.post(command);
        }

        public void executeDelayed(Runnable command, long time) {
            handler.postDelayed(command, time);
        }
    }
} // beta-level Kotlin coroutines
