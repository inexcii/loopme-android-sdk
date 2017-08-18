package com.loopme;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import com.loopme.common.Logging;
import com.loopme.common.LoopMeError;
import com.loopme.common.StaticParams;
import com.loopme.common.Utils;
import com.loopme.constants.AdFormat;
import com.loopme.constants.AdState;
import com.loopme.debugging.ErrorLog;
import com.loopme.debugging.LiveDebug;

/**
 * The `LoopMeInterstitial` class provides the facilities to display a full-screen ad
 * during natural transition points in your application.
 * <p>
 * It is recommended to implement `LoopMeInterstitial.OnMraidBridgeListener`
 * to stay informed about ad state changes,
 * such as when an ad has been loaded or has failed to load its content, when video ad has been watched completely,
 * when an ad has been presented or dismissed from the screen, and when an ad has expired or received a tap.
 */
public final class LoopMeInterstitial extends BaseAd {

    private static final String LOG_TAG = LoopMeInterstitial.class.getSimpleName();

    /**
     * AppKeys for test purposes
     */
    public static final String TEST_PORT_INTERSTITIAL = "test_interstitial_p";
    public static final String TEST_LAND_INTERSTITIAL = "test_interstitial_l";

    private Listener mAdListener;

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

    /**
     * Creates new `LoopMeInterstitial` object with the given appKey
     *
     * @param activity - application context
     * @param appKey  - your app key
     * @throws IllegalArgumentException if any of parameters is null
     */
    LoopMeInterstitial(Activity activity, String appKey) {
        super(activity, appKey);
        Logging.out(LOG_TAG, "Start creating interstitial with app key: " + appKey);

        mAdController = new AdController(this);

        Utils.init(activity);
        LiveDebug.init(activity);
    }

    /**
     * Getting already initialized ad object or create new one with specified appKey
     * Note: Returns null if Android version under 4.0
     *
     * @param appKey  - your app key
     * @param activity - Activity context
     * @return instance of LoopMeInterstitial
     */
    public static LoopMeInterstitial getInstance(String appKey, Activity activity) {
        if (Build.VERSION.SDK_INT >= 14) {
            return LoopMeAdHolder.getInterstitial(appKey, activity);
        } else {
            Logging.out(LOG_TAG, "Not supported Android version. Expected Android 4.0+");
            return null;
        }
    }

