package com.loopme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.loopme.common.StaticParams;

public class AdReceiver extends BroadcastReceiver {

    private Listener mListener;

    public interface Listener {
        void onDestroyBroadcast(int adToClose);

        void onClickBroadcast();
    }

    public AdReceiver(Listener listener) {
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mListener == null) {
            return;
        }

        if (intent.getAction().equalsIgnoreCase(StaticParams.DESTROY_INTENT)) {
            int adIdToClose = StaticParams.DEFAULT_AD_ID;
            if (intent.getExtras() != null) {
                adIdToClose = intent.getExtras().getInt(StaticParams.AD_ID_TAG);
            }
            mListener.onDestroyBroadcast(adIdToClose);

        } else if (intent.getAction().equalsIgnoreCase(StaticParams.CLICK_INTENT)) {
            mListener.onClickBroadcast();
        }
    }
}
