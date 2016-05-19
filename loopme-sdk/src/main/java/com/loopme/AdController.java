package com.loopme;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.loopme.adview.AdView;
import com.loopme.adview.Bridge;
import com.loopme.common.MinimizedMode;
import com.loopme.common.VideoLoader;
import com.loopme.constants.AdFormat;
import com.loopme.constants.DisplayMode;
import com.loopme.common.Logging;
import com.loopme.common.LoopMeError;
import com.loopme.common.StaticParams;
import com.loopme.common.SwipeListener;
import com.loopme.common.Utils;
import com.loopme.constants.StretchOption;
import com.loopme.constants.VideoState;
import com.loopme.constants.WebviewState;

public class AdController {

    private static final String LOG_TAG = AdController.class.getSimpleName();

    private static final String EXTRA_URL = "url";

    private AdView mAdView;
    private volatile Bridge.Listener mBridgeListener;

    private boolean mIsVideoPresented;

    private BaseAd mAd;

    private int mDisplayMode = DisplayMode.NORMAL;
    private int mPrevDisplayMode = DisplayMode.NORMAL;

    private MinimizedMode mMinimizedMode;
    private LoopMeBannerView mMinimizedView;

    private boolean mHorizontalScrollOrientation;

    //we should ignore first command
    private boolean mIsFirstFullScreenCommand = true;

    private boolean mIsBackFromExpand;

    private VideoLoader mVideoLoader;

    private boolean mPostponePlay;
    private int mPostponePlayPosition;

    private String mFileRest;

    private VideoController mVideoController;
    private IViewController mViewController;

    private View.OnTouchListener mOnTouchListener;

    public AdController(BaseAd ad) {
        mAd = ad;
        mAdView = new AdView(mAd.getContext());

        mBridgeListener = initBridgeListener();
        mAdView.addBridgeListener(mBridgeListener);

        mOnTouchListener = initOnTouchListener();
        mAdView.setOnTouchListener(mOnTouchListener);
    }

