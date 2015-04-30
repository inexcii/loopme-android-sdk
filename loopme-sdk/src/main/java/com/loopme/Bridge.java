package com.loopme;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Vibrator;
import android.text.TextUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.URI;
import java.net.URISyntaxException;

import com.loopme.Logging.LogLevel;

public class Bridge extends WebViewClient {
	
	private static final String LOG_TAG = Bridge.class.getSimpleName();
	
	private static final String LOOPME = "loopme";
	private static final String WEBVIEW = "webview";
	private static final String VIDEO = "video";
	
	private static final String WEBVIEW_CLOSE = "/close";
	private static final String WEBVIEW_FAIL = "/fail";
	private static final String WEBVIEW_SUCCESS = "/success";
	private static final String WEBVIEW_VIBRATE = "/vibrate";
	
	private static final String VIDEO_LOAD = "/load";
	private static final String VIDEO_MUTE = "/mute";
	private static final String VIDEO_PLAY = "/play";
	private static final String VIDEO_PAUSE = "/pause";
	private static final String VIDEO_ENABLE_STRETCH = "/enableStretching";
	private static final String VIDEO_DISABLE_STRETCH = "/disableStretching";
	
	private static final String QUERY_PARAM_SRC = "src";
	private static final String QUERY_PARAM_CURRENT_TIME = "currentTime";
	private static final String QUERY_PARAM_MUTE = "mute";

    private Listener mListener;
    
    public interface Listener {

    	void onJsClose();
    	void onJsLoadSuccess();
    	void onJsLoadFail(String mess);
    	
    	void onJsVideoLoad(String videoUrl);
    	void onJsVideoMute(boolean mute);
    	void onJsVideoPlay(int time);
    	void onJsVideoPause(int time);
    	void onJsVideoStretch(boolean b);

    	void onNonLoopMe(String url);
    }
    
    public Bridge(Bridge.Listener listener) {
    	if (listener != null) {
    		mListener = listener;
    	} else {
    		Logging.out(LOG_TAG, "VideoBridgeListener should not be null", LogLevel.ERROR);
    	}
    }
    
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
    	Logging.out(LOG_TAG, "shouldOverrideUrlLoading " + url, LogLevel.DEBUG);

    	if (TextUtils.isEmpty(url) ) {
    		return false;
    	}
    	
    	Context context = view.getContext();
    	URI redirect = null;
		try {
			redirect = new URI(url);
		} catch (URISyntaxException e) {
			Logging.out(LOG_TAG, e.getMessage(), LogLevel.ERROR);
			e.printStackTrace();
			return false;
		}
    	
    	String protocol = redirect.getScheme();
    	if (TextUtils.isEmpty(protocol)) {
    		return false;
    	}
    	
    	if (protocol.equalsIgnoreCase(LOOPME)) {
    		((AdView) view).sendNativeCallFinished();
    		String host = redirect.getHost();
    		String path = redirect.getPath();
    		if (TextUtils.isEmpty(host) || TextUtils.isEmpty(path)) {
    			return false;
    		}
    		
    		if (host.equalsIgnoreCase(WEBVIEW)) {
    			handleWebviewCommands(path, context);
    		} else if (host.equalsIgnoreCase(VIDEO)) {
    			handleVideoCommands(path, url);
    		} 
    	} else {
    		if (mListener != null) {
        		mListener.onNonLoopMe(url);
        	}
    	}
    	
        return true;
    }
    
    private void handleWebviewCommands(String str, Context context) {
    	if (str == null || mListener == null) {
    		return;
    	}
    	
    	if (str.equalsIgnoreCase(WEBVIEW_CLOSE)) {
    		mListener.onJsClose();
    		
    	} else if (str.equalsIgnoreCase(WEBVIEW_VIBRATE)) {
    		try {
    			Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    			if (v != null) {
    				v.vibrate(500);
    			}
    		} catch (Exception e) {
    			Logging.out(LOG_TAG, "Missing permission for vibrate", LogLevel.DEBUG);
    		}
    			
    	} else if (str.equalsIgnoreCase(WEBVIEW_FAIL)) {
    		mListener.onJsLoadFail("Ad received specific URL loopme://webview/fail");
    		
    	} else if (str.equalsIgnoreCase(WEBVIEW_SUCCESS)) {
    		mListener.onJsLoadSuccess();
    	}
    }
    
    private void handleVideoCommands(String str, String url) {
    	if (str == null || mListener == null) {
    		return;
    	}
    	Uri uri = null;
    	try {
    		uri = Uri.parse(url);
    	} catch (NullPointerException e) {
    		e.printStackTrace();
    	}
    	if (uri == null) {
    		return;
    	}
    	
    	if (str.equalsIgnoreCase(VIDEO_LOAD)) {
    		String videoUrl = uri.getQueryParameter(QUERY_PARAM_SRC);
    		mListener.onJsVideoLoad(videoUrl);
    	
    	} else if (str.equalsIgnoreCase(VIDEO_MUTE)) {
    		String muteStr = uri.getQueryParameter(QUERY_PARAM_MUTE);
    		mListener.onJsVideoMute(Boolean.parseBoolean(muteStr));
    		
    	} else if (str.equalsIgnoreCase(VIDEO_PLAY)) {
    		String playStr = uri.getQueryParameter(QUERY_PARAM_CURRENT_TIME);
    		int time = 0;
    		if (playStr != null) {
    			time = Integer.parseInt(playStr);
    		}
    		mListener.onJsVideoPlay(time);
    		
    	} else if (str.equalsIgnoreCase(VIDEO_PAUSE)) {
    		String pauseStr = uri.getQueryParameter(QUERY_PARAM_CURRENT_TIME);
    		int pause_time = 0;
    		if (pauseStr != null) {
    			pause_time = Integer.parseInt(pauseStr);
    		}
    		mListener.onJsVideoPause(pause_time);
    		
    	} else if (str.equalsIgnoreCase(VIDEO_ENABLE_STRETCH)) {
    		mListener.onJsVideoStretch(true);
    		
    	} else if (str.equalsIgnoreCase(VIDEO_DISABLE_STRETCH)) {
    		mListener.onJsVideoStretch(false);
    		
    	}
    }
    
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
		if (mListener != null) {
        	mListener.onJsLoadFail("onReceivedError " + description);
        }
    }

	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		Logging.out(LOG_TAG, "onPageStarted", LogLevel.DEBUG);
		super.onPageStarted(view, url, favicon);
	}
}
