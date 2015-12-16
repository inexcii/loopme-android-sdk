package com.loopme;

import android.os.CountDownTimer;

public class AdFetcherTimer extends CountDownTimer {

    private static final String LOG_TAG = AdFetcherTimer.class.getSimpleName();

    private Listener mListener;

    public interface Listener {
        void onTimeout();
    }

    public AdFetcherTimer(long millisInFuture, Listener listener) {
        super(millisInFuture, 1000 * 60);
        mListener = listener;
        Logging.out(LOG_TAG, "Start fetcher timeout");
    }

    @Override
    public void onTick(long millisUntilFinished) {
    }

    @Override
    public void onFinish() {
        if (mListener != null) {
            mListener.onTimeout();
        }
    }
}
