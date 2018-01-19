package com.loopme;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.loopme.adview.AdView;
import com.loopme.constants.StretchOption;
import com.loopme.video360.MDVRLibrary;

public interface IViewController {

    void setViewSize(int w, int h);
    void setVideoSize(int w, int h);
    void buildVideoAdView(Context c, ViewGroup vg, AdView av);

    void rebuildView(ViewGroup vg, AdView av, int displayMode);
    void setStretchParam(StretchOption option);

    void onPause();
    void onResume();
    void onDestroy();
    boolean handleTouchEvent(MotionEvent event);
    void initVRLibrary(Context context);
}
