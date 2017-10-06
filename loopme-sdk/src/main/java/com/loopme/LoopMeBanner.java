package com.loopme;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.text.TextUtils;

import com.loopme.common.Logging;
import com.loopme.common.LoopMeError;
import com.loopme.common.MinimizedMode;
import com.loopme.common.ResponseParser;
import com.loopme.common.StaticParams;
import com.loopme.constants.AdFormat;

/**
 * The `LoopMeBanner` class provides facilities to display a custom size ads
 * during natural transition points in your application.
 * <p>
 * It is recommended to implement `LoopMeBanner.Listener` to stay informed about ad state changes,
 * such as when an ad has been loaded or has failed to load its content, when video ad has been watched completely,
 * when an ad has been presented or dismissed from the screen, and when an ad has expired or received a tap.
 */
public class LoopMeBanner extends Settings {
    public static final String TEST_MPU_BANNER = "test_mpu";
    private static final String FIRST_BANNER = "FIRST_BANNER";
    private static final String SECOND_BANNER = "SECOND_BANNER";
    private static final String LOG_TAG = LoopMeBanner.class.getSimpleName();
    private int mFailCounter;
    private String mAppKey;
    private Activity mActivity;
    private Listener mMainAdListener;
    private CountDownTimer mSleepLoadTimer;
    private LoopMeBannerGeneral mFirstBanner;
    private LoopMeBannerGeneral mSecondBanner;
    private volatile LoopMeBannerView mBannerView;
    private String mCurrentAd = FIRST_BANNER;
    private boolean mIsLoadingPaused;

    /**
     * Creates new `LoopMeBanner` object with the given appKey
     *
     * @param activity - application context
     * @param appKey   - your app key
     * @throws IllegalArgumentException if any of parameters is null
     */
    LoopMeBanner(Activity activity, String appKey) {
        this.mActivity = activity;
        this.mAppKey = appKey;
        mFirstBanner = LoopMeBannerGeneral.getInstance(appKey, activity);
        mSecondBanner = LoopMeBannerGeneral.getInstance(appKey, activity);
    }

    /**
     * Getting already initialized ad object or create new one with specified appKey
     * Note: Returns null if Android version under 4.0
     *
     * @param appKey   - your app key
     * @param activity - Activity context
     * @return instance of LoopMeBanner
     */
    public static LoopMeBanner getInstance(String appKey, Activity activity) {
        return new LoopMeBanner(activity, appKey);
    }

    /**
     * Links (@link LoopMeBannerView) view to banner.
     * If ad doesn't linked to @link LoopMeBannerView, it can't be display.
     *
     * @param viewGroup - @link LoopMeBannerView (container for ad) where ad will be displayed.
     */
    public void bindView(LoopMeBannerView viewGroup) {
        mBannerView = viewGroup;
    }

    private void bindView(LoopMeBannerView viewGroup, LoopMeBannerGeneral banner) {
        if (banner != null) {
            banner.bindView(viewGroup);
        }
    }

    public void setMinimizedMode(MinimizedMode mode) {
        setMinimizedMode(mode, mFirstBanner);
        setMinimizedMode(mode, mSecondBanner);
    }

    private void setMinimizedMode(MinimizedMode mode, LoopMeBannerGeneral banner) {
        if (banner != null) {
            banner.setMinimizedMode(mode);
        }
    }

    public LoopMeBannerView getBannerView() {
        return mBannerView;
    }

    /**
     * Checks whether any view already binded to ad or not.
     *
     * @return true - if binded,
     * false - otherwise.
     */
    public boolean isViewBinded() {
        return mBannerView != null;
    }

    /**
     * Pauses video ad
     * Needs to be triggered on appropriate Activity life-cycle method "onPause()".
     */
    public void pause() {
        pause(mFirstBanner);
        pause(mSecondBanner);
    }

    private void pause(LoopMeBannerGeneral banner) {
        if (banner != null) {
            banner.pause();
        }
    }

    public void show() {
        if (!isShowing()) {
            if (isReady(mFirstBanner)) {
                bindView(mBannerView, mFirstBanner);
                show(mFirstBanner);
                mCurrentAd = FIRST_BANNER;
            } else if (isReady(mSecondBanner)) {
                bindView(mBannerView, mSecondBanner);
                show(mSecondBanner);
                mCurrentAd = SECOND_BANNER;
            }
        } else {
            Logging.out(LOG_TAG, "Interstitial is already presented on the screen");
        }
    }

    public void showNativeVideo() {
        if (isReady(mFirstBanner)) {
            showNativeVideo(mFirstBanner);
        } else if (isReady(mSecondBanner)) {
            showNativeVideo(mSecondBanner);
        }
    }

    private void showNativeVideo(LoopMeBannerGeneral banner) {
        if (banner != null) {
            banner.showNativeVideo();
        }
    }

