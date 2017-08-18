package com.loopme;

import android.support.multidex.MultiDexApplication;

import com.loopme.common.Logging;
import com.moat.analytics.mobile.loo.MoatAnalytics;
import com.moat.analytics.mobile.loo.MoatOptions;

/**
 * Created by katerina on 2/1/17.
 */

public class App extends MultiDexApplication {

    private static final String LOG_TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        MoatAnalytics.getInstance().start(this);
        initMoatAnalytics();
    }

    private void initMoatAnalytics() {
        MoatOptions options = new MoatOptions();
        options.disableAdIdCollection = true;
        Logging.out(LOG_TAG, "initMoatAnalytics");
        MoatAnalytics.getInstance().start(options, this);
    }
}
