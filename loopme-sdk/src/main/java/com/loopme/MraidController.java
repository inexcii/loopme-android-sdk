package com.loopme;

import android.content.Context;
import android.content.Intent;

import com.loopme.common.Logging;
import com.loopme.common.StaticParams;
import com.loopme.common.Utils;
import com.loopme.constants.AdFormat;
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

    public MraidController(BaseAd ad) {
        mBaseAd = ad;
    }

    public void setMraidView(MraidView mv) {
        mMraidView = mv;
    }

    @Override
    public void close() {
        Logging.out(LOG_TAG, "close");
        mBaseAd.dismiss();
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
        Intent i = new Intent(mMraidView.getContext(), MraidVideoActivity.class);
        i.putExtra(EXTRAS_VIDEO_URL, url);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mMraidView.getContext().startActivity(i);
        mMraidView.setIsViewable(true);
    }

    @Override
    public void expand(boolean isExpand) {
        Logging.out(LOG_TAG, "expand " + isExpand);
        AdUtils.startMraidActivity(mBaseAd, isExpand);
    }

    @Override
    public void onLoadSuccess() {
        mBaseAd.onAdLoadSuccess();
    }

    @Override
    public void onChangeCloseButtonVisibility(boolean hasOwnCloseButton) {
        mBaseAd.getAdParams().setOwnCloseButton(hasOwnCloseButton);
        broadcastCloseButtonIntent(hasOwnCloseButton);
    }

    private void broadcastCloseButtonIntent(boolean hasOwnCloseButton) {
        Intent intent = new Intent();
        intent.setAction(StaticParams.MRAID_NEED_CLOSE_BUTTON);
        intent.putExtra(EXTRAS_CUSTOM_CLOSE, hasOwnCloseButton);
        mBaseAd.getContext().sendBroadcast(intent);
    }
}
