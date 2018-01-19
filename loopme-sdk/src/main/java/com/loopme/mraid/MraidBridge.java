package com.loopme.mraid;

import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.loopme.common.Logging;
import com.loopme.common.LoopMeError;
import com.loopme.common.MraidOrientation;
import com.loopme.common.StaticParams;
import com.loopme.common.Utils;
import com.loopme.debugging.ErrorLog;

import java.net.URI;
import java.net.URISyntaxException;

public class MraidBridge extends WebViewClient {

    private static final String LOG_TAG = MraidBridge.class.getSimpleName();

    private static final String LOOPME_SCHEME = "loopme";
    private static final String MRAID_SCHEME = "mraid";
    private static final String CUSTOM_HTTP_SCHEME = "http";
    private static final String CUSTOM_HTTPS_SCHEME = "https";

    private static final String QUERY_PARAMETER_URI = "uri";
    private static final String QUERY_PARAMETER_URL = "url";
    private static final String QUERY_PARAMETER_WIDTH = "width";
    private static final String QUERY_PARAMETER_HEIGHT = "height";
    private static final String QUERY_PARAMETER_OFFSET_X = "offsetX";
    private static final String QUERY_PARAMETER_OFFSET_Y = "offsetY";
    private static final String QUERY_PARAMETER_CUSTOM_CLOSE = "shouldUseCustomClose";
    private static final String QUERY_PARAMETER_CUSTOM_CLOSE_POSITION = "customClosePosition";
    private static final String QUERY_PARAMETER_ALLOW_ORIENTATION_CHANGE = "allowOrientationChange";
    private static final String QUERY_PARAMETER_FORCE_ORIENTATION = "forceOrientation";
    private static final String ALLOW_OFF_SCREEN = "allowOffscreen";

    private static final String CLOSE = "close";
    private static final String OPEN = "open";
    private static final String PLAY_VIDEO = "playVideo";
    private static final String RESIZE = "resize";
    private static final String EXPAND = "expand";
    private static final String USE_CUSTOM_CLOSE = "usecustomclose";
    private static final String SET_ORIENTATION_PROPERTIES = "setOrientationProperties";
    private static final int START_URLS_INDEX = 17;
    private static final String WEBVIEW_FAIL = "/fail";
    private static final String WEBVIEW_SUCCESS = "/success";

    public interface OnMraidBridgeListener {
        void close();

        void open(String url);

        void resize(int w, int h);

        void playVideo(String videoUrl);

        void expand(boolean b);

        void onLoadSuccess();

        void onLoadFail(LoopMeError error);

        void onChangeCloseButtonVisibility(boolean hasOwnCloseButton);

        void onMraidCallComplete(String command);

        void onLoopMeCallComplete(String command);

        void setOrientationProperties(boolean allowOrientationChange, MraidOrientation forceOrientation);
    }

    private OnMraidBridgeListener mOnMraidBridgeListener;

    public MraidBridge(OnMraidBridgeListener onMraidBridgeListener) {
        mOnMraidBridgeListener = onMraidBridgeListener;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (TextUtils.isEmpty(url)) {
            notifyError(view, "Broken redirect in mraid: " + url);
            return false;
        }
        try {
            URI redirect = new URI(url);
            String protocol = redirect.getScheme();
            if (TextUtils.isEmpty(protocol)) {
                return false;
            }
            if (protocol.equalsIgnoreCase(MRAID_SCHEME)) {
                String host = redirect.getHost();
                handleMraidCommand(host, url);
                return true;
            }

            if (protocol.equalsIgnoreCase(LOOPME_SCHEME)) {
                String path = redirect.getPath();
                handleLoopMeCommand(path, url);
                return true;
            }

            if (isHttpProtocol(protocol)) {
                mOnMraidBridgeListener.open(url);
                return true;
            }
        } catch (URISyntaxException e) {
            notifyError(view, "Broken redirect in bridge: " + url);
            return false;
        }
        return false;
    }

    private void handleLoopMeCommand(String path, String url) {
        if (mOnMraidBridgeListener != null) {
            mOnMraidBridgeListener.onLoopMeCallComplete(url);
        }
        switch (path) {
            case WEBVIEW_FAIL: {
                loadFail(new LoopMeError("Ad received specific URL loopme://webview/fail"));
                break;
            }
            case WEBVIEW_SUCCESS: {
                loadSuccess();
                break;
            }
            default: {
                break;
            }
        }

    }

