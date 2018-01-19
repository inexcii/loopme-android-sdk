package com.loopme;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.CountDownTimer;

import com.loopme.common.Logging;
import com.loopme.common.LoopMeError;
import com.loopme.common.ResponseParser;
import com.loopme.common.StaticParams;
import com.loopme.constants.AdFormat;
import com.loopme.debugging.ErrorLog;

/**
 * The `LoopMeInterstitial` class provides the facilities to display a full-screen ad
 * during natural transition points in your application.
 * <p>
 * It is recommended to implement `LoopMeInterstitial.Listener`
 * to stay informed about ad state changes,
 * such as when an ad has been loaded or has failed to load its content, when video ad has been watched completely,
 * when an ad has been presented or dismissed from the screen, and when an ad has expired or received a tap.
 */
public class LoopMeInterstitial extends Settings {
    public static final String TEST_PORT_INTERSTITIAL = "test_interstitial_p";
    public static final String TEST_LAND_INTERSTITIAL = "test_interstitial_l";

    private static final String LOG_TAG = LoopMeInterstitial.class.getSimpleName();
    private int mFailCounter;
    private String mAppKey;
    private Activity mActivity;
    private Listener mMainAdListener;
    private CountDownTimer mSleepLoadTimer;
    private LoopMeInterstitialGeneral mFirstInterstitial;
    private LoopMeInterstitialGeneral mSecondInterstitial;
    private boolean mIsLoadingPaused;
    private IntegrationType mIntegrationType = IntegrationType.NORMAL;

    /**
     * Creates new `LoopMeInterstitial` object with the given appKey
     *
     * @param activity - application context
     * @param appKey   - your app key
     * @throws IllegalArgumentException if any of parameters is null
     */
    public LoopMeInterstitial(Activity activity, String appKey) {
        this.mActivity = activity;
        this.mAppKey = appKey;
        mFirstInterstitial = LoopMeInterstitialGeneral.getInstance(appKey, activity);
    }

    /**
     * Getting already initialized ad object or create new one with specified appKey
     * Note: Returns null if Android version under 5.0
     *
     * @param appKey   - your app key
     * @param activity - Activity context
     * @return instance of LoopMeInterstitial
     */
    public static LoopMeInterstitial getInstance(String appKey, Activity activity) {
        if (Build.VERSION.SDK_INT < 21) {
            LoopMeError error = new LoopMeError("Unsupported android version. Loopme-sdk requires android API_LEVEL >= 21.");
            ErrorLog.post(error.getMessage());
            throw new UnsupportedClassVersionError(error.getMessage());
        }
        return new LoopMeInterstitial(activity, appKey);
    }

    /**
     * Shows interstitial.
     * Interstitial should be loaded and ready to be shown.
     * <p>
     * As a result you'll receive onLoopMeInterstitialShow() callback
     */
    public void show() {
        if (!isShowing()) {
            if (isReady(mFirstInterstitial)) {
                show(mFirstInterstitial);
            } else if (isReady(mSecondInterstitial)) {
                show(mSecondInterstitial);
            } else {
                onMissShow();
            }
        } else {
            Logging.out(LOG_TAG, "Interstitial is already presented on the screen");
        }
    }

    public void load(IntegrationType integrationType) {
        mIntegrationType = integrationType;
        mFirstInterstitial.setIntegrationType(mIntegrationType);
        load();
    }

    public void load() {
        if (isLoadingPaused()) {
            onAutoLoadPaused();
            return;
        }
        load(mFirstInterstitial);
        if (isAutoLoadingEnabled()) {
            initSecondInstance();
            load(mSecondInterstitial);
        }
    }

    private void initSecondInstance() {
        if (isAutoLoadingEnabled() && mSecondInterstitial == null) {
            mSecondInterstitial = LoopMeInterstitialGeneral.getInstance(mAppKey, mActivity);
            mSecondInterstitial.setIntegrationType(mIntegrationType);
            setListener(initInternalListener(), mSecondInterstitial);
        }
    }

    private void onAutoLoadPaused() {
        if (mMainAdListener != null) {
            mMainAdListener.onLoopMeInterstitialLoadFail(this, new LoopMeError("Paused by auto loading"));
        }
    }

    private boolean isLoadingPaused() {
        return isAutoLoadingEnabled() && mIsLoadingPaused;
    }

    /**
     * Indicates whether ad content was loaded successfully and ready to be displayed.
     * After you initialized a `LoopMeInterstitial`/`LoopMeBanner` object and triggered the `load` method,
     * this property will be set to TRUE on it's successful completion.
     * It is set to FALSE when loaded ad content has expired or already was presented,
     * in this case it requires next `load` method triggering
     */
    public boolean isReady() {
        return isReady(mFirstInterstitial) || isReady(mSecondInterstitial);
    }