    public void resume() {
        resume(mFirstBanner);
        resume(mSecondBanner);
    }

    private void resume(LoopMeBannerGeneral banner) {
        if (banner != null && banner.isShowing()) {
            banner.resume();
        }
    }

    public void switchToMinimizedMode() {
        switchToMinimizedMode(mFirstBanner);
        switchToMinimizedMode(mSecondBanner);
    }

    public void switchToMinimizedMode(LoopMeBannerGeneral banner) {
        if (banner != null) {
            banner.switchToMinimizedMode();
        }
    }

    public void switchToNormalMode() {
        switchToNormalMode(mFirstBanner);
        switchToNormalMode(mSecondBanner);
    }

    private void switchToNormalMode(LoopMeBannerGeneral banner) {
        if (banner != null) {
            banner.switchToNormalMode();
        }
    }

    public AdController getAdController() {
        if (isReady(mFirstBanner)) {
            return mFirstBanner.getAdController();
        } else if (isReady(mSecondBanner)) {
            return mSecondBanner.getAdController();
        } else {
            return null;
        }
    }

    public void load(IntegrationType integrationType) {
        if (mFirstBanner != null && mSecondBanner != null) {
            mFirstBanner.setIntegrationType(integrationType);
            mSecondBanner.setIntegrationType(integrationType);
        }
        load();
    }

    public void load() {
        if (isLoadingPaused()) {
            onAutoLoadPaused();
            return;
        }
        stopSleepLoadTimer();
        load(mFirstBanner);
        if (!ResponseParser.isApi19() && isAutoLoadingEnabled()) {
            load(mSecondBanner);
        }
    }

