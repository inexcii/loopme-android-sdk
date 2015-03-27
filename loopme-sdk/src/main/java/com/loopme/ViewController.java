package com.loopme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.loopme.AdView.WebviewState;
import com.loopme.Logging.LogLevel;

class ViewController implements UserIteractionListener, Callback {
	
	private static final String LOG_TAG = ViewController.class.getSimpleName();
	
	private static final String EXTRA_URL = "url";
	private static final String EXTRA_APPKEY = "appkey";

	private AdView mAdView;
	private volatile Bridge.Listener mBridgeListener;
	
	private VideoController mVideoController;

	private boolean mIsVideoPresented;
	
	private BaseAd mAd;
	
	private SurfaceView mSurfaceView;
	
	public ViewController(BaseAd ad) {
		mAd = ad;
		mAdView = new AdView(mAd.getContext());
		mBridgeListener = initBridgeListener();
		mAdView.addBridgeListener(mBridgeListener);
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
	}
	
	void onAdAppear() {
		if (mAdView != null) {
			mAdView.onAppear();
		}
	}
	
	void onAdDisappear() {
		if (mAdView != null) {
			mAdView.onDisappear();
		}
	}
	
	void onAdHidden() {
		if (mAdView != null) {
			mAdView.onHidden();
		}
	}
	
	void onAdShake() {
		if (mAdView != null) {
			mAdView.shake();
		}
	}
	
	WebviewState getCurrentViewState() {
		if (mAdView != null) {
			return mAdView.getCurrentViewState();
		}
		return null;
	}
	
	VideoState getCurrentVideoState() {
		if (mAdView != null) {
			return mAdView.getCurrentVideoState();
		}
		return null;
	}
	
	void buildStaticAdView(ViewGroup bannerView) {
		mAdView.setBackgroundColor(Color.BLACK);
		bannerView.addView(mAdView);
	}
	
	void buildVideoAdView(ViewGroup bannerView) {
		mSurfaceView = new SurfaceView(mAd.getContext());
		SurfaceHolder holder = mSurfaceView.getHolder();
		holder.addCallback(this);
		
		mAdView.setBackgroundColor(Color.TRANSPARENT);
		mAdView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
		bannerView.setBackgroundColor(Color.BLACK);
		bannerView.addView(mSurfaceView, 0);
		bannerView.addView(mAdView, 1);
	}
	
	void ensureAdIsVisible(View view) {
		if (mAdView == null || view == null) {
			return;
		}
		
		Rect rect = new Rect();
		boolean b = view.getGlobalVisibleRect(rect);
		
		int halfOfView = view.getHeight() / 2;
		if (rect.height() == 0) {
			return;
		} 
		if (rect.height() < halfOfView) {
			if (mAdView.getCurrentViewState() != WebviewState.HIDDEN) {
				Logging.out(LOG_TAG, "visibility less then 50%", LogLevel.DEBUG);
				mAdView.onHidden();
			}
		} 
		else if (rect.height() >= halfOfView) {
			if (mAdView.getCurrentViewState() != WebviewState.VISIBLE) {
				Logging.out(LOG_TAG, "visibility more then 50%", LogLevel.DEBUG);
				mAdView.onAppear();
			}
		}
	}
	
	void preloadHtml(String html) {
		if (mAdView != null) {
			mAdView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
		}
	}
	
	boolean isAdVisibleEnough(View view) {
		if (mAdView == null || view == null) {
			return false;
		}
		Rect rect = new Rect();
		boolean b = view.getGlobalVisibleRect(rect);
		int halfOfView = view.getHeight() / 2;
		if (rect.height() < halfOfView) {
			return false;
		} 
		else {
			return true;
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
			public void onJsVideoPause(int time) {
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
	
	boolean isValidMediaPlayer() {
		return (mVideoController != null) && 
				(mVideoController.getPlayer() != null);
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
		
		mVideoController = new VideoController(mAd.getAppKey(), mAdView);
		mVideoController.loadVideoFile(videoUrl, mAd.getContext(), new VideoController.Listener() {
			
			@Override
			public void onError(String mess) {
				loadFail(mAd, LoopMeError.VIDEO_LOADING);
			}
		});
	}
	
	private void onAdVideoMute(boolean mute) {
		Logging.out(LOG_TAG, "JS command: video mute " + mute, LogLevel.DEBUG);

		if (mVideoController != null) {
			mVideoController.muteVideo(mute);
		}
	}
	
	private void onAdVideoPlay(int time) {
		Logging.out(LOG_TAG, "JS command: play video " + time, LogLevel.DEBUG);
		onPlayCommand(time);
	}
	
	private void onAdVideoPause(int time) {
		Logging.out(LOG_TAG, "JS command: pause video " + time, LogLevel.DEBUG);
		onPauseCommand(time);
	}
	
	private void onAdClose() {
		Logging.out(LOG_TAG, "JS command: close", LogLevel.DEBUG);
		onCloseButtonPressed();
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
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			mAd.onAdClicked();
			mAdView.onHidden();
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
	public void onCloseButtonPressed() {
		mAd.dismiss();
	}

	@Override
	public void onPlayCommand(int time) {
		if (mVideoController != null) {
			mVideoController.playVideo(time);
		}
	}

	@Override
	public void onPauseCommand(int time) {
		if (mVideoController != null) {
			mVideoController.pauseVideo(time);
		}
	}
	
	private void resizeVideo(SurfaceView surfaceView, SurfaceHolder holder, int viewWidth, int viewHeight) {
		if (mVideoController != null) {
			mVideoController.resizeVideo(surfaceView, holder, viewWidth, viewHeight);
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		final Surface surface = holder.getSurface();

	    //in android 4.0-4.1 surfaceCreated can return invalid surface
		//http://stackoverflow.com/questions/18451854/the-surface-has-been-released-inside-surfacecreated
		if (surface == null || !surface.isValid()) {
	    	return;
	    }

		int viewWidth = mAd.detectWidth();
		int viewHeight = mAd.detectHeight();
		resizeVideo(mSurfaceView, holder, viewWidth, viewHeight);

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}
}
