package com.loopme.adview;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;

import com.loopme.common.Logging;
import com.loopme.constants.VideoState;
import com.loopme.constants.WebviewState;

/**
 * Custom ad view.
 * Communicate with javascript.
 */
public class AdView extends WebView implements BridgeInterface, Bridge.Listener {

    private static final String LOG_TAG = AdView.class.getSimpleName();

    public static final String DEFAULT_CHROME_USER_AGENT = "Mozilla/5.0 (Linux; Android 4.4.2; Nexus 4 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.114 Mobile Safari/537.36";

    public static final String CHROME_USER_AGENT = initUserAgent();

    private Bridge.Listener mBridgeListener;
    private volatile Bridge mBridge;

    private int mCurrentVideoState = VideoState.IDLE;
    private int mViewState = WebviewState.CLOSED;

    public AdView(Context context) {
        super(context);
        init();
    }

    private static String initUserAgent() {
        String agent = System.getProperty("http.agent");
        if (TextUtils.isEmpty(agent)) {
            agent = DEFAULT_CHROME_USER_AGENT;
        }
        return agent;
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

        webSettings.setUserAgentString(CHROME_USER_AGENT);

        setWebChromeClient(new AdViewChromeClient());

        mBridge = new Bridge(this);
        setWebViewClient(mBridge);
    }

    public int getCurrentVideoState() {
        return mCurrentVideoState;
    }

    @Override
    public void setWebViewState(int state) {
        if (mViewState != state) {
            mViewState = state;
            Logging.out(LOG_TAG, "WEBVIEW : " + WebviewState.toString(state));
            String command = new BridgeCommandBuilder().webviewState(mViewState);
            loadUrl(command);
        }
    }

    @Override
    public void setFullscreenMode(boolean mode) {
        String command = new BridgeCommandBuilder().fullscreenMode(mode);
        loadUrl(command);
    }

    public int getCurrentWebViewState() {
        return mViewState;
    }

    @Override
    public void setVideoState(int state) {
        if (mCurrentVideoState != state) {
            mCurrentVideoState = state;
            Logging.out(LOG_TAG, "VIDEO : " + VideoState.toString(state));
            String command = new BridgeCommandBuilder().videoState(state);
            loadUrl(command);
        }
    }

    @Override
    public void setVideoDuration(int duration) {
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
        Logging.out(LOG_TAG, "MUTE : " + mute);
        String command = new BridgeCommandBuilder().videoMute(mute);
        loadUrl(command);
    }

    public void shake() {
        Logging.out(LOG_TAG, "SHAKE");
        String command = new BridgeCommandBuilder().shake(true);
        loadUrl(command);
    }

    @Override
    public void sendNativeCallFinished() {
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
    public void onJsFullscreenMode(boolean b) {
        if (mBridgeListener != null) {
            mBridgeListener.onJsFullscreenMode(b);
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