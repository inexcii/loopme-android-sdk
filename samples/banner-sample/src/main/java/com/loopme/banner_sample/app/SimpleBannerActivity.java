package com.loopme.banner_sample.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.loopme.LoopMeBanner;
import com.loopme.LoopMeBannerView;
import com.loopme.common.LoopMeError;

public class SimpleBannerActivity extends AppCompatActivity implements LoopMeBanner.Listener {

    private LoopMeBanner mBanner;
    private LoopMeBannerView mAdSpace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.simplebanner_activity_layout);
        setTitle("Simple");

        mAdSpace = (LoopMeBannerView) findViewById(R.id.video_ad_spot);

        mBanner = LoopMeBanner.getInstance(LoopMeBanner.TEST_MPU_BANNER, this);
        mBanner.setListener(this);
        mBanner.bindView(mAdSpace);
        mBanner.load();
    }

    @Override
    protected void onPause() {
        if (mBanner != null) {
            mBanner.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mBanner != null) {
            mBanner.resume();
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (mBanner != null) {
            mBanner.dismiss();
            mBanner.removeListener();
        }
        super.onBackPressed();
    }

    @Override
    public void onLoopMeBannerClicked(LoopMeBanner arg0) {
    }

    @Override
    public void onLoopMeBannerExpired(LoopMeBanner arg0) {
    }

    @Override
    public void onLoopMeBannerHide(LoopMeBanner arg0) {
    }

    @Override
    public void onLoopMeBannerLeaveApp(LoopMeBanner arg0) {
    }

    @Override
    public void onLoopMeBannerLoadFail(LoopMeBanner arg0, LoopMeError arg1) {
        Toast.makeText(this, "LoadFail: " + arg1.getMessage(),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoopMeBannerLoadSuccess(LoopMeBanner arg0) {
        arg0.show();
    }

    @Override
    public void onLoopMeBannerShow(LoopMeBanner arg0) {
    }

    @Override
    public void onLoopMeBannerVideoDidReachEnd(LoopMeBanner arg0) {
    }
}
