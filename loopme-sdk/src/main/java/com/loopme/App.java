package com.loopme;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

/**
 * Created by katerina on 2/1/17.
 */

public class App extends MultiDexApplication {

    private static final String LOG_TAG = App.class.getSimpleName();
    private static App sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }


    public Context getAppContext() {
        return sInstance;
    }

}
