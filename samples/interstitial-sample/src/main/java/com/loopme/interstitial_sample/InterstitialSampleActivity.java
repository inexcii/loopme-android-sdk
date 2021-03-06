package com.loopme.interstitial_sample;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.loopme.LoopMeInterstitial;
import com.loopme.common.LoopMeError;

public class InterstitialSampleActivity extends AppCompatActivity implements LoopMeInterstitial.Listener, View.OnClickListener {

    private LoopMeInterstitial mInterstitial;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        findViewById(R.id.show_button).setOnClickListener(this);
        findViewById(R.id.load_button).setOnClickListener(this);
        initProgressDialog();
    }

    @Override
    protected void onPause() {
        cancelDialog();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        cancelDialog();
        // Clean up resources
        destroyInterstitial();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.show_button: {
                onShowClicked();
                break;
            }
            case R.id.load_button: {
                onLoadClicked();
            }
        }

    }

    private void onLoadClicked() {
        // Create new interstitial object
        if (mInterstitial == null) {
            LoopMeInterstitial.setAutoLoading(false);
            mInterstitial = LoopMeInterstitial.getInstance(LoopMeInterstitial.TEST_PORT_INTERSTITIAL, this);
        }

        if (mInterstitial != null) {
            showProgress();

            // Adding listener to receive SDK notifications during the
            // loading/displaying ad processes
            mInterstitial.setListener(this);

            // Start loading immediately
            mInterstitial.load();
        }
    }

    private void onShowClicked() {
        // Checks whether ad ready to be shown
        if (mInterstitial != null && mInterstitial.isReady()) {
            // Show ad
            mInterstitial.show();
        } else {
            Toast.makeText(getApplicationContext(), R.string.interstitial_is_not_ready, Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.show();
        }
    }

    private void initProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.loading));
        mProgressDialog.setMessage(getString(R.string.please_wait));
        mProgressDialog.setCancelable(false);
    }

    private void cancelDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
    }

    private void destroyInterstitial() {
        if (mInterstitial != null) {
            mInterstitial.destroy();
        }
    }

    private void dismissProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onLoopMeInterstitialLoadSuccess(LoopMeInterstitial interstitial) {
        dismissProgress();
    }

    @Override
    public void onLoopMeInterstitialLoadFail(LoopMeInterstitial interstitial, LoopMeError error) {
        dismissProgress();
        Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoopMeInterstitialShow(LoopMeInterstitial interstitial) {
    }

    @Override
    public void onLoopMeInterstitialHide(LoopMeInterstitial interstitial) {
    }

    @Override
    public void onLoopMeInterstitialClicked(LoopMeInterstitial interstitial) {
    }

    @Override
    public void onLoopMeInterstitialLeaveApp(LoopMeInterstitial interstitial) {
    }

    @Override
    public void onLoopMeInterstitialExpired(LoopMeInterstitial interstitial) {
        dismissProgress();
    }

    @Override
    public void onLoopMeInterstitialVideoDidReachEnd(LoopMeInterstitial interstitial) {
    }
}
