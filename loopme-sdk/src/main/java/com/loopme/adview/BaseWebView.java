package com.loopme.adview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;

/**
 * Created by vynnykiakiv on 4/6/17.
 */

public class BaseWebView extends WebView {
    private static boolean sDeadlockCleared = false;
    protected boolean mIsDestroyed;


    public BaseWebView(Context context) {
        super(context);
        if (!sDeadlockCleared) {
            resolveWebViewDeadlock(getContext());
            sDeadlockCleared = true;
        }
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

    private void resolveWebViewDeadlock(@NonNull final Context context) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            final WebView webView = new WebView(context.getApplicationContext());
            webView.setBackgroundColor(Color.TRANSPARENT);
            webView.loadDataWithBaseURL(null, "", "text/html", "UTF-8", null);

            final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.width = 1;
            params.height = 1;
            params.type = WindowManager.LayoutParams.TYPE_TOAST;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
            params.format = PixelFormat.TRANSPARENT;
            params.gravity = Gravity.START | Gravity.TOP;

            final WindowManager windowManager =
                    (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            windowManager.addView(webView, params);
        }
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
