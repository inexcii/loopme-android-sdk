package com.loopme.mraid;


import android.content.Context;
import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.loopme.MraidController;
import com.loopme.adview.AdViewChromeClient;
import com.loopme.adview.BridgeCommandBuilder;
import com.loopme.common.Logging;
import com.loopme.constants.WebviewState;

public class MraidView extends WebView {
    private int mViewState = WebviewState.CLOSED;


    private static final String LOG_TAG = MraidView.class.getSimpleName();

    public static final String DEFAULT_CHROME_USER_AGENT = "Mozilla/5.0 (Linux; Android 4.4.2; " +
            "Nexus 4 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.114 " +
            "Mobile Safari/537.36";

    public static final String CHROME_USER_AGENT = initUserAgent();
    public static final String JAVASCRIPT = "javascript:";

    public MraidView(Context context, MraidController mraidController) {
        super(context);
        init(mraidController);
    }

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

        webSettings.setUserAgentString(CHROME_USER_AGENT);
        setWebChromeClient(new AdViewChromeClient());

        mraidController.setMraidView(this);
        setWebViewClient(new MraidBridge(mraidController));
    }

    private static String initUserAgent() {
        String agent = System.getProperty("http.agent");
        if (TextUtils.isEmpty(agent)) {
            agent = DEFAULT_CHROME_USER_AGENT;
        }
        return agent;
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
        String command = "mraidbridge.setState('" + state + "');";
        Logging.out(LOG_TAG, "setState " + state);
        loadUrl(JAVASCRIPT + command);
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
}