    /**
     * Indicates whether `LoopMeInterstitial`/`LoopMeBanner` currently presented on screen.
     * Ad status will be set to `AdState.SHOWING` after trigger `show` method
     *
     * @return true - if ad presented on screen
     * false - if ad absent on scrren
     */
    public boolean isShowing() {
        return isShowing(mFirstInterstitial) || isShowing(mSecondInterstitial);
    }

    /**
     * Indicates whether `LoopMeInterstitial`/`LoopMeBanner` in "loading ad content" process.
     * Ad status will be set to `AdState.LOADING` after trigger `load` method
     *
     * @return true - if ad is loading now
     * false - if ad is not loading now
     */
    public boolean isLoading() {
        return isLoading(mFirstInterstitial) || isLoading(mSecondInterstitial);
    }

    /**
     * The appKey uniquely identifies your app to the LoopMe ad network.
     * To get an appKey visit the LoopMe Dashboard.
     */
    public String getAppKey() {
        return mAppKey;
    }

    public Context getContext() {
        return mActivity;
    }

    /**
     * Dismisses an interstitial ad
     * This method dismisses an interstitial ad and only if it is currently presented.
     * <p>
     * After it interstitial ad requires "loading process" to be ready for displaying
     * <p>
     * As a result you'll receive onLoopMeInterstitialHide() notification
     */
    public void dismiss() {
        dismiss(mFirstInterstitial);
        dismiss(mSecondInterstitial);
    }

    public int getAdFormat() {
        return AdFormat.INTERSTITIAL;
    }


    /**
     * NOTE: should be in UI thread
     */
    public void destroy() {
        destroyFirst();
        destroySecond();
        stopSleepLoadTimer();
    }

    /**
     * Sets listener in order to receive notifications during the loading/displaying ad processes
     *
     * @param listener - LoopMeInterstitial.Listener
     */
    public void setListener(Listener listener) {
        mMainAdListener = listener;
        setListener(initInternalListener(), mFirstInterstitial);
        setListener(initInternalListener(), mSecondInterstitial);
    }

    private LoopMeInterstitialGeneral.Listener initInternalListener() {
        return new LoopMeInterstitialGeneral.Listener() {
            @Override
            public void onLoopMeInterstitialLoadSuccess(LoopMeInterstitialGeneral interstitial) {
                if (mMainAdListener != null) {
                    mMainAdListener.onLoopMeInterstitialLoadSuccess(LoopMeInterstitial.this);
                }
                mFailCounter = 0;
                onLoadedSuccess();
            }

            @Override
            public void onLoopMeInterstitialLoadFail(LoopMeInterstitialGeneral interstitial, LoopMeError error) {
                if (mMainAdListener != null) {
                    mMainAdListener.onLoopMeInterstitialLoadFail(LoopMeInterstitial.this, error);
                }
                increaseFailCounter();
                onLoadFail();
            }

            @Override
            public void onLoopMeInterstitialShow(LoopMeInterstitialGeneral interstitial) {
                if (mMainAdListener != null) {
                    mMainAdListener.onLoopMeInterstitialShow(LoopMeInterstitial.this);
                }
            }

            @Override
            public void onLoopMeInterstitialHide(LoopMeInterstitialGeneral interstitial) {
                if (mMainAdListener != null) {
                    mMainAdListener.onLoopMeInterstitialHide(LoopMeInterstitial.this);
                }
                reload();
            }

            @Override
            public void onLoopMeInterstitialClicked(LoopMeInterstitialGeneral interstitial) {
                if (mMainAdListener != null) {
                    mMainAdListener.onLoopMeInterstitialClicked(LoopMeInterstitial.this);
                }
            }

            @Override
            public void onLoopMeInterstitialLeaveApp(LoopMeInterstitialGeneral interstitial) {
                if (mMainAdListener != null) {
                    mMainAdListener.onLoopMeInterstitialLeaveApp(LoopMeInterstitial.this);
                }
            }

            @Override
            public void onLoopMeInterstitialExpired(LoopMeInterstitialGeneral interstitial) {
                if (mMainAdListener != null) {
                    mMainAdListener.onLoopMeInterstitialExpired(LoopMeInterstitial.this);
                }
            }

            @Override
            public void onLoopMeInterstitialVideoDidReachEnd(LoopMeInterstitialGeneral interstitial) {
                if (mMainAdListener != null) {
                    mMainAdListener.onLoopMeInterstitialVideoDidReachEnd(LoopMeInterstitial.this);
                }
            }
        };
    }

    private void destroySecond() {
        if (mSecondInterstitial != null) {
            mSecondInterstitial.removeListener();
            mSecondInterstitial.destroy();
            mSecondInterstitial = null;
        }
    }

    private void destroyFirst() {
        if (mFirstInterstitial != null) {
            mFirstInterstitial.removeListener();
            mFirstInterstitial.destroy();
            mFirstInterstitial = null;
        }
    }

    private void increaseFailCounter() {
        if (isAutoLoadingEnabled()) {
            if (mFailCounter > StaticParams.MAX_FAIL_COUNT) {
                sleep();
            } else {
                mFailCounter++;
                Logging.out(LOG_TAG, "Attempt #" + mFailCounter);
                reload();
            }
        }
    }

