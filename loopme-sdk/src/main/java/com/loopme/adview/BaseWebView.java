package com.loopme.adview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * Created by vynnykiakiv on 4/6/17.
 */

public class BaseWebView extends WebView {
    protected boolean mIsDestroyed;


    public BaseWebView(Context context) {
        super(context);
    }

    @Override
    public void destroy() {
        mIsDestroyed = true;
        BaseWebView.removeFromParent(this);
        removeAllViews();
        super.destroy();
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void enableJavascriptCaching() {
        getSettings().setJavaScriptEnabled(true);
        getSettings().setAppCacheEnabled(true);
        getSettings().setAppCachePath(getContext().getCacheDir().getAbsolutePath());
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
