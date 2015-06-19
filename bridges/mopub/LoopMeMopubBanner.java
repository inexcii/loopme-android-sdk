package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ListView;
import android.widget.ScrollView;
import com.loopme.LoopMeAdapter;
import com.loopme.LoopMeBanner;
import com.loopme.LoopMeBannerView;

import java.util.Map;

public class LoopMeMopubBanner extends CustomEventBanner implements LoopMeBanner.Listener {

    private static final String LOG_TAG = LoopMeMopubBanner.class.getSimpleName();

    private static LoopMeBanner mBanner;
    private String mLoopMeAppId;

    private CustomEventBanner.CustomEventBannerListener mBannerListener;
    private Activity mActivity;
    private LoopMeBannerView mLoopMeBannerView;

    @Override
    protected void loadBanner(Context context, CustomEventBannerListener customEventBannerListener, Map<String, Object> map, Map<String, String> map1) {
        Log.d(LOG_TAG, "Bridge loadBanner");

        mBannerListener = customEventBannerListener;
        mActivity = null;
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        } else {
            // You may also pass in an Activity Context in the localExtras map and retrieve it here.
        }

        mLoopMeAppId = map1.get("app_key");
        mLoopMeBannerView = (LoopMeBannerView) map.get("bannerView");
        if (mLoopMeBannerView == null) {
            Log.d(LOG_TAG, "LoopMeBannerView is null");
        } else {
            Log.d(LOG_TAG, "LoopMeBannerView is correct");
        }

        mBanner = LoopMeBanner.getInstance(mLoopMeAppId, mActivity);
        mBanner.bindView(mLoopMeBannerView);
        mBanner.setListener(this);
        mBanner.load();
    }

    @Override
    public void onInvalidate() {
    }

    @Override
    public void onLoopMeBannerLoadSuccess(LoopMeBanner loopMeBanner) {
        MoPubView view = new MoPubView(mActivity);
        mBannerListener.onBannerLoaded(view);

        mLoopMeBannerView.setVisibility(View.VISIBLE);
        final ViewTreeObserver observer = mLoopMeBannerView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mBanner != null) {
                    mBanner.show();
                }
                mLoopMeBannerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    public static void pause() {
        if (mBanner != null) {
            mBanner.pause();
        }
    }

    public static void resume() {
        if (mBanner != null) {
            mBanner.show();
        }
    }

    public static void resume(ScrollView scrollview) {
        if (mBanner != null) {
            mBanner.showAdIfItVisible(scrollview);
        }
    }

    public static void resume(LoopMeAdapter adapter, ListView listview) {
        if (mBanner != null) {
            mBanner.showAdIfItVisible(adapter, listview);
        }
    }

    public static void destroy() {
        if (mBanner != null) {
            mBanner.dismiss();
            mBanner.destroy();
        }
    }

    @Override
    public void onLoopMeBannerLoadFail(LoopMeBanner loopMeBanner, int i) {
        mBannerListener.onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
    }

    @Override
    public void onLoopMeBannerShow(LoopMeBanner loopMeBanner) {

    }

    @Override
    public void onLoopMeBannerHide(LoopMeBanner loopMeBanner) {

    }

    @Override
    public void onLoopMeBannerClicked(LoopMeBanner loopMeBanner) {
        mBannerListener.onBannerClicked();
    }

    @Override
    public void onLoopMeBannerLeaveApp(LoopMeBanner loopMeBanner) {
        mBannerListener.onLeaveApplication();
    }

    @Override
    public void onLoopMeBannerVideoDidReachEnd(LoopMeBanner loopMeBanner) {

    }

    @Override
    public void onLoopMeBannerExpired(LoopMeBanner loopMeBanner) {

    }
}