    private void sleep() {
        if (mSleepLoadTimer == null) {
            mIsLoadingPaused = true;
            mSleepLoadTimer = initSleepLoadTimer();
            float sleepTimeout = StaticParams.SLEEP_TIME / StaticParams.ONE_MINUTE_IN_MILLIS;
            Logging.out(LOG_TAG, "Sleep timeout: " + sleepTimeout + " minutes");
            mSleepLoadTimer.start();
        }
    }

    private CountDownTimer initSleepLoadTimer() {
        return new CountDownTimer(StaticParams.SLEEP_TIME, StaticParams.ONE_MINUTE_IN_MILLIS) {
            @Override
            public void onTick(long millisUntilFinished) {
                Logging.out(LOG_TAG, "Till next attempt: " + millisUntilFinished / StaticParams.ONE_MINUTE_IN_MILLIS + " min.");
            }

            @Override
            public void onFinish() {
                stopSleepLoadTimer();
                load();
            }
        };
    }

    protected void stopSleepLoadTimer() {
        if (mSleepLoadTimer != null) {
            Logging.out(LOG_TAG, "Stop sleep timer");
            mSleepLoadTimer.cancel();
            mSleepLoadTimer = null;
        }
        mFailCounter = 0;
        mIsLoadingPaused = false;
    }

    private void reload() {
        if (!ResponseParser.isApi19() && isAutoLoadingEnabled()) {
            if (!isReady(mFirstInterstitial)) {
                load(mFirstInterstitial);
            }
            if (!isReady(mSecondInterstitial)) {
                load(mSecondInterstitial);
            }
        }
    }

    private void setListener(LoopMeInterstitialGeneral.Listener listener, LoopMeInterstitialGeneral interstitial) {
        if (interstitial != null) {
            interstitial.setListener(listener);
        }
    }

    public Listener getListener() {
        return mMainAdListener;
    }

    public void removeListener() {
        mMainAdListener = null;
        removeListener(mFirstInterstitial);
        removeListener(mSecondInterstitial);
    }

    public void clearCache() {
        clearCache(mFirstInterstitial);
        clearCache(mSecondInterstitial);
    }

    private void removeListener(LoopMeInterstitialGeneral interstitial) {
        if (interstitial != null) {
            interstitial.removeListener();
        }
    }

    private void clearCache(LoopMeInterstitialGeneral interstitial) {
        if (interstitial != null) {
            interstitial.clearCache();
        }
    }

    private void load(BaseAd baseAd) {
        if (baseAd != null) {
            baseAd.load();
            onLoad();
        }
    }


    private void show(LoopMeInterstitialGeneral interstitial) {
        if (interstitial != null) {
            interstitial.show();
            onShow();
        }
    }

    private void dismiss(BaseAd baseAd) {
        if (baseAd != null) {
            baseAd.dismiss();
        }
    }

    private boolean isReady(BaseAd baseAd) {
        return baseAd != null && baseAd.isReady();
    }

    private boolean isShowing(BaseAd baseAd) {
        return baseAd != null && baseAd.isShowing();
    }

    private boolean isLoading(BaseAd baseAd) {
        return baseAd != null && baseAd.isLoading();
    }

    public void setKeywords(String keywords) {
        setKeywords(keywords, mFirstInterstitial);
        setKeywords(keywords, mSecondInterstitial);
    }

    public void setGender(String gender) {
        setGender(gender, mFirstInterstitial);
        setGender(gender, mSecondInterstitial);
    }

    public void setYearOfBirth(int year) {
        setYearOfBirth(year, mFirstInterstitial);
        setYearOfBirth(year, mSecondInterstitial);
    }

    private void setYearOfBirth(int year, BaseAd baseAd) {
        if (baseAd != null) {
            baseAd.setYearOfBirth(year);
        }
    }

    private void setKeywords(String keywords, BaseAd baseAd) {
        if (baseAd != null) {
            baseAd.setKeywords(keywords);
        }
    }

    private void setGender(String gender, BaseAd baseAd) {
        if (baseAd != null) {
            baseAd.setGender(gender);
        }
    }

    public interface Listener {

        void onLoopMeInterstitialLoadSuccess(LoopMeInterstitial interstitial);

        void onLoopMeInterstitialLoadFail(LoopMeInterstitial interstitial, LoopMeError error);

        void onLoopMeInterstitialShow(LoopMeInterstitial interstitial);

        void onLoopMeInterstitialHide(LoopMeInterstitial interstitial);

        void onLoopMeInterstitialClicked(LoopMeInterstitial interstitial);

        void onLoopMeInterstitialLeaveApp(LoopMeInterstitial interstitial);

        void onLoopMeInterstitialExpired(LoopMeInterstitial interstitial);

        void onLoopMeInterstitialVideoDidReachEnd(LoopMeInterstitial interstitial);
    }
}