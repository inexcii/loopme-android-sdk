package com.loopme;

import android.app.Activity;
import android.view.View;
import android.view.ViewTreeObserver;

import com.loopme.common.Logging;
import com.loopme.common.LoopMeError;
import com.loopme.common.MinimizedMode;
import com.loopme.common.Utils;
import com.loopme.constants.AdFormat;
import com.loopme.constants.AdState;
import com.loopme.constants.DisplayMode;
import com.loopme.constants.VideoState;
import com.loopme.constants.WebviewState;
import com.loopme.debugging.ErrorLog;
import com.loopme.debugging.LiveDebug;

/**
 * The `LoopMeBanner` class provides facilities to display a custom size ads
 * during natural transition points in your application.
 * <p>
 * It is recommended to implement `LoopMeBanner.Listener` to stay informed about ad state changes,
 * such as when an ad has been loaded or has failed to load its content, when video ad has been watched completely,
 * when an ad has been presented or dismissed from the screen, and when an ad has expired or received a tap.
 */
public class LoopMeBannerGeneral extends BaseAd {

    private static final String LOG_TAG = LoopMeBannerGeneral.class.getSimpleName();

    /**
     * AppKey for test purposes
     */
    public static final String TEST_MPU_BANNER = "test_mpu";

    private Listener mAdListener;

    private volatile LoopMeBannerView mBannerView;

    private boolean mIsVideoFinished;

    public interface Listener {

        void onLoopMeBannerLoadSuccess(LoopMeBannerGeneral banner);

        void onLoopMeBannerLoadFail(LoopMeBannerGeneral banner, LoopMeError error);

        void onLoopMeBannerShow(LoopMeBannerGeneral banner);

        void onLoopMeBannerHide(LoopMeBannerGeneral banner);

        void onLoopMeBannerClicked(LoopMeBannerGeneral banner);

        void onLoopMeBannerLeaveApp(LoopMeBannerGeneral banner);

        void onLoopMeBannerVideoDidReachEnd(LoopMeBannerGeneral banner);

        void onLoopMeBannerExpired(LoopMeBannerGeneral banner);
    }

    /**
     * Creates new `LoopMeBanner` object with the given appKey
     *
     * @param activity - application context
     * @param appKey   - your app key
     * @throws IllegalArgumentException if any of parameters is null
     */
    LoopMeBannerGeneral(Activity activity, String appKey) {
        super(activity, appKey);

        Utils.init(activity);
        LiveDebug.init(activity);
        Logging.out(LOG_TAG, "Start creating banner with app key: " + appKey);
    }

    /**
     * Getting already initialized ad object or create new one with specified appKey
     * Note: Returns null if Android version under 4.0
     *
     * @param appKey   - your app key
     * @param activity - Activity context
     * @return instance of LoopMeBanner
     */
    public static LoopMeBannerGeneral getInstance(String appKey, Activity activity) {
        return LoopMeAdHolder.createBanner(appKey, activity);
    }

    private void ensureAdIsVisible() {
        if (mAdController != null) {
            mAdController.ensureAdIsVisible(mBannerView);
        }
    }

    /**
     * NOTE: should be in UI thread
     */
    @Override
    public void destroy() {
        mAdListener = null;
        if (mAdController != null) {
            mAdController.destroyMinimizedView();
            if (mAdController.getViewController() != null) {
                mAdController.getViewController().onPause();
                mAdController.getViewController().onDestroy();
            }
        }

        super.destroy();
    }

    protected void destroyBannerView() {
        if (mBannerView != null) {
            mBannerView.setVisibility(View.GONE);
            mBannerView.removeAllViews();
            mBannerView = null;
        }
    }

    /**
     * Links (@link LoopMeBannerView) view to banner.
     * If ad doesn't linked to @link LoopMeBannerView, it can't be display.
     *
     * @param viewGroup - @link LoopMeBannerView (container for ad) where ad will be displayed.
     */
    public void bindView(LoopMeBannerView viewGroup) {
        if (viewGroup != null) {
            String visibility = Utils.getViewVisibility(viewGroup);
            Logging.out(LOG_TAG, "Bind view (" + visibility + ")");
            mBannerView = viewGroup;
        }
    }

