package com.loopme;

import com.loopme.common.ResponseParser;
import com.loopme.common.StaticParams;
import com.loopme.common.Utils;
import com.loopme.debugging.ErrorLog;
import com.loopme.debugging.Params;

public abstract class Settings {
    private static boolean sBackendAutoLoadingValue = true;
    private static boolean sUserAutoLoadingValue = true;
    private long mStartLoadingTime;
    private int mLoadCounter;
    private int mMissedShowCounter;
    private int mShowCounter;

    /**
     * Changes default value of time interval during which video file will be cached.
     * Default time interval is 32 hours.
     */
    public void setVideoCacheTimeInterval(long milliseconds) {
        if (milliseconds > 0) {
            StaticParams.CACHED_VIDEO_LIFE_TIME = milliseconds;
        }
    }

    /**
     * Defines, should use mobile network for caching video or not.
     * By default, video will not cache on mobile network (only on wi-fi)
     *
     * @param b - true if need to cache video on mobile network,
     *          false if need to cache video only on wi-fi network.
     */
    public void useMobileNetworkForCaching(boolean b) {
        StaticParams.USE_MOBILE_NETWORK_FOR_CACHING = b;
    }

    /**
     * Use it for figure out any problems during integration process.
     * We recommend to set it "false" after full integration and testing.
     * <p>
     * If true - all debug logs will be in Logcat.
     * If false - only main info logs will be in Logcat.
     */
    public void setDebugMode(boolean mode) {
        StaticParams.DEBUG_MODE = mode;
    }

    public boolean isAutoLoadingEnabled() {
        return sUserAutoLoadingValue && sBackendAutoLoadingValue && !ResponseParser.isApi19();
    }

    public static void setAutoLoading(boolean autoLoadingEnabled) {
        sUserAutoLoadingValue = autoLoadingEnabled;
    }

    protected static void setBackendAutoLoadingValue(boolean autoLoadingEnabled) {
        sBackendAutoLoadingValue = autoLoadingEnabled;

    }

    protected String getPassedTime() {
        double time = (double) (System.currentTimeMillis() - mStartLoadingTime) / 1000;
        return String.valueOf(Utils.formatTime(time));
    }

    protected void onLoad() {
        mStartLoadingTime = System.currentTimeMillis();
        mLoadCounter++;
    }

    protected void onShow() {
        if (isNeedSendShowEvent()) {
            ErrorLog.postDebugEvent(Params.SDK_SHOW, getPassedTime());
            resetCounters();
        }
    }

    protected void onMissShow() {
        if (isNeedSendMissedEvent()) {
            ErrorLog.postDebugEvent(Params.SDK_MISSED, getPassedTime());
            mMissedShowCounter++;
        }
    }

    protected void onLoadedSuccess() {
        ErrorLog.postDebugEvent(Params.SDK_READY, getPassedTime());
    }

    protected void onLoadFail() {
        resetCounters();
    }

    private void resetCounters() {
        mLoadCounter = 0;
        mShowCounter = 0;
        mMissedShowCounter = 0;
    }

    private boolean isNeedSendShowEvent() {
        return mLoadCounter != mShowCounter;
    }

    private boolean isNeedSendMissedEvent() {
        return mLoadCounter != mMissedShowCounter;
    }
}