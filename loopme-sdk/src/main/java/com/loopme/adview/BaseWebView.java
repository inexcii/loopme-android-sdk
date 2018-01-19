package com.loopme.adview;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.loopme.common.StaticParams;

public class BaseWebView extends WebView {
    protected boolean mIsDestroyed;

    public BaseWebView(Context context) {
        super(context);
        allowCookies();
        allowMixedContent();
        allowWebViewDebug();
    }

    private void allowWebViewDebug() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && StaticParams.DEBUG_MODE) {
            setWebContentsDebuggingEnabled(true);
        }
    }

    protected void allowCookies() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(this, true);
        }
        CookieManager.setAcceptFileSchemeCookies(true);
    }

    private void allowMixedContent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }
    }

    @Override
    public void destroy() {
        mIsDestroyed = true;
        BaseWebView.removeFromParent(this);
        removeAllViews();
        super.destroy();
    }

    public static void removeFromParent(@Nullable View view) {
        if (view == null || view.getParent() == null) {
            return;
        }

        if (view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }
}