    public void setMinimizedMode(MinimizedMode mode) {
        if (mAdController != null && mode != null) {
            Logging.out(LOG_TAG, "Set minimized mode");
            mAdController.setMinimizedMode(mode);
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
        if (mAdController != null) {
            if (mAdController.getViewController() != null) {
                mAdController.getViewController().onPause();
            }
            if (mAdController.getCurrentDisplayMode() == DisplayMode.FULLSCREEN) {
                return;
            }

            if (mAdController.getCurrentVideoState() == VideoState.PLAYING) {
                Logging.out(LOG_TAG, "pause Ad");
                mAdController.setWebViewState(WebviewState.HIDDEN);
            }
        }
    }

    /**
     * Sets listener in order to receive notifications during the loading/displaying ad processes
     *
     * @param listener - LoopMeInterstitial.Listener
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

    void showNativeVideo() {
        if (mAdState == AdState.SHOWING) {
            return;
        }
        if (isReady() && mBannerView != null) {
            Logging.out(LOG_TAG, "Banner did start showing ad (native)");
            mAdState = AdState.SHOWING;
            stopExpirationTimer();

            mAdController.buildVideoAdView(mBannerView);
            if (getAdParams().isVideo360()) {
                IViewController v360 = mAdController.getViewController();
                v360.initVRLibrary(getContext());
                v360.onResume();
            }

            if (mBannerView.getVisibility() != View.VISIBLE) {
                mBannerView.setVisibility(View.VISIBLE);
            }
            onLoopMeBannerShow();
        } else {
            ErrorLog.post("Banner is not ready");
        }
    }

    public void show() {
        Logging.out(LOG_TAG, "Banner did start showing ad");
        if (mAdState == AdState.SHOWING) {
            Logging.out(LOG_TAG, "Banner already displays on screen");
            return;
        }

        if (isReady() && mBannerView != null) {
            mAdState = AdState.SHOWING;
            stopExpirationTimer();

            if (getAdParams().isMraid()) {
                mAdController.buildMraidContainer(mBannerView);
                mAdController.getMraidView().setIsViewable(true);
                mAdController.getMraidView().notifyStateChange();
            } else {
                if (isVideoPresented()) {
                    mAdController.buildVideoAdView(mBannerView);
                } else {
                    mAdController.buildStaticAdView(mBannerView);
                    mAdController.getAdView().setVideoState(VideoState.PLAYING);
                }
                if (getAdParams().isVideo360()) {
                    IViewController v360 = mAdController.getViewController();
                    v360.initVRLibrary(getContext());
                    v360.onResume();
                }
            }

            if (mBannerView.getVisibility() != View.VISIBLE) {
                mBannerView.setVisibility(View.VISIBLE);
            }

            final ViewTreeObserver observer = mBannerView.getViewTreeObserver();

            ViewTreeObserver.OnGlobalLayoutListener layoutListener =
                    new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            Logging.out(LOG_TAG, "onGlobalLayout");
                            if (mAdController != null &&
                                    mAdController.getCurrentDisplayMode() != DisplayMode.FULLSCREEN) {
                                ensureAdIsVisible();
                            }
                            if (observer.isAlive()) {
                                observer.removeOnGlobalLayoutListener(this);
                            }
                        }
                    };
            observer.addOnGlobalLayoutListener(layoutListener);

            onLoopMeBannerShow();
        } else {
            ErrorLog.post("Banner is not ready");
        }
    }

    private boolean isVideoPresented() {
        return mAdController.isVideoPresented();
    }

    public void resume() {
        Logging.out(LOG_TAG, "resume");
        if (mAdController != null && isReady()) {
            ensureAdIsVisible();
            if (mAdController.getViewController() != null) {
                mAdController.getViewController().onResume();
            }
        }
    }

    public AdController getAdController() {
        return mAdController;
    }

    void switchToMinimizedMode() {
        if (mAdState == AdState.SHOWING && mAdController != null && !mIsVideoFinished) {
            if (mAdController.isInMinimizedMode()) {
                mAdController.setWebViewState(WebviewState.VISIBLE);
                return;
            }
            if (mAdController.isMinimizedModeEnable()) {
                mAdController.switchToMinimizedMode();
            } else {
                pause();
            }
        }
    }

    void playbackFinishedWithError() {
        mIsVideoFinished = true;
    }

    void switchToNormalMode() {
        if ((mAdState == AdState.SHOWING || mAdState == AdState.NONE) && isMinimizedMode()) {
            mAdController.switchToNormalMode();
        }
    }

    private boolean isMinimizedMode() {
        return mAdController != null && mAdController.isInMinimizedMode();
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
        Logging.out(LOG_TAG, "Banner will be dismissed");
        if (mAdState == AdState.SHOWING || mAdState == AdState.NONE) {
            dismissBannerView();
            if (mAdController != null) {
                mAdController.destroyMinimizedView();
                mAdController.setWebViewStateDependsOnAdType(WebviewState.CLOSED);
                if (mAdController.getViewController() != null) {
                    mAdController.getViewController().onPause();
                }
            }
            onLoopMeBannerHide();
        } else {
            Logging.out(LOG_TAG, "Can't dismiss ad, it's not displaying");
        }
    }

    protected void dismissBannerView() {
        if (mBannerView != null) {
            mBannerView.setVisibility(View.GONE);
            mBannerView.removeAllViews();
        }
    }

    @Override
    public int getAdFormat() {
        return AdFormat.BANNER;
    }

    /**
     * Triggered when banner ad failed to load ad content
     *
     * @param error - error of unsuccesful ad loading attempt
     */
    void onLoopMeBannerLoadFail(final LoopMeError error) {
        Logging.out(LOG_TAG, "Ad fails to load: " + error.getMessage());
        mAdState = AdState.NONE;
        mIsReady = false;
        stopFetcherTimer();
        if (mAdController != null) {
            mAdController.resetFullScreenCommandCounter();
        }
        if (mAdListener != null) {
            mAdListener.onLoopMeBannerLoadFail(this, error);
        } else {
            Logging.out(LOG_TAG, "Warning: empty listener");
        }
    }

    /**
     * Triggered when the banner has successfully loaded the ad content
     */
    void onLoopMeBannerSuccessLoad() {
        long currentTime = System.currentTimeMillis();
        long loadingTime = currentTime - mAdLoadingTimer;

        Logging.out(LOG_TAG, "Ad successfully loaded (" + loadingTime + "ms)");
        mIsReady = true;
        mAdState = AdState.NONE;
        stopFetcherTimer();
        if (mAdListener != null) {
            mAdListener.onLoopMeBannerLoadSuccess(this);
        } else {
            Logging.out(LOG_TAG, "Warning: empty listener");
        }
    }

    /**
     * Triggered when the banner ad appears on the screen
     */
    void onLoopMeBannerShow() {
        Logging.out(LOG_TAG, "Ad appeared on screen");
        mIsVideoFinished = false;
        if (mAdListener != null) {
            mAdListener.onLoopMeBannerShow(this);
        }
    }

    /**
     * Triggered when the banner ad disappears on the screen
     */
    void onLoopMeBannerHide() {
        Logging.out(LOG_TAG, "Ad disappeared from screen");
        mIsReady = false;
        mAdState = AdState.NONE;
        if (mAdController != null) {
            mAdController.resetFullScreenCommandCounter();
        }
        releaseViewController();
        if (mAdListener != null) {
            mAdListener.onLoopMeBannerHide(this);
        }
    }

    /**
     * Triggered when the user taps the banner ad and the banner is about to perform extra actions
     * Those actions may lead to displaying a modal browser or leaving your application.
     */
    void onLoopMeBannerClicked() {
        Logging.out(LOG_TAG, "Ad received click event");
        if (mAdListener != null) {
            mAdListener.onLoopMeBannerClicked(this);
        }
    }

    /**
     * Triggered when your application is about to go to the background, initiated by the SDK.
     * This may happen in various ways, f.e if user wants open the SDK's browser web page in native browser or clicks
     * on `mailto:` links...
     */
    void onLoopMeBannerLeaveApp() {
        Logging.out(LOG_TAG, "Leaving application");
        if (mAdListener != null) {
            mAdListener.onLoopMeBannerLeaveApp(this);
        }
    }

    /**
     * Triggered only when banner's video was played until the end.
     * It won't be sent if the video was skipped or the banner was dissmissed during the displaying process
     */
    void onLoopMeBannerVideoDidReachEnd() {
        Logging.out(LOG_TAG, "Video did reach end");
        mIsVideoFinished = true;
        mIsReady = false;
        mAdState = AdState.NONE;

        switchToNormalMode();
        if (mAdListener != null) {
            mAdListener.onLoopMeBannerVideoDidReachEnd(this);
        }
    }

    /**
     * Triggered when the banner's loaded ad content is expired.
     * Expiration happens when loaded ad content wasn't displayed during some period of time, approximately one hour.
     * Once the banner is presented on the screen, the expiration is no longer tracked and banner won't
     * receive this message
     */
    void onLoopMeBannerExpired() {
        Logging.out(LOG_TAG, "Ad content is expired");
        mExpirationTimer = null;
        mIsReady = false;
        mAdState = AdState.NONE;
        releaseViewController();
        if (mAdListener != null) {
            mAdListener.onLoopMeBannerExpired(this);
        }
    }

    @Override
    void onAdExpired() {
        onLoopMeBannerExpired();
    }

    @Override
    void onAdLoadSuccess() {
        onLoopMeBannerSuccessLoad();
    }

    @Override
    void onAdLoadFail(final LoopMeError error) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onLoopMeBannerLoadFail(error);
            }
        });
    }

    @Override
    void onAdLeaveApp() {
        onLoopMeBannerLeaveApp();
    }

    @Override
    void onAdClicked() {
        onLoopMeBannerClicked();
    }

    @Override
    void onAdVideoDidReachEnd() {
        onLoopMeBannerVideoDidReachEnd();
    }

    @Override
    int detectWidth() {
        if (mBannerView == null) {
            return 0;
        }
        android.view.ViewGroup.LayoutParams params = mBannerView.getLayoutParams();
        return params.width;
    }

    @Override
    int detectHeight() {
        if (mBannerView == null) {
            return 0;
        }
        android.view.ViewGroup.LayoutParams params = mBannerView.getLayoutParams();
        return params.height;
    }

    public boolean isFullScreenMode() {
        return mAdController != null && mAdController.isFullScreenMode();
    }
}
