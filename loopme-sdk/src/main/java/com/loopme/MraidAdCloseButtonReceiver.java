package com.loopme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.loopme.common.StaticParams.EXTRAS_CUSTOM_CLOSE;

/**
 * Created by vynnykiakiv on 4/5/17.
 */

public class MraidAdCloseButtonReceiver extends BroadcastReceiver {

    private MraidAdCloseButtonListener mListener;

    public MraidAdCloseButtonReceiver(MraidAdCloseButtonListener listener) {
        this.mListener = listener;
    }

    interface MraidAdCloseButtonListener {
        void onCloseButtonVisibilityChanged(boolean customCloseButton);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (mListener != null && intent.getExtras() != null) {
            boolean customCloseButton = intent.getExtras().getBoolean(EXTRAS_CUSTOM_CLOSE);
            mListener.onCloseButtonVisibilityChanged(customCloseButton);
        }
    }
}
