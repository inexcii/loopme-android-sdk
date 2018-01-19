package com.loopme.mraid;


import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.webkit.WebSettings;

import com.loopme.MraidController;
import com.loopme.adview.AdViewChromeClient;
import com.loopme.adview.BaseWebView;
import com.loopme.adview.BridgeCommandBuilder;
import com.loopme.common.Logging;
import com.loopme.common.StaticParams;
import com.loopme.common.Utils;
import com.loopme.constants.WebviewState;

public class MraidView extends BaseWebView {
    private int mViewState = WebviewState.CLOSED;
    private static final String LOG_TAG = MraidView.class.getSimpleName();
    public static final String JAVASCRIPT = "javascript:";
    private String mCurrentAdState = "";

    public MraidView(Context context, MraidController mraidController) {
        super(context);
        init(mraidController);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void init(MraidController mraidController) {
        WebSettings webSettings = getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setPluginState(WebSettings.PluginState.ON);
        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        webSettings.setSupportZoom(false);

        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setDomStorageEnabled(true);
        Logging.out(LOG_TAG, "Encoding: " + webSettings.getDefaultTextEncodingName());

        setWebChromeClient(new AdViewChromeClient());

        mraidController.setMraidView(this);
        setWebViewClient(new MraidBridge(mraidController));
    }

    public void setIsViewable(boolean isViewable) {
        String command = "mraidbridge.setIsViewable(" + isViewable + ")";
        Logging.out(LOG_TAG, "setIsViewable " + isViewable);
        loadUrl(JAVASCRIPT + command);
    }

    public void notifyReady() {
        String command = "mraidbridge.notifyReadyEvent();";
        Logging.out(LOG_TAG, "notifyReady");
        loadUrl(JAVASCRIPT + command);
    }

    public void notifyError() {
        String command = "mraidbridge.notifyErrorEvent();";
        Logging.out(LOG_TAG, "notifyError");
        loadUrl(JAVASCRIPT + command);
    }

    public void notifyStateChange() {
        String command = "mraidbridge.notifyStateChangeEvent();";
        Logging.out(LOG_TAG, "state changed");
        loadUrl(JAVASCRIPT + command);
    }

    public void setState(String state) {
        if (!TextUtils.equals(mCurrentAdState, state)) {
            String command = "mraidbridge.setState('" + state + "');";
            mCurrentAdState = state;
            Logging.out(LOG_TAG, "setState " + state);
            loadUrl(JAVASCRIPT + command);
        }
    }

    public void notifySizeChangeEvent(int w, int h) {
        String command = "mraidbridge.notifySizeChangeEvent(" + w + "," + h + ");";
        Logging.out(LOG_TAG, "notifySizeChangeEvent");
        loadUrl(JAVASCRIPT + command);
    }

    public void resize() {
        String command = "mraidbridge.resize();";
        Logging.out(LOG_TAG, "resize " + command);
        loadUrl(JAVASCRIPT + command);
    }

    public void setWebViewState(int state) {
        if (mViewState != state) {
            mViewState = state;
            Logging.out(LOG_TAG, "MRAID WEBVIEW : " + WebviewState.toString(state));
            String command = new BridgeCommandBuilder().webviewState(mViewState);
            loadUrl(command);
        }
    }

    public void onNativeCallComplete(String command) {
        Logging.out(LOG_TAG, "onMraidCallComplete " + command);
        loadUrl(JAVASCRIPT + "mraidbridge.nativeCallComplete()");
    }

    public boolean isExpanded() {
        return TextUtils.equals(mCurrentAdState, MraidState.EXPANDED);
    }


    public void loadHtml(String html) {
        html = Utils.addMraidScript(html);
        loadDataWithBaseURL(StaticParams.BASE_URL_ANDROID_ASSET, html, StaticParams.MIME_TYPE_TEXT_HTML, StaticParams.UTF_8, null);
    }

    public void onLoopMeCallComplete(String completedCommand) {
        Logging.out(LOG_TAG, "onLoopMeCallComplete " + completedCommand);
        String command = new BridgeCommandBuilder().isNativeCallFinished(true);
        loadUrl(command);
    }
}