    private boolean isHttpProtocol(String protocol) {
        return TextUtils.equals(protocol, CUSTOM_HTTP_SCHEME) || TextUtils.equals(protocol, CUSTOM_HTTPS_SCHEME);
    }

    private void notifyError(View view, String errorMessage) {
        ErrorLog.post(errorMessage);
        ((MraidView) view).notifyError();
        Logging.out(LOG_TAG, errorMessage);
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        ((MraidView) view).notifyError();
    }

    private void handleMraidCommand(String command, String url) {
        Uri uri = Uri.parse(url);
        switch (command) {

            case USE_CUSTOM_CLOSE: {
                boolean hasOwnCloseButton = detectBooleanQueryParameter(uri, QUERY_PARAMETER_CUSTOM_CLOSE);
                mOnMraidBridgeListener.onChangeCloseButtonVisibility(hasOwnCloseButton);
                break;
            }

            case EXPAND:
                boolean custom = detectBooleanQueryParameter(uri, QUERY_PARAMETER_CUSTOM_CLOSE);
                mOnMraidBridgeListener.expand(custom);
                break;

            case RESIZE:
                //mraid://resize?width=320&height=250&offsetX=0&offsetY=0&customClosePosition=top-right&allowOffscreen=false
                String width = detectQueryParameter(uri, QUERY_PARAMETER_WIDTH);
                int w = Integer.parseInt(width);
                String height = detectQueryParameter(uri, QUERY_PARAMETER_HEIGHT);
                int h = Integer.parseInt(height);
                String offsetX = detectQueryParameter(uri, QUERY_PARAMETER_OFFSET_X);
                String offsetY = detectQueryParameter(uri, QUERY_PARAMETER_OFFSET_Y);
                String customClosePosition = detectQueryParameter(uri, QUERY_PARAMETER_CUSTOM_CLOSE_POSITION);
                String allowOffscreen = detectQueryParameter(uri, ALLOW_OFF_SCREEN);
                mOnMraidBridgeListener.resize(Utils.convertDpToPixel(w), Utils.convertDpToPixel(h));
                break;

            case CLOSE:
                mOnMraidBridgeListener.close();
                break;

            case OPEN:
                String openUrl = url.substring(START_URLS_INDEX);
                Logging.out(LOG_TAG, String.valueOf(openUrl));
                mOnMraidBridgeListener.open(openUrl);
                break;

            case PLAY_VIDEO:
                String videoUrl = detectQueryParameter(uri, QUERY_PARAMETER_URI);
                Logging.out(LOG_TAG, String.valueOf(videoUrl));
                mOnMraidBridgeListener.playVideo(videoUrl);
                break;

            case SET_ORIENTATION_PROPERTIES: {
                boolean allowOrientationChange = detectBooleanQueryParameter(uri, QUERY_PARAMETER_ALLOW_ORIENTATION_CHANGE);
                MraidOrientation forceOrientation = detectOrientation(uri, QUERY_PARAMETER_FORCE_ORIENTATION);
                mOnMraidBridgeListener.setOrientationProperties(allowOrientationChange, forceOrientation);
                break;
            }
            default:
                break;

        }
        mOnMraidBridgeListener.onMraidCallComplete(command);
    }

    private void loadFail(LoopMeError error) {
        if (mOnMraidBridgeListener != null) {
            mOnMraidBridgeListener.onLoadFail(error);
        }
    }

    private void loadSuccess() {
        if (mOnMraidBridgeListener != null) {
            mOnMraidBridgeListener.onLoadSuccess();
        }
    }

    private String detectQueryParameter(Uri uri, String parameter) {
        String result = null;
        try {
            result = uri.getQueryParameter(parameter);
        } catch (NullPointerException | UnsupportedOperationException e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean detectBooleanQueryParameter(Uri uri, String parameter) {
        String result = detectQueryParameter(uri, parameter);
        return Boolean.parseBoolean(result);
    }

    private String detectStringQueryParameter(Uri uri, String parameter) {
        return detectQueryParameter(uri, parameter);
    }

    private MraidOrientation detectOrientation(Uri uri, String parameter) {
        String orientation = detectStringQueryParameter(uri, parameter);
        if (StaticParams.ORIENTATION_PORT.equals(orientation)) {
            return MraidOrientation.PORTRAIT;
        } else if (StaticParams.ORIENTATION_LAND.equals(orientation)) {
            return MraidOrientation.LANDSCAPE;
        } else {
            return MraidOrientation.NONE;
        }
    }
}