    private View.OnTouchListener initOnTouchListener() {

            return new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    mViewController.handleTouchEvent(event);
                    return false;
                }
            };
    }

    public void initVideoController() {
        VideoController.Callback callback = initVideoControllerCallback();
        mVideoController = new VideoController(mAdView, callback, mAd.getAppKey(), mAd.getAdFormat());
    }

    public void pauseVideo() {
        if (mVideoController != null) {
            mVideoController.pauseVideo();
        }
    }

    public void initViewController() {
        if (!mAd.getAdParams().isVideo360()) {
            ViewController.Callback viewCallback = initViewControllerCallback();
            mViewController = new ViewController(viewCallback);
        } else {
            View360Controller.Callback callback = initView360ControllerCallback();
            mViewController = new View360Controller(callback);
        }
    }

    IViewController getViewController() {
        return mViewController;
    }

    public void initControllers() {
        initVideoController();
        initViewController();
    }

    private View360Controller.Callback initView360ControllerCallback() {
        return new View360Controller.Callback() {
            @Override
            public void onSurfaceReady(Surface surface) {
                Logging.out(LOG_TAG, "onSurfaceReady ####");
                mVideoController.setSurface(surface);
            }

            @Override
            public void onEvent(String event) {
                if (mAdView != null) {
                    mAdView.send360Event(event);
                }
            }
        };
    }

    private ViewController.Callback initViewControllerCallback() {
        return new ViewController.Callback() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface) {
                surfaceTextureAvailable(surface);
            }

            @Override
            public void onSurfaceTextureDestroyed() {
                surfaceTextureDestroyed();
            }
        };
    }

    private VideoController.Callback initVideoControllerCallback() {
        return new VideoController.Callback() {
            @Override
            public void onVideoReachEnd() {
                mAd.onAdVideoDidReachEnd();
            }

            @Override
            public void onFail(LoopMeError error) {
                mAd.onAdLoadFail(error);
            }

            @Override
            public void onVideoSizeChanged(int width, int height) {
                mViewController.setVideoSize(width, height);
            }

            @Override
            public void postponePlay(int position) {
                mPostponePlay = true;
                mPostponePlayPosition = position;
            }

            @Override
            public void playbackFinishedWithError() {
                if (mAd.getAdFormat() == AdFormat.BANNER) {
                    ((LoopMeBanner) mAd).playbackFinishedWithError();
                }
            }
        };
    }

    void resetFullScreenCommandCounter() {
        mIsFirstFullScreenCommand = true;
    }

    void destroy() {
        mBridgeListener = null;
        if (mVideoController != null) {
            mVideoController.destroy();
        }
        if (mVideoLoader != null) {
            mVideoLoader.stop();
        }

        if (mAdView != null) {
            mAdView.stopLoading();
            mAdView.clearCache(true);
            mAdView = null;
        }
        mMinimizedMode = null;
    }

    public void setWebViewState(int state) {
        if (mAdView != null) {
            mAdView.setWebViewState(state);
        }
    }

    void onAdShake() {
        if (mAdView != null) {
            mAdView.shake();
        }
    }

    int getCurrentVideoState() {
        if (mAdView != null) {
            return mAdView.getCurrentVideoState();
        }
        return -1;
    }

    public int getCurrentDisplayMode() {
        return mDisplayMode;
    }

    void buildStaticAdView(ViewGroup bannerView) {
        if (bannerView == null || mAdView == null) {
            return;
        }
        mAdView.setBackgroundColor(Color.BLACK);
        bannerView.addView(mAdView);
    }

    void ensureAdIsVisible(View view) {
        if (mAdView == null || view == null) {
            return;
        }

        Rect rect = new Rect();
        boolean b = view.getGlobalVisibleRect(rect);

        int halfOfView = mHorizontalScrollOrientation ? view.getWidth() / 2 : view.getHeight() / 2;
        int rectHeight = mHorizontalScrollOrientation ? rect.width() : rect.height();

        if (b) {
            if (rectHeight < halfOfView) {
                setWebViewState(WebviewState.HIDDEN);
                mIsBackFromExpand = false;

            } else if (rectHeight >= halfOfView) {
                setWebViewState(WebviewState.VISIBLE);

            }
        } else {
            setWebViewState(WebviewState.HIDDEN);
        }
    }

    void buildVideoAdView(ViewGroup bannerView) {
        if (mViewController != null) {
            mViewController.buildVideoAdView(mAd.getContext(), bannerView, mAdView);
        }
    }

    void rebuildView(ViewGroup bannerView) {
        if (mViewController != null) {
            mViewController.rebuildView(bannerView, mAdView);
        }
    }

    private void storePreviousMode(int displayMode) {
        if (displayMode == DisplayMode.FULLSCREEN) {
            mPrevDisplayMode = DisplayMode.FULLSCREEN;

        } else if (displayMode == DisplayMode.MINIMIZED) {
            mPrevDisplayMode = DisplayMode.MINIMIZED;

        } else {
            mPrevDisplayMode = DisplayMode.NORMAL;
        }
    }

    boolean isBackFromExpand() {
        return mIsBackFromExpand;
    }

    void switchToMinimizedMode() {
        if (mDisplayMode == DisplayMode.MINIMIZED) {
            if (getCurrentVideoState() == VideoState.PAUSED) {
                setWebViewState(WebviewState.VISIBLE);
            }
            return;
        }
        Logging.out(LOG_TAG, "switch to minimized mode");
        storePreviousMode(mDisplayMode);
        mDisplayMode = DisplayMode.MINIMIZED;

        int width = mMinimizedMode.getWidth();
        int height = mMinimizedMode.getHeight();
        mMinimizedView = new LoopMeBannerView(mAdView.getContext(), width, height);

        mViewController.rebuildView(mMinimizedView, mAdView);
        addBordersToView(mMinimizedView);
        mViewController.onResume();

        if (mAdView.getCurrentWebViewState() == WebviewState.HIDDEN) {
            mMinimizedView.setAlpha(0);
        }

        mMinimizedMode.getRootView().addView(mMinimizedView);
        configMinimizedViewLayoutParams(mMinimizedView);

        setWebViewState(WebviewState.VISIBLE);

        mAdView.setOnTouchListener(new SwipeListener(width,
                new SwipeListener.Listener() {
                    @Override
                    public void onSwipe(boolean toRight) {
                        mAdView.setWebViewState(WebviewState.HIDDEN);

                        Animation anim = AnimationUtils.makeOutAnimation(mAd.getContext(),
                                toRight);
                        anim.setDuration(200);
                        mMinimizedView.startAnimation(anim);

                        switchToNormalMode();
                        mMinimizedMode = null;
                    }
            }));
    }

    private void configMinimizedViewLayoutParams(LoopMeBannerView bannerView) {
        LayoutParams lp = (LayoutParams) bannerView.getLayoutParams();
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lp.bottomMargin = mMinimizedMode.getMarginBottom();
        lp.rightMargin = mMinimizedMode.getMarginRight();
        bannerView.setLayoutParams(lp);
    }

    @SuppressLint("NewApi")
    private void addBordersToView(LoopMeBannerView bannerView) {
        ShapeDrawable drawable = new ShapeDrawable(new RectShape());
        drawable.getPaint().setColor(Color.BLACK);
        drawable.getPaint().setStyle(Style.FILL_AND_STROKE);
        drawable.getPaint().setAntiAlias(true);

        bannerView.setPadding(2, 2, 2, 2);
        if (Build.VERSION.SDK_INT < 16) {
            bannerView.setBackgroundDrawable(drawable);
        } else {
            bannerView.setBackground(drawable);
        }
    }

    void switchToNormalMode() {
        if (mDisplayMode == DisplayMode.NORMAL) {
            return;
        }

        Logging.out(LOG_TAG, "switch to normal mode");
        if (mDisplayMode == DisplayMode.FULLSCREEN) {
            mIsBackFromExpand = true;
        }
        storePreviousMode(mDisplayMode);
        mDisplayMode = DisplayMode.NORMAL;

        LoopMeBannerView initialView = ((LoopMeBanner) mAd).getBannerView();
        initialView.setVisibility(View.VISIBLE);

        mViewController.rebuildView(initialView, mAdView);

        if (mMinimizedView != null && mMinimizedView.getParent() != null) {
            ((ViewGroup) mMinimizedView.getParent()).removeView(mMinimizedView);
            mMinimizedView.removeAllViews();
        }
    }

    public void setMinimizedMode(MinimizedMode mode) {
        mMinimizedMode = mode;
    }

    boolean isMinimizedModeEnable() {
        return mMinimizedMode != null && mMinimizedMode.getRootView() != null;
    }

    void destroyMinimizedView() {
        if (mMinimizedView != null) {
            if (mMinimizedView.getParent() != null) {
                ((ViewGroup) mMinimizedView.getParent()).removeView(mMinimizedView);
            }
            mMinimizedView.removeAllViews();
            mMinimizedView = null;
        }
    }

    void preloadHtml(String html) {
        if (mAdView != null) {
            Logging.out(LOG_TAG, "loadDataWithBaseURL");
            mAdView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        } else {
            mAd.onAdLoadFail(new LoopMeError("Html loading error"));
        }
    }

    public boolean isVideoPresented() {
        return mIsVideoPresented;
    }

    private Bridge.Listener initBridgeListener() {
        return new Bridge.Listener() {

            @Override
            public void onJsVideoPlay(int time) {
                handleVideoPlay(time);
            }

            @Override
            public void onJsVideoPause(final int time) {
                handleVideoPause(time);
            }

            @Override
            public void onJsVideoMute(boolean mute) {
                handleVideoMute(mute);
            }

            @Override
            public void onJsVideoLoad(final String videoUrl) {
                handleVideoLoad(videoUrl);
            }

            @Override
            public void onJsLoadSuccess() {
                handleLoadSuccess();
            }

            @Override
            public void onJsClose() {
                handleClose();
            }

            @Override
            public void onJsLoadFail(String mess) {
                handleLoadFail(mess);
            }

            @Override
            public void onJsFullscreenMode(boolean b) {
                handleFullscreenMode(b);
            }

            @Override
            public void onNonLoopMe(String url) {
                handleNonLoopMe(url);
            }

            @Override
            public void onJsVideoStretch(boolean b) {
                handleVideoStretch(b);
            }
        };
    }

    private void handleLoadSuccess() {
        mAd.startExpirationTimer();
        mAd.onAdLoadSuccess();
    }

    private void handleLoadFail(String mess) {
        if (mAd != null) {
            mAd.onAdLoadFail(new LoopMeError("Failed to process ad"));
        }
    }

    private void handleVideoLoad(String videoUrl) {
        Logging.out(LOG_TAG, "JS command: load video " + videoUrl);
        mIsVideoPresented = true;
        boolean preload = mAd.getAdParams().getPartPreload();
        mVideoController.contain360(mAd.getAdParams().isVideo360());
        loadVideoFile(videoUrl, mAd.getContext(), preload);
    }

    private void loadVideoFile(final String videoUrl, Context context, final boolean preload) {

        mVideoLoader = new VideoLoader(videoUrl, preload, context, new VideoLoader.Callback() {

            @Override
            public void onError(LoopMeError error) {
                sendLoadFail(error);
            }

            @Override
            public void onPreviewLoaded(String filePath) {
                Logging.out(LOG_TAG, "onPreviewLoaded");
                mVideoController.initPlayerFromFile(filePath);
            }

            @Override
            public void onFullVideoLoaded(String filePath) {
                Logging.out(LOG_TAG, "onFullVideoLoaded: " + filePath);
                if (preload) {
                    if (mAd.isShowing()) {
                        mFileRest = filePath;
                        mVideoController.setFileRest(mFileRest);
                        mVideoController.waitForVideo();
                    } else {
                        mVideoController.releasePlayer();
                        mVideoController.initPlayerFromFile(filePath);
                    }
                } else {
                    mVideoController.initPlayerFromFile(filePath);
                }
            }
        });
        mVideoLoader.start();
    }

    private void sendLoadFail(LoopMeError error) {
        if (mAd != null) {
            mAd.onAdLoadFail(error);
        }
    }

    private void handleVideoMute(boolean mute) {
        if (mAdView != null) {
            mAdView.setVideoMute(mute);
        }
        if (mVideoController != null) {
            mVideoController.muteVideo(mute);
        }
    }

    private void handleVideoPlay(final int time) {
        mVideoController.playVideo(time, mAd.getAdParams().isVideo360());

        if (mDisplayMode == DisplayMode.MINIMIZED) {
            Utils.animateAppear(mMinimizedView);
        }
    }

    private void handleVideoPause(int time) {
        mVideoController.pauseVideo();
    }

    private void handleClose() {
        mAd.dismiss();
    }

    private void handleVideoStretch(boolean b) {
        Logging.out(LOG_TAG, "JS command: stretch video ");
        StretchOption stretch = b ? StretchOption.STRECH : StretchOption.NO_STRETCH;
        mViewController.setStretchParam(stretch);
    }

    private void handleFullscreenMode(boolean b) {
        if (mIsFirstFullScreenCommand) {
            mIsFirstFullScreenCommand = false;
            mAdView.setFullscreenMode(false);
            return;
        }
        if (b) {
            switchToFullScreenMode();
        } else {
            broadcastDestroyIntent();
        }
        mAdView.setFullscreenMode(b);
    }

    private void broadcastDestroyIntent() {
        Intent intent = new Intent();
        intent.setAction(StaticParams.DESTROY_INTENT);
        mAd.getContext().sendBroadcast(intent);
    }

    private void switchToFullScreenMode() {
        if (mDisplayMode != DisplayMode.FULLSCREEN) {
            Logging.out(LOG_TAG, "switch to fullscreen mode");
            storePreviousMode(mDisplayMode);
            mDisplayMode = DisplayMode.FULLSCREEN;

            if (mPrevDisplayMode == DisplayMode.MINIMIZED) {
                if (mMinimizedView != null && mMinimizedView.getParent() != null) {
                    ((ViewGroup) mMinimizedView.getParent()).removeView(mMinimizedView);
                }
            }
            AdUtils.startAdActivity(mAd);
        }
    }

    void switchToPreviousMode() {
        if (mPrevDisplayMode == DisplayMode.MINIMIZED) {
            switchToMinimizedMode();
        } else if (mPrevDisplayMode == DisplayMode.NORMAL) {
            switchToNormalMode();
        }
    }

    private void handleNonLoopMe(String url) {
        Logging.out(LOG_TAG, "Non Js command");
        Context context = mAd.getContext();
        if (Utils.isOnline(context)) {
            Intent intent = new Intent(context, AdBrowserActivity.class);
            intent.putExtra(EXTRA_URL, url);
            intent.putExtra(StaticParams.APPKEY_TAG, mAd.getAppKey());
            intent.putExtra(StaticParams.FORMAT_TAG, mAd.getAdFormat());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            mAd.onAdClicked();
            setWebViewState(WebviewState.HIDDEN);
            broadcastAdClickedIntent();

            context.startActivity(intent);
        } else {
            Logging.out(LOG_TAG, "No internet connection");
        }
    }

    private void broadcastAdClickedIntent() {
        Intent intent = new Intent();
        intent.setAction(StaticParams.CLICK_INTENT);
        mAd.getContext().sendBroadcast(intent);
    }

    private void surfaceTextureAvailable(SurfaceTexture surface) {
        Logging.out(LOG_TAG, "onSurfaceTextureAvailable");

        mVideoController.setSurfaceTextureAvailable(true);
        if (mPostponePlay) {
            mVideoController.playVideo(mPostponePlayPosition, mAd.getAdParams().isVideo360());
            mPostponePlay = false;
        }

        int viewWidth = 0;
        int viewHeight = 0;

        switch (mDisplayMode) {
            case DisplayMode.MINIMIZED:
                if (mMinimizedMode != null) {
                    viewWidth = mMinimizedMode.getWidth();
                    viewHeight = mMinimizedMode.getHeight();
                } else {
                    Logging.out(LOG_TAG, "WARNING: MinimizedMode is null");
                }
                break;

            case DisplayMode.NORMAL:
                viewWidth = mAd.detectWidth();
                viewHeight = mAd.detectHeight();
                break;

            case DisplayMode.FULLSCREEN:
                viewWidth = Utils.getScreenWidth();
                viewHeight = Utils.getScreenHeight();
                break;

            default:
                Logging.out(LOG_TAG, "Unknown display mode");
                break;
        }

        Surface s = new Surface(surface);
        mVideoController.setSurface(s);

        mViewController.setViewSize(viewWidth, viewHeight);
    }

    private boolean surfaceTextureDestroyed() {
        Logging.out(LOG_TAG, "onSurfaceTextureDestroyed");
        mVideoController.setSurfaceTextureAvailable(false);
        mVideoController.setSurface(null);
        return true;
    }
}