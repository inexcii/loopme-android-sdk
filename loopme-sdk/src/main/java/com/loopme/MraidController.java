package com.loopme;

import android.content.Context;
import android.content.Intent;

import com.loopme.common.Logging;
import com.loopme.common.LoopMeError;
import com.loopme.common.MraidOrientation;
import com.loopme.common.StaticParams;
import com.loopme.common.Utils;
import com.loopme.constants.AdFormat;
import com.loopme.constants.WebviewState;
import com.loopme.mraid.MraidBridge;
import com.loopme.mraid.MraidState;
import com.loopme.mraid.MraidVideoActivity;
import com.loopme.mraid.MraidView;

import static com.loopme.common.StaticParams.EXTRAS_CUSTOM_CLOSE;

public class MraidController implements MraidBridge.OnMraidBridgeListener {

    private static final String LOG_TAG = MraidController.class.getSimpleName();
    private static final String EXTRAS_URL = "url";
    private static final String EXTRAS_VIDEO_URL = "videoUrl";

    private BaseAd mBaseAd;
    private MraidView mMraidView;
    private int mWidth;
    private int mHeight;
    private boolean mAllowOrientationChange = true;
    private MraidOrientation mForceOrientation = MraidOrientation.NONE;

    public MraidController(BaseAd ad) {
        mBaseAd = ad;
    }

    public void setMraidView(MraidView mv) {
        mMraidView = mv;
    }

    @Override
    public void close() {
        if (isExpanded()) {
            Logging.out(LOG_TAG, "collapse banner");
            mBaseAd.getAdController().collapseMraidBanner();
        } else {
            Logging.out(LOG_TAG, "close");
            mBaseAd.dismiss();
        }
    }

    @Override
    public void open(String url) {
        Logging.out(LOG_TAG, "open " + url);
        Context context = mMraidView.getContext();
        if (Utils.isOnline(context)) {
            Intent intent = new Intent(context, AdBrowserActivity.class);
            intent.putExtra(EXTRAS_URL, url);
            Utils.setAdIdOrAppKey(intent, mBaseAd);
            intent.putExtra(StaticParams.FORMAT_TAG, mBaseAd.getAdFormat());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(intent);
        } else {
            Logging.out(LOG_TAG, "No internet connection");
        }
    }

    @Override
    public void resize(int w, int h) {
        Logging.out(LOG_TAG, "resize");
        if (mBaseAd.getAdFormat() == AdFormat.BANNER) {
            LoopMeBannerGeneral banner = (LoopMeBannerGeneral) mBaseAd;
            banner.getBannerView().setViewSize(w, h);
            mMraidView.resize();
            mMraidView.setState(MraidState.RESIZED);
            mMraidView.notifySizeChangeEvent(w, h);
            mMraidView.setIsViewable(true);
        }
    }

    @Override
    public void playVideo(String url) {
        Logging.out(LOG_TAG, "playVideo");
        Intent intent = new Intent(mMraidView.getContext(), MraidVideoActivity.class);
        intent.putExtra(EXTRAS_VIDEO_URL, url);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mMraidView.getContext().startActivity(intent);
        mMraidView.setIsViewable(true);
    }

    @Override
    public void expand(boolean isExpand) {
        Logging.out(LOG_TAG, "expand " + isExpand);
        mMraidView.setState(MraidState.EXPANDED);
        AdUtils.startMraidActivity(mBaseAd, isExpand);
    }

    @Override
    public void onLoadSuccess() {
        if (mMraidView != null) {
            mMraidView.setState(MraidState.DEFAULT);
            mMraidView.notifyReady();
            mBaseAd.onAdLoadSuccess();
        }
    }

    @Override
    public void onChangeCloseButtonVisibility(boolean hasOwnCloseButton) {
        mBaseAd.getAdParams().setOwnCloseButton(hasOwnCloseButton);
        broadcastCloseButtonIntent(hasOwnCloseButton);
    }

    @Override
    public void onMraidCallComplete(String command) {
        if (mMraidView != null) {
            mMraidView.onNativeCallComplete(command);
        }
    }

    @Override
    public void onLoopMeCallComplete(String command) {
        if (mMraidView != null) {
            mMraidView.onLoopMeCallComplete(command);
        }
    }

    @Override
    public void setOrientationProperties(boolean allowOrientationChange, MraidOrientation forceOrientation) {
        mAllowOrientationChange = allowOrientationChange;
        mForceOrientation = forceOrientation;
    }

    @Override
    public void onLoadFail(LoopMeError error) {
        mBaseAd.onAdLoadFail(error);
    }

    private void broadcastCloseButtonIntent(boolean hasOwnCloseButton) {
        Intent intent = new Intent();
        intent.setAction(StaticParams.MRAID_NEED_CLOSE_BUTTON);
        intent.putExtra(EXTRAS_CUSTOM_CLOSE, hasOwnCloseButton);
        mBaseAd.getContext().sendBroadcast(intent);
    }

    public void onCollapseBanner() {
        mMraidView.notifySizeChangeEvent(mWidth, mHeight);
        mMraidView.setState(MraidState.DEFAULT);
        mMraidView.setIsViewable(true);
        mMraidView.setWebViewState(WebviewState.VISIBLE);
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setBannerSize(String html) {
        HtmlParser parser = new HtmlParser(html);
        mWidth = Utils.convertDpToPixel(parser.getAdWidth());
        mHeight = Utils.convertDpToPixel(parser.getAdHeight());
    }

    public int getForceOrientation() {
        return mForceOrientation.getActivityInfoOrientation();
    }

    public void destroy() {
        if (isExpanded()) {
            Utils.broadcastDestroyIntent(mBaseAd.getContext(), mBaseAd.getAdId());
        }
    }

    private boolean isExpanded() {
        return mMraidView != null && mMraidView.isExpanded();
    }
}
