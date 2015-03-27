package com.loopme.loopme_example;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Toast;

import com.loopme.LoopMeError;
import com.loopme.LoopMeInterstitial;

public class LoopMeExampleActivity extends Activity implements LoopMeInterstitial.Listener {

	private static final String VIDEO_INTERSTITIAL_APP_KEY = "3ee6fc7a45";
	
    private LoopMeInterstitial mInterstitial;
    private ProgressDialog mProgressDialog;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        initProgressDialog();
	}
    
    private void initProgressDialog() {
    	mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading");
        mProgressDialog.setMessage("Please wait");
        mProgressDialog.setCancelable(false);
    }

    public void onLoadClicked(View view){
        mProgressDialog.show();

        // Create interstitial object
        mInterstitial = new LoopMeInterstitial(this, VIDEO_INTERSTITIAL_APP_KEY);
        
        /*
		 * Adding listener to receive SDK notifications during the
		 * loading/displaying ad processes
		 */
        mInterstitial.setListener(this);
        
        // Start loading immediately
        mInterstitial.load();
    }

    public void onShowClicked(View view){
        // Checks wether ad ready to be shown
    	if (mInterstitial != null && mInterstitial.isReady()) {
        	// Show ad
        	mInterstitial.show();
        } else {
        	Toast.makeText(getApplicationContext(), "Interstitial is not ready", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onPause() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
        }
        if (mInterstitial != null) {
        	// Clean up resources
        	mInterstitial.destroy();
        }
        super.onDestroy();
    }

	@Override
	public void onLoopMeInterstitialLoadSuccess(LoopMeInterstitial interstitial) {
		mProgressDialog.dismiss();
	}

	@Override
	public void onLoopMeInterstitialLoadFail(LoopMeInterstitial interstitial,
			LoopMeError error) {
		mProgressDialog.dismiss();
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
	}

	@Override
	public void onLoopMeInterstitialVideoDidReachEnd(LoopMeInterstitial arg0) {
	}
}