    private void onAutoLoadPaused() {
        if (mMainAdListener != null) {
            mMainAdListener.onLoopMeBannerLoadFail(this, new LoopMeError("Paused by auto loading"));
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
        return isReady(mFirstBanner) || isReady(mSecondBanner);
    }

    /**
     * Indicates whether `LoopMeInterstitial`/`LoopMeBanner` currently presented on screen.
     * Ad status will be set to `AdState.SHOWING` after trigger `show` method
     *
     * @return true - if ad presented on screen
     * false - if ad absent on screen
     */
    public boolean isShowing() {
        return isShowing(mFirstBanner) || isShowing(mSecondBanner);
    }

    /**
     * Indicates whether `LoopMeInterstitial`/`LoopMeBanner` in "loading ad content" process.
     * Ad status will be set to `AdState.LOADING` after trigger `load` method
     *
     * @return true - if ad is loading now
     * false - if ad is not loading now
     */
    public boolean isLoading() {
        return isLoading(mFirstBanner) || isLoading(mSecondBanner);
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
     * Dismisses an banner ad
     * This method dismisses an banner ad and only if it is currently presented.
     * NOTE: should be called from UI thread
     * <p>
     * After it banner ad requires "loading process" to be ready for displaying
     * <p>
     * As a result you'll receive onLoopMeBannerHide() notification
     * <p>
     * NOTE: should be triggered from UI thread
     */
    public void dismiss() {
        dismiss(mFirstBanner);
        dismiss(mSecondBanner);
        loadCurrentBanner();
    }

    private void loadCurrentBanner() {
        if (TextUtils.equals(mCurrentAd, FIRST_BANNER)) {
            reload(mFirstBanner);
        } else {
            reload(mSecondBanner);
        }
    }

    private void reload(BaseAd baseAd) {
        if (!ResponseParser.isApi19() && isAutoLoadingEnabled()) {
            if (!isReady(baseAd)) {
                load(baseAd);
            }
        }
    }

    public int getAdFormat() {
        return AdFormat.BANNER;
    }

    /**
     * NOTE: should be in UI thread
     */
    public void destroy() {
        destroyFirst();
        destroySecond();
        stopSleepLoadTimer();
        destroyBannersView();
    }

    private void destroyBannersView() {
        if (mFirstBanner != null) {
            mFirstBanner.destroyBannerView();
        }
        if (mSecondBanner != null) {
            mSecondBanner.destroyBannerView();
        }
    }

    /**
     * Sets listener in order to receive notifications during the loading/displaying ad processes
     *
     * @param listener - LoopMeBanner.Listener
     */
    public void setListener(Listener listener) {
        mMainAdListener = listener;
        setListener(initInternalListener(), mFirstBanner);
        setListener(initInternalListener(), mSecondBanner);
    }

    private LoopMeBannerGeneral.Listener initInternalListener() {
        return new LoopMeBannerGeneral.Listener() {

            @Override
            public void onLoopMeBannerLoadSuccess(LoopMeBannerGeneral banner) {
                if (mMainAdListener != null) {
                    mMainAdListener.onLoopMeBannerLoadSuccess(LoopMeBanner.this);
                }
                mFailCounter = 0;
            }

            @Override
            public void onLoopMeBannerLoadFail(LoopMeBannerGeneral banner, LoopMeError error) {
                if (mMainAdListener != null) {
                    mMainAdListener.onLoopMeBannerLoadFail(LoopMeBanner.this, error);
                }
                increaseFailCounter();
            }

            @Override
            public void onLoopMeBannerShow(LoopMeBannerGeneral banner) {
                if (mMainAdListener != null) {
                    mMainAdListener.onLoopMeBannerShow(LoopMeBanner.this);
                }
            }

            @Override
            public void onLoopMeBannerHide(LoopMeBannerGeneral banner) {
                if (mMainAdListener != null) {
                    mMainAdListener.onLoopMeBannerHide(LoopMeBanner.this);
                }
            }

            @Override
            public void onLoopMeBannerClicked(LoopMeBannerGeneral banner) {
                if (mMainAdListener != null) {
                    mMainAdListener.onLoopMeBannerClicked(LoopMeBanner.this);
                }

            }

            @Override
            public void onLoopMeBannerLeaveApp(LoopMeBannerGeneral banner) {
                if (mMainAdListener != null) {
                    mMainAdListener.onLoopMeBannerLeaveApp(LoopMeBanner.this);
                }
            }

            @Override
            public void onLoopMeBannerVideoDidReachEnd(LoopMeBannerGeneral banner) {
                if (mMainAdListener != null) {
                    mMainAdListener.onLoopMeBannerVideoDidReachEnd(LoopMeBanner.this);
                }
            }

            @Override
            public void onLoopMeBannerExpired(LoopMeBannerGeneral banner) {
                if (mMainAdListener != null) {
                    mMainAdListener.onLoopMeBannerExpired(LoopMeBanner.this);
                }
            }
        };
    }

    private void destroySecond() {
        if (mSecondBanner != null) {
            mSecondBanner.removeListener();
            mSecondBanner.destroy();
            mSecondBanner = null;
        }
    }

    private void destroyFirst() {
        if (mFirstBanner != null) {
            mFirstBanner.removeListener();
            mFirstBanner.destroy();
            mFirstBanner = null;
        }
    }

    private void increaseFailCounter() {
        if (isAutoLoadingEnabled()) {
            if (mFailCounter > StaticParams.MAX_FAIL_COUNT) {
                sleep();
            } else {
                mFailCounter++;
                Logging.out(LOG_TAG, "Attempt #" + mFailCounter);
                reloadAll();
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

    private void reloadAll() {
        if (!ResponseParser.isApi19() && isAutoLoadingEnabled()) {
            if (!isReady(mFirstBanner)) {
                load(mFirstBanner);
            }
            if (!isReady(mSecondBanner)) {
                load(mSecondBanner);
            }
        }
    }

    private void setListener(LoopMeBannerGeneral.Listener listener, LoopMeBannerGeneral banner) {
        if (banner != null) {
            banner.setListener(listener);
        }
    }

    public Listener getListener() {
        return mMainAdListener;
    }

    public void removeListener() {
        mMainAdListener = null;
        removeListener(mFirstBanner);
        removeListener(mSecondBanner);
    }

    public void clearCache() {
        clearCache(mFirstBanner);
        clearCache(mSecondBanner);
    }

    private void removeListener(LoopMeBannerGeneral banner) {
        if (banner != null) {
            banner.removeListener();
        }
    }

    private void clearCache(LoopMeBannerGeneral banner) {
        if (banner != null) {
            banner.clearCache();
        }
    }

    private void load(BaseAd baseAd) {
        if (baseAd != null) {
            baseAd.load();
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

    private void show(LoopMeBannerGeneral banner) {
        if (banner != null) {
            banner.show();
        }
    }

    private void dismiss(BaseAd baseAd) {
        if (baseAd != null) {
            baseAd.dismiss();
        }
    }

    public void setKeywords(String keywords) {
        setKeywords(keywords, mFirstBanner);
        setKeywords(keywords, mSecondBanner);
    }

    public void setGender(String gender) {
        setGender(gender, mFirstBanner);
        setGender(gender, mSecondBanner);
    }

    public void setYearOfBirth(int year) {
        setYearOfBirth(year, mFirstBanner);
        setYearOfBirth(year, mSecondBanner);
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

        void onLoopMeBannerLoadSuccess(LoopMeBanner banner);

        void onLoopMeBannerLoadFail(LoopMeBanner banner, LoopMeError error);

        void onLoopMeBannerShow(LoopMeBanner banner);

        void onLoopMeBannerHide(LoopMeBanner banner);

        void onLoopMeBannerClicked(LoopMeBanner banner);

        void onLoopMeBannerLeaveApp(LoopMeBanner banner);

        void onLoopMeBannerVideoDidReachEnd(LoopMeBanner banner);

        void onLoopMeBannerExpired(LoopMeBanner banner);
    }
}