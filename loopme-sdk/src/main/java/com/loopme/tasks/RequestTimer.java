package com.loopme.tasks;

import android.os.CountDownTimer;

import com.loopme.common.Logging;

public class RequestTimer extends CountDownTimer {

    private static final String LOG_TAG = RequestTimer.class.getSimpleName();
    private static final long TICK_INTERVAL_IN_MILLIS = 1000;
    private Listener mListener;

    public RequestTimer(long millisInFuture, Listener requestTimerListener) {
        super(millisInFuture, TICK_INTERVAL_IN_MILLIS);
        this.mListener = requestTimerListener;
    }

    public void startTimer(){
        Logging.out(LOG_TAG, "start request timer");
        start();
    }

    public void stopTimer(){
        Logging.out(LOG_TAG, "stop request timer");
        cancel();
    }

    @Override
    public void onTick(long millisUntilFinished) {

    }

    @Override
    public void onFinish() {
        if(mListener != null){
            mListener.onTimeout();
        }
    }

    public interface Listener{
        void onTimeout();
    }
}
