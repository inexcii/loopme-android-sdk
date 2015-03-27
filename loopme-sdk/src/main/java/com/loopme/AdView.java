package com.loopme;

import android.content.Context;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.PluginState;

import com.loopme.Logging.LogLevel;

/**
 * Custom ad view. 
 * Communicate with javascript.
 *
 */
public class AdView extends WebView implements BridgeInterface, Bridge.Listener {

	private static final String LOG_TAG = AdView.class.getSimpleName();
	
	private Bridge.Listener mBridgeListener;
	private volatile Bridge mBridge;
	private VideoState mCurrentVideoState;
	private WebviewState mViewState = WebviewState.CLOSED;
	
	public AdView(Context context) {
		super(context);
		init();
	}
	
	enum WebviewState {
		VISIBLE,
		HIDDEN,
		CLOSED;
	}
	
	/**
	 * Add listener of js commands
	 */
	public void addBridgeListener(Bridge.Listener listener) {
		mBridgeListener = listener;
	}
	
	private void init() {
		WebSettings webSettings = getSettings();
		webSettings.setJavaScriptEnabled(true);

		webSettings.setPluginState(PluginState.ON);

		setVerticalScrollBarEnabled(false);
		setHorizontalScrollBarEnabled(false);

		webSettings.setSupportZoom(false);
		setWebChromeClient(new WebChromeClient());
		
		mBridge = new Bridge(this);
		setWebViewClient(mBridge);
	}
	
	public VideoState getCurrentVideoState() {
		return mCurrentVideoState;
	}
	
	public WebviewState getCurrentViewState() {
		return mViewState;
	}

	@Override
	public void onAppear() {
		mViewState = WebviewState.VISIBLE;
		Logging.out(LOG_TAG, "WEBVIEW : VISIBLE", LogLevel.DEBUG);
		String command = new BridgeCommandBuilder().webviewState(mViewState);
		loadUrl(command);
	}

	@Override
	public void onDisappear() {
		mViewState = WebviewState.CLOSED;
		Logging.out(LOG_TAG, "WEBVIEW : CLOSED", LogLevel.DEBUG);
		String command = new BridgeCommandBuilder().webviewState(mViewState);
		loadUrl(command);
	}
	
	@Override
	public void onHidden() {
		mViewState = WebviewState.HIDDEN;
		Logging.out(LOG_TAG, "WEBVIEW : HIDDEN", LogLevel.DEBUG);
		String command = new BridgeCommandBuilder().webviewState(mViewState);
		loadUrl(command);
	}

	@Override
	public void setVideoState(VideoState state) {
		mCurrentVideoState = state;
		Logging.out(LOG_TAG, "VIDEO : " + state.toString(), LogLevel.DEBUG);
		String command = new BridgeCommandBuilder().videoState(state);
		loadUrl(command);
	}

	@Override
	public void setVideoDuration(int duration) {
		Logging.out(LOG_TAG, "js video duration " + duration, LogLevel.DEBUG);
		String command = new BridgeCommandBuilder().videoDuration(duration);
		loadUrl(command);
	}

	@Override
	public void setVideoCurrentTime(int currentTime) {
		String command = new BridgeCommandBuilder().videoCurrentTime(currentTime);
		loadUrl(command);
	}

	@Override
	public void setVideoMute(boolean mute) {
		Logging.out(LOG_TAG, "MUTE : " + mute, LogLevel.DEBUG);
		String command = new BridgeCommandBuilder().videoMute(mute);
		loadUrl(command);
	}
	 
	public void shake() {
		Logging.out(LOG_TAG, "SHAKE", LogLevel.DEBUG);
		String command = new BridgeCommandBuilder().shake(true);
		loadUrl(command);
	}
	
	@Override
	public void sendNativeCallFinished() {
		Logging.out(LOG_TAG, "sendNativeCallFinished", LogLevel.DEBUG);
		String command = new BridgeCommandBuilder().isNativeCallFinished(true);
		loadUrl(command);
	}

	@Override
	public void onJsClose() {
		if (mBridgeListener != null) {
			mBridgeListener.onJsClose();
		}
	}

	@Override
	public void onJsLoadSuccess() {
		if (mBridgeListener != null) {
			mBridgeListener.onJsLoadSuccess();
		}
	}

	@Override
	public void onJsLoadFail(String mess) {
		if (mBridgeListener != null) {
			mBridgeListener.onJsLoadFail(mess);
		}
	}

	@Override
	public void onNonLoopMe(String url) {
		if (mBridgeListener != null) {
			mBridgeListener.onNonLoopMe(url);
		}
	}

	@Override
	public void onJsVideoLoad(String videoUrl) {
		if (mBridgeListener != null) {
			mBridgeListener.onJsVideoLoad(videoUrl);
		}
	}

	@Override
	public void onJsVideoMute(boolean mute) {
		if (mBridgeListener != null) {
			mBridgeListener.onJsVideoMute(mute);
		}
	}

	@Override
	public void onJsVideoPlay(int time) {
		if (mBridgeListener != null) {
			mBridgeListener.onJsVideoPlay(time);
		}
	}

	@Override
	public void onJsVideoPause(int time) {
		if (mBridgeListener != null) {
			mBridgeListener.onJsVideoPause(time);
		}
	}

	@Override
	public void onJsVideoStretch(boolean b) {
		if (mBridgeListener != null) {
			mBridgeListener.onJsVideoStretch(b);
		}
	}
}