package com.loopme.tasks;

import com.loopme.common.Logging;

public class RequestTimer implements Runnable {
    private static final String LOG_TAG = RequestTimer.class.getSimpleName();
    private int mTimeout;
    private Listener mListener;
    private Thread mThread;

    public void start() {
        mThread.start();
    }

    public void stop() {
        try {
            mThread.interrupt();
        } catch (SecurityException e) {
            Logging.out(LOG_TAG, e.getMessage());
        }
        Logging.out(LOG_TAG, LOG_TAG + " is successfully stopped.");
    }

    public interface Listener {
        void onTimeout();
    }

    public RequestTimer(int timeout, Listener listener) {
        this.mTimeout = timeout;
        this.mListener = listener;
        mThread = new Thread(this);
    }

    @Override
    public void run() {
        long timeNow = System.currentTimeMillis();
        long timeInFuture = timeNow + mTimeout;

        while (timeNow >= timeInFuture) {
            timeNow = System.currentTimeMillis();
        }

        if (mListener != null && !mThread.isInterrupted()) {
            mListener.onTimeout();
        }
    }
}
