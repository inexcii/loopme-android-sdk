package com.loopme;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.media.MediaPlayer;
import android.os.Build;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.loopme.adview.AdView;
import com.loopme.adview.BaseWebView;
import com.loopme.adview.Bridge;
import com.loopme.common.Logging;
import com.loopme.common.LoopMeError;
import com.loopme.common.MinimizedMode;
import com.loopme.common.StaticParams;
import com.loopme.common.SwipeListener;
import com.loopme.common.Utils;
import com.loopme.common.VideoLoader;
import com.loopme.constants.AdFormat;
import com.loopme.constants.DisplayMode;
import com.loopme.constants.StretchOption;
import com.loopme.constants.VideoState;
import com.loopme.constants.WebviewState;
import com.loopme.mraid.MraidView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdController {

    private static final String LOG_TAG = AdController.class.getSimpleName();

    private static final String LEVEL1 = "level1";
    private static final String LEVEL2 = "level2";
    private static final String LEVEL3 = "level3";
    private static final String LEVEL4 = "level4";
    private static final String SLICER1 = "slicer1";
    private static final String SLICER2 = "slicer2";
    private static final String EXTRA_URL = "url";
    private BaseAd mBaseAd;

    private AdView mAdView;
    private MraidView mMraidView;
    private boolean mIsVideoPresented;

    private BaseAd mAd;

    private int mDisplayMode = DisplayMode.NORMAL;
    private int mPrevDisplayMode = DisplayMode.NORMAL;

    private MinimizedMode mMinimizedMode;
    private LoopMeBannerView mMinimizedView;

    //we should ignore first command
    private boolean mIsFirstFullScreenCommand = true;
    private boolean mHorizontalScrollOrientation;
    private boolean mIsBackFromExpand;
    private boolean mPostponePlay;

    private VideoLoader mVideoLoader;

    private int mPostponePlayPosition;
    private String mFileRest;

    private VideoController mVideoController;
    private IViewController mViewController;
    private MraidController mMraidController;

    private volatile Bridge.Listener mBridgeListener;
    private View.OnTouchListener mOnTouchListener;
    private boolean mHtmlAd;

    public AdController(BaseAd ad) {
        mBaseAd = ad;
        mAdView = new AdView(mBaseAd.getContext());
        mBridgeListener = initBridgeListener();
        mAdView.addBridgeListener(mBridgeListener);

        mOnTouchListener = initOnTouchListener();
        mAdView.setOnTouchListener(mOnTouchListener);
        if (mBaseAd != null) {
            mBaseAd.setNativeAd(true);
        }
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

        mVideoController = new VideoController(mAdView, callback, mBaseAd.getAppKey(), mBaseAd.getAdFormat());
    }

    public void pauseVideo() {
        if (mVideoController != null) {
            mVideoController.pauseVideo();
        }
    }

    public void initViewController() {
        if (!mBaseAd.getAdParams().isVideo360()) {
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

    public void initControllers(boolean mraid) {
        if (mraid) {
            Logging.out(LOG_TAG, "initMraidController");
            mMraidController = new MraidController(mBaseAd);
        }
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
                mBaseAd.onAdVideoDidReachEnd();
            }

            @Override
            public void onFail(LoopMeError error) {
                mBaseAd.onAdLoadFail(error);
            }

            @Override
            public void onVideoSizeChanged(int width, int height) {
                mViewController.setVideoSize(width, height);
            }

            @Override
            public void onPostponePlay(int position) {
                mPostponePlay = true;
                mPostponePlayPosition = position;
            }

            @Override
            public void onPlaybackFinishedWithError() {
                if (mBaseAd.getAdFormat() == AdFormat.BANNER) {
                    ((LoopMeBanner) mBaseAd).playbackFinishedWithError();
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
        if (mMraidView != null) {
            mMraidView.stopLoading();
            mMraidView.clearCache(true);
            mMraidView = null;
        }
        mMinimizedMode = null;
    }

    public void setWebViewState(int state) {
        if (mAdView != null) {
            mAdView.setWebViewState(state);
        }
    }

    public void setMraidWebViewState(int state) {
        if (mMraidView != null) {
            mMraidView.setWebViewState(state);
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

    void buildMraidContainer(ViewGroup bannerView) {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        bannerView.addView(mMraidView, layoutParams);
    }

    void buildVideoAdView(ViewGroup bannerView) {
        if (mViewController != null) {
            mViewController.buildVideoAdView(mBaseAd.getContext(), bannerView, mAdView);
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

                        Animation anim = AnimationUtils.makeOutAnimation(mBaseAd.getContext(),
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

        LoopMeBannerView initialView = ((LoopMeBanner) mBaseAd).getBannerView();
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

    void preloadHtml(String html, boolean mraid) {
        if (mraid) {
            mMraidView = new MraidView(mBaseAd.getContext(), mMraidController);
            mMraidView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);

        } else {
            if (mAdView != null) {
                preloadAd(html);
            } else {
                mBaseAd.onAdLoadFail(new LoopMeError("Html loading error"));
            }
        }
    }

    private void preloadAd(String html) {
        if (mBaseAd.getAdFormat() == AdFormat.INTERSTITIAL) {
            dummyPreloadHtml(html);
        } else {
            loadHtml(html);

        }
    }

    MraidView getMraidView() {
        return mMraidView;
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
                handleLoadNoneHtmlAdSuccess();
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
            public void onJsFullscreenMode(boolean isFullScreen) {
                handleFullscreenMode(isFullScreen);
            }

            @Override
            public void onNonLoopMe(String url) {
                handleNonLoopMe(url);
            }

            @Override
            public void onHtmlAdOpens() {
                if (isHtmlAd()) {
                    setWebViewState(WebviewState.VISIBLE);
                }
            }

            @Override
            public void onJsVideoStretch(boolean b) {
                handleVideoStretch(b);
            }
        };
    }

    private void handleLoadHtmlAdSuccess() {
        if (isHtmlAd()) {
            handleLoadSuccess();
        }
    }

    private void handleLoadNoneHtmlAdSuccess() {
        if (!isHtmlAd()) {
            handleLoadSuccess();
        }
    }

    private void handleLoadSuccess() {
        mBaseAd.startExpirationTimer();
        mBaseAd.onAdLoadSuccess();
    }

    private void handleLoadFail(String message) {
        if (mBaseAd != null) {
            mBaseAd.onAdLoadFail(new LoopMeError("Failed to process ad" + message));
        }
    }

    private void handleVideoLoad(String videoUrl) {
        Logging.out(LOG_TAG, "JS command: load video " + videoUrl);
        mIsVideoPresented = true;
        boolean preload = mBaseAd.getAdParams().getPartPreload();
        mVideoController.contain360(mBaseAd.getAdParams().isVideo360());
        loadVideoFile(videoUrl, mBaseAd.getContext(), preload);
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
                    if (mBaseAd.isShowing()) {
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
        if (mBaseAd != null) {
            mBaseAd.onAdLoadFail(error);
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
        if(mVideoController != null) {
            mVideoController.playVideo(time, mBaseAd.getAdParams().isVideo360());
        }
        if (mDisplayMode == DisplayMode.MINIMIZED) {
            Utils.animateAppear(mMinimizedView);
        }
    }

    private void handleVideoPause(int time) {
        mVideoController.pauseVideo();
    }

    private void handleClose() {
        mBaseAd.dismiss();
    }

    private void handleVideoStretch(boolean b) {
        Logging.out(LOG_TAG, "JS command: stretch video ");
        StretchOption stretch = b ? StretchOption.STRETCH : StretchOption.NO_STRETCH;
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
        mBaseAd.getContext().sendBroadcast(intent);
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
            AdUtils.startAdActivity(mBaseAd);
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
        Context context = mBaseAd.getContext();
        if (Utils.isOnline(context)) {
            Intent intent = new Intent(context, AdBrowserActivity.class);
            intent.putExtra(EXTRA_URL, url);
            intent.putExtra(StaticParams.APPKEY_TAG, mBaseAd.getAppKey());
            intent.putExtra(StaticParams.FORMAT_TAG, mBaseAd.getAdFormat());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            mBaseAd.onAdClicked();
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
        mBaseAd.getContext().sendBroadcast(intent);
    }

    private void surfaceTextureAvailable(SurfaceTexture surface) {
        Logging.out(LOG_TAG, "onSurfaceTextureAvailable");

        mVideoController.setSurfaceTextureAvailable(true);
        if (mPostponePlay) {
            mVideoController.playVideo(mPostponePlayPosition, mBaseAd.getAdParams().isVideo360());
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
                viewWidth = mBaseAd.detectWidth();
                viewHeight = mBaseAd.detectHeight();
                break;

            case DisplayMode.FULLSCREEN:
                viewWidth = Utils.getScreenWidth();
                viewHeight = Utils.getScreenHeight();
                break;

            default:
                Logging.out(LOG_TAG, "Unknown display mode");
                break;
        }
        try {
            Surface s = new Surface(surface);
            mVideoController.setSurface(s);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        mViewController.setViewSize(viewWidth, viewHeight);
    }

    private boolean surfaceTextureDestroyed() {
        Logging.out(LOG_TAG, "onSurfaceTextureDestroyed");
        mVideoController.setSurfaceTextureAvailable(false);
        try {
            mVideoController.setSurface(null);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void loadHtmlAdToWebView(String html, boolean mraid) {
        if (!mraid && mBaseAd.getAdFormat() == AdFormat.INTERSTITIAL && isHtmlAd()) {
            loadHtml(html);
        }
    }

    private void loadHtml(String html) {
        if (mAdView != null) {
            mAdView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void dummyPreloadHtml(String html) {
        BaseWebView dummyWebView = new BaseWebView(mBaseAd.getContext());
        dummyWebView.enableJavascriptCaching();
        dummyWebView.setWebViewClient(new WebViewClient() {

            private static final String WEBVIEW_HTML_AD = "loopme://webview/success";
            private static final String WEBVIEW_FAIL = "loopme://webview/fail";

            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                if (TextUtils.equals(url, WEBVIEW_HTML_AD)) {
                    mHtmlAd = true;
                    handleLoadHtmlAdSuccess();
                } else if (TextUtils.equals(url, WEBVIEW_FAIL)) {
                    handleLoadFail("Ad received specific URL loopme://webview/fail");
                } else {
                    loadHtmlRunnable(mBaseAd.getAdParams().getHtml());
                }
                webView.stopLoading();
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String
                    failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                if (mBridgeListener != null) {
                    mBridgeListener.onJsLoadFail("onReceivedError " + description);
                }
            }
        });
        dummyWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }


    public boolean isHtmlAd() {
        return mHtmlAd;
    }

    private void loadHtmlRunnable(final String html) {
        mBaseAd.mHandler.post(new Runnable() {
            @Override
            public void run() {
                loadHtml(html);
            }
        });

    }

    public AdView getAdView() {
        return mAdView;
    }

    public MraidController getMraidController() {
        return mMraidController;
    }


    public void resumeVideo() {
        if (mVideoController != null) {
            mVideoController.resumeVideo();
        }
    }

    public int getWebViewState() {
        if (mAdView != null) {
            return mAdView.getCurrentWebViewState();
        } else {
            return WebviewState.CLOSED;
        }
    }
}
