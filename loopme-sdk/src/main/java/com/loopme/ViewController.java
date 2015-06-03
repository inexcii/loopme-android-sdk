package com.loopme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.loopme.AdView.WebviewState;
import com.loopme.Logging.LogLevel;

class ViewController implements TextureView.SurfaceTextureListener {
	
	private static final String LOG_TAG = ViewController.class.getSimpleName();
	
	private static final String EXTRA_URL = "url";
	private static final String EXTRA_APPKEY = "appkey";
	private static final String EXTRA_FORMAT = "format";

	private AdView mAdView;
	private volatile Bridge.Listener mBridgeListener;
	
	private VideoController mVideoController;

	private boolean mIsVideoPresented;
	
	private BaseAd mAd;
	
	private TextureView mTextureView;

    private DisplayMode mDisplayMode;
	private MinimizedMode mMinimizedMode;
	private LoopMeBannerView mMinimizedView;

	public ViewController(BaseAd ad) {
		mAd = ad;
		mAdView = new AdView(mAd.getContext());
		mBridgeListener = initBridgeListener();
		mAdView.addBridgeListener(mBridgeListener);
        mAdView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return (event.getAction() == MotionEvent.ACTION_MOVE);
			}
		});
		mVideoController = new VideoController(mAd.getAppKey(), mAdView, mAd.getAdFormat());
	}
	
	void destroy(boolean interruptFile) {
		mBridgeListener = null;
		if (mVideoController != null) {
			mVideoController.destroy(interruptFile);
			mVideoController = null;
		}
		((Activity) mAd.getContext()).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (mAdView != null) {
					mAdView.stopLoading();
					mAdView.clearCache(true);
					mAdView.destroy();
					mAdView = null;
					Logging.out(LOG_TAG, "AdView destroyed", LogLevel.DEBUG);
				}
			}
		});
		mMinimizedMode = null;
	}

    void setWebViewState(WebviewState state) {
        if (mAdView != null) {
            mAdView.setWebViewState(state);
        }
    }

	void onAdShake() {
		if (mAdView != null) {
			mAdView.shake();
		}
	}
	
	VideoState getCurrentVideoState() {
		if (mAdView != null) {
			return mAdView.getCurrentVideoState();
		}
		return null;
	}
	
	void buildStaticAdView(ViewGroup bannerView) {
		if (bannerView == null || mAdView == null) {
			return;
		}
		mAdView.setBackgroundColor(Color.BLACK);
		bannerView.addView(mAdView);
	}
	
	void buildVideoAdView(ViewGroup bannerView) {
		mTextureView = new TextureView(mAd.getContext());
		mTextureView.setBackgroundColor(Color.TRANSPARENT);
		mTextureView.setSurfaceTextureListener(this);
		
		mAdView.setBackgroundColor(Color.TRANSPARENT);
		mAdView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
		bannerView.setBackgroundColor(Color.BLACK);
        bannerView.addView(mTextureView, 0);
		if (mAdView.getParent() != null) {
			((ViewGroup) mAdView.getParent()).removeView(mAdView);
		}
		bannerView.addView(mAdView, 1);
	}
	
	void rebuildView(ViewGroup bannerView) {
		if (bannerView == null || mAdView == null || mTextureView == null) {
			return;
		}
		bannerView.setBackgroundColor(Color.BLACK);
		if (mTextureView.getParent() != null) {
			((ViewGroup) mTextureView.getParent()).removeView(mTextureView);
		}
		if (mAdView.getParent() != null) {
			((ViewGroup) mAdView.getParent()).removeView(mAdView);
		}
		
		bannerView.addView(mTextureView, 0);
		bannerView.addView(mAdView, 1);
	}
	
	void ensureAdIsVisible(View view) {
		if (mAdView == null || view == null) {
			return;
		}
		
		Rect rect = new Rect();
		boolean b = view.getGlobalVisibleRect(rect);

		int halfOfView = view.getHeight() / 2;
		int rectHeight = rect.height();

		if (b) {

			if (rectHeight < halfOfView) {
				setWebViewState(WebviewState.HIDDEN);
			}
			else if (rectHeight >= halfOfView) {
				setWebViewState(WebviewState.VISIBLE);
			}
		}
	}
	
	void switchToMinimizedMode() {
		if (mDisplayMode == DisplayMode.MINIMIZED) {
			if (getCurrentVideoState() == VideoState.PAUSED) {
                setWebViewState(WebviewState.VISIBLE);
			} 
			return;
		}
        Logging.out(LOG_TAG, "switchToMinimizedMode", LogLevel.DEBUG);
        mDisplayMode = DisplayMode.MINIMIZED;

		int width = mMinimizedMode.getWidth();
		int height = mMinimizedMode.getHeight();
		mMinimizedView = new LoopMeBannerView(mAdView.getContext(), width, height);
		
        rebuildView(mMinimizedView);
		addBordersToView(mMinimizedView);

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
        Logging.out(LOG_TAG, "switchToNormalMode", LogLevel.DEBUG);
        mDisplayMode = DisplayMode.NORMAL;

        LoopMeBannerView initialView = ((LoopMeBanner) mAd).getBannerView();
        initialView.setVisibility(View.VISIBLE);

		if (mMinimizedView != null && mMinimizedView.getParent() != null) {
			((ViewGroup) mMinimizedView.getParent()).removeView(mMinimizedView);
			rebuildView(initialView);
			mMinimizedView.removeAllViews();
		}

		mAdView.setOnTouchListener(null);
	}
	
	void setMinimizedMode(MinimizedMode mode) {
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
            Logging.out(LOG_TAG, "loadDataWithBaseURL", LogLevel.DEBUG);
			mAdView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
		} else {
            mAd.onAdLoadFail(LoopMeError.HTML_LOADING);
        }
	}
	
	public boolean isVideoPresented() {
		return mIsVideoPresented;
	}
	
	private Bridge.Listener initBridgeListener() {
		return new Bridge.Listener() {
			
			@Override
			public void onJsVideoPlay(int time) {
				onAdVideoPlay(time);
			}
			
			@Override
			public void onJsVideoPause(final int time) {
				onAdVideoPause(time);
			}
			
			@Override
			public void onJsVideoMute(boolean mute) {
				onAdVideoMute(mute);
			}
			
			@Override
			public void onJsVideoLoad(final String videoUrl) {
				onAdVideoLoad(videoUrl);
			}
			
			@Override
			public void onJsLoadSuccess() {
				onAdLoadSuccess();
			}
			
			@Override
			public void onJsClose() {
				onAdClose();
			}

			@Override
			public void onJsLoadFail(String mess) {
				onAdLoadFail(mess);
			}

			@Override
			public void onNonLoopMe(String url) {
				onAdNonLoopMe(url);
			}

			@Override
			public void onJsVideoStretch(boolean b) {
				onAdVideoStretch(b);
			}
		};
	}
	
	VideoController getVideoController() {
		return mVideoController;
	}
	
	private void loadFail(BaseAd baseAd, int error) {
		baseAd.onAdLoadFail(error);
	}
	
	private void onAdLoadSuccess() {
		Logging.out(LOG_TAG, "JS command: load success", LogLevel.DEBUG);
		mAd.startExpirationTimer();
		mAd.onAdLoadSuccess();
	}
	
	private void onAdLoadFail(String mess) {
		Logging.out(LOG_TAG, "JS command: load fail", LogLevel.DEBUG);
		loadFail(mAd, LoopMeError.SPECIFIC_HOST);
	}
	
	private void onAdVideoLoad(final String videoUrl) {
		Logging.out(LOG_TAG, "JS command: load video " + videoUrl, LogLevel.DEBUG);
		
		mIsVideoPresented = true;
		mVideoController.loadVideoFile(videoUrl, mAd.getContext());
	}
	
	private void onAdVideoMute(boolean mute) {
		Logging.out(LOG_TAG, "JS command: video mute " + mute, LogLevel.DEBUG);

		if (mVideoController != null) {
			mVideoController.muteVideo(mute);
		}
	}
	
	private void onAdVideoPlay(final int time) {
		Logging.out(LOG_TAG, "JS command: play video " + time, LogLevel.DEBUG);

		if (mVideoController != null) {
			mVideoController.playVideo(time);
		}

        if (mDisplayMode == DisplayMode.MINIMIZED) {
            Utils.animateAppear(mMinimizedView);
        }
	}
	
	private void onAdVideoPause(int time) {
		Logging.out(LOG_TAG, "JS command: pause video " + time, LogLevel.DEBUG);
        if (mVideoController != null) {
        	mVideoController.pauseVideo(time);
        }
	}
	
	private void onAdClose() {
		Logging.out(LOG_TAG, "JS command: close", LogLevel.DEBUG);
        mAd.dismiss();
	}
	
	private void onAdVideoStretch(boolean b) {
		Logging.out(LOG_TAG, "JS command: stretch video ", LogLevel.DEBUG);
		if (mVideoController != null) {
			if (b) {
				mVideoController.setStreachVideoParameter(VideoController.StretchOption.STRECH);
			} else {
				mVideoController.setStreachVideoParameter(VideoController.StretchOption.NO_STRETCH);
			}
		}
	}
	
	private void onAdNonLoopMe(String url) {
		Logging.out(LOG_TAG, "Non Js command", LogLevel.DEBUG);
		Context context = mAd.getContext();
		if (Utils.isOnline(context)) {
			Intent intent = new Intent(context, AdBrowserActivity.class);
			intent.putExtra(EXTRA_URL, url);
			intent.putExtra(EXTRA_APPKEY, mAd.getAppKey());
			intent.putExtra(EXTRA_FORMAT, mAd.getAdFormat().toString());
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			mAd.onAdClicked();
            setWebViewState(WebviewState.HIDDEN);
			broadcastAdClickedIntent();
			
			context.startActivity(intent);
		} else {
			Logging.out(LOG_TAG, "No internet connection", LogLevel.DEBUG);
		}
	}
	
	private void broadcastAdClickedIntent() {
		Intent intent = new Intent();
		intent.setAction(StaticParams.CLICK_INTENT);
		mAd.getContext().sendBroadcast(intent);
	}
	
	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
			int height) {

        Logging.out(LOG_TAG, "onSurfaceTextureAvailable", LogLevel.DEBUG);

		int viewWidth;
        int viewHeight;

        if (mDisplayMode == DisplayMode.MINIMIZED && mMinimizedMode != null) {

            viewWidth = mMinimizedMode.getWidth();
            viewHeight = mMinimizedMode.getHeight();

        } else {
            viewWidth = mAd.detectWidth();
            viewHeight = mAd.detectHeight();
        }

        if (mVideoController != null) {
			mVideoController.setSurface(mTextureView);
            mVideoController.resizeVideo(mTextureView, viewWidth, viewHeight);
        }
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
			int height) {
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		Logging.out(LOG_TAG, "onSurfaceTextureDestroyed", LogLevel.DEBUG);
		return false;
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
	}
}