    @Override
    public void destroy() {
        broadcastDestroyIntent();

        super.destroy();
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
        if (mAdState == AdState.SHOWING) {
            Logging.out(LOG_TAG, "Dismiss ad");
            broadcastDestroyIntent();
            stopExpirationTimer();
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
            }
        } else {
            Logging.out(LOG_TAG, "Can't dismiss ad, it's not displaying");
        }
    }

    private void broadcastDestroyIntent() {
        Intent intent = new Intent();
        intent.setAction(StaticParams.DESTROY_INTENT);
        getContext().sendBroadcast(intent);
    }

    /**
     * Sets listener in order to receive notifications during the loading/displaying ad processes
     * @param listener - OnMraidBridgeListener
     */
    public void setListener(Listener listener) {
        mAdListener = listener;
    }

    public Listener getListener() {
        return mAdListener;
    }

    /**
     * Removes listener.
     */
    public void removeListener() {
        mAdListener = null;
    }

    /**
     * Shows insterstitial.
     * Interstitial should be loaded and ready to be shown.
     * <p>
     * As a result you'll receive onLoopMeInterstitialShow() callback
     */
    public void show() {
        Logging.out(LOG_TAG, "Interstitial will present fullscreen ad. App key: " + getAppKey());
        if (isReady()) {
            if (mAdState != AdState.SHOWING) {
                mAdState = AdState.SHOWING;
                stopExpirationTimer();
                if (!getAdParams().isMraid()) {
                    AdUtils.startAdActivity(this);
                } else {
                    AdUtils.startMraidActivity(this, getAdParams().isOwnCloseButton());
                }
            } else {
                Logging.out(LOG_TAG, "Interstitial is already presented on the screen");
            }
        } else {
            mShowWhenAdNotReadyCounter++;
            Logging.out(LOG_TAG, "Interstitial is not ready (" + mShowWhenAdNotReadyCounter +
                    " time(s))");
            ErrorLog.post("Interstitial is not ready");
        }
    }

    @Override
    public int getAdFormat() {
        return AdFormat.INTERSTITIAL;
    }

    public AdController getAdController() {
        return mAdController;
    }

    /**
     * Triggered when the interstitial has successfully loaded the ad content
     *
     * @param interstitial - interstitial object the sender of message
     */
    public void onLoopMeInterstitialLoadSuccess(LoopMeInterstitial interstitial) {
        long currentTime = System.currentTimeMillis();
        long loadingTime = currentTime - mAdLoadingTimer;

        Logging.out(LOG_TAG, "Ad successfully loaded (" + loadingTime + "ms)");
        mIsReady = true;
        mAdState = AdState.NONE;
        stopFetcherTimer();
        if (mAdListener != null) {
            mAdListener.onLoopMeInterstitialLoadSuccess(LoopMeInterstitial.this);
            Logging.logEvent(getAppKey(), "Ad loaded successfully.");
        } else {
            Logging.out(LOG_TAG, "Warning: empty listener");
        }
    }

    /**
     * Triggered when interstitial ad failed to load ad content
     *
     * @param interstitial - interstitial object - the sender of message
     * @param error        - error of unsuccesful ad loading attempt
     */
    void onLoopMeInterstitialLoadFail(LoopMeInterstitial interstitial, final LoopMeError error) {
        Logging.out(LOG_TAG, "Ad fails to load: " + error.getMessage());
        mIsReady = false;
        mAdState = AdState.NONE;
        stopFetcherTimer();
        if (mAdListener != null) {
            mAdListener.onLoopMeInterstitialLoadFail(LoopMeInterstitial.this, error);
            Logging.logEvent(getAppKey(), "Ad failed to load. " + error.getMessage());
        } else {
            Logging.out(LOG_TAG, "Warning: empty listener");
        }
    }

    /**
     * Triggered when the interstitial ad appears on the screen
     *
     * @param interstitial - interstitial object the sender of message
     */
    void onLoopMeInterstitialShow(LoopMeInterstitial interstitial) {
        if (mAdListener != null) {
            Logging.out(LOG_TAG, "Ad appeared on screen");
            mAdListener.onLoopMeInterstitialShow(this);
            Logging.logEvent("Ad appeared on the screen.");
        }
    }

    /**
     * Triggered when the interstitial ad disappears on the screen
     *
     * @param interstitial - interstitial object the sender of message
     */
    void onLoopMeInterstitialHide(LoopMeInterstitial interstitial) {
        Logging.out(LOG_TAG, "Ad disappeared from screen");
        mIsReady = false;
        mAdState = AdState.NONE;
        releaseViewController();
        if (mAdListener != null) {
            mAdListener.onLoopMeInterstitialHide(this);
            Logging.logEvent("Ad closed.");
        }
    }

    /**
     * Triggered when the user taps the interstitial ad and the interstitial is about to perform extra actions
     * Those actions may lead to displaying a modal browser or leaving your application.
     *
     * @param interstitial - interstitial object the sender of message
     */
    void onLoopMeInterstitialClicked(LoopMeInterstitial interstitial) {
        Logging.out(LOG_TAG, "Ad received tap event");
        if (mAdListener != null) {
            mAdListener.onLoopMeInterstitialClicked(this);
            Logging.logEvent("User interacts with Ad.");
        }
    }

    /**
     * Triggered when your application is about to go to the background, initiated by the SDK.
     * This may happen in various ways, f.e if user wants open the SDK's browser web page in native browser or clicks
     * on `mailto:` links...
     *
     * @param interstitial - interstitial object the sender of message
     */
    void onLoopMeInterstitialLeaveApp(LoopMeInterstitial interstitial) {
        Logging.out(LOG_TAG, "Leaving application");
        if (mAdListener != null) {
            mAdListener.onLoopMeInterstitialLeaveApp(this);
        }
    }

    /**
     * Triggered when the interstitial's loaded ad content is expired.
     * Expiration happens when loaded ad content wasn't displayed during some period of time, approximately one hour.
     * Once the interstitial is presented on the screen, the expiration is no longer tracked and interstitial won't
     * receive this message
     *
     * @param interstitial - interstitial object the sender of message
     */
    void onLoopMeInterstitialExpired(LoopMeInterstitial interstitial) {
        Logging.out(LOG_TAG, "Ads content expired");
        mExpirationTimer = null;
        mIsReady = false;
        mAdState = AdState.NONE;
        releaseViewController();
        if (mAdListener != null) {
            mAdListener.onLoopMeInterstitialExpired(this);
        }
    }

    /**
     * Triggered only when interstitial's video was played until the end.
     * It won't be sent if the video was skipped or the interstitial was dissmissed during the displaying process
     *
     * @param interstitial - interstitial object - the sender of message
     */
    void onLoopMeInterstitialVideoDidReachEnd(LoopMeInterstitial interstitial) {
        Logging.out(LOG_TAG, "Video reach end");
        if (mAdListener != null) {
            mAdListener.onLoopMeInterstitialVideoDidReachEnd(this);
        }
    }

    @Override
    void onAdExpired() {
        onLoopMeInterstitialExpired(this);
    }

    @Override
    void onAdLoadSuccess() {
        onLoopMeInterstitialLoadSuccess(this);
    }

    @Override
    void onAdLoadFail(final LoopMeError error) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onLoopMeInterstitialLoadFail(LoopMeInterstitial.this, error);
            }
        });
    }

    @Override
    void onAdLeaveApp() {
        onLoopMeInterstitialLeaveApp(this);
    }

    @Override
    void onAdClicked() {
        onLoopMeInterstitialClicked(this);
    }

    @Override
    void onAdVideoDidReachEnd() {
        onLoopMeInterstitialVideoDidReachEnd(this);
    }

    @Override
    int detectWidth() {
        return Utils.getScreenWidth();
    }

    @Override
    int detectHeight() {
        return Utils.getScreenHeight();
    }

    /**
     * Removes all video files from cache.
     */
    public void clearCache() {
        if (getContext() != null) {
            Utils.clearCache(getContext());
        }
    }
}