package com.loopme.banner_sample.app;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.loopme.LoopMeBanner;
import com.loopme.LoopMeBannerView;

import android.os.Bundle;
import com.loopme.LoopMeError;

public class SimpleBannerActivity extends AppCompatActivity implements LoopMeBanner.Listener {

	private LoopMeBanner mBanner;
	private LoopMeBannerView mAdSpace;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.simplebanner_activity_layout);
		setTitle("Simple");
		
		mAdSpace = (LoopMeBannerView) findViewById(R.id.video_ad_spot);
		
		/*
		 * Getting already initialized ad object or create new one with specified appKey
		 */
		mBanner = LoopMeBanner.getInstance(LoopMeBanner.TEST_MPU_BANNER,
				this.getApplicationContext());

		if (mBanner != null) {
			mBanner.setListener(this);
			mBanner.bindView(mAdSpace);
			mBanner.load();
		}
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
			/*
			 * Resume actions inside banner (f.e. resume video playback)
			 */
			mBanner.show(null, null);
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
		Toast.makeText(this, "Load Fail: " + arg1.getMessage(),
			Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onLoopMeBannerLoadSuccess(LoopMeBanner arg0) {
		arg0.show(null, null);
	}

	@Override
	public void onLoopMeBannerShow(LoopMeBanner arg0) {
	}

	@Override
	public void onLoopMeBannerVideoDidReachEnd(LoopMeBanner arg0) {
	}
}
