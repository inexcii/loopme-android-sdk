package com.loopme.banner_sample;

import com.loopme.LoopMeBanner;
import com.loopme.LoopMeBannerView;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;

public class SimpleBannerActivity extends Activity implements LoopMeBanner.Listener {

	private LoopMeBanner mBanner;
	private LoopMeBannerView mAdSpace;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.simplebanner_activity_layout);
		
		/*
		 * Find ad view in layout
		 */
		mAdSpace = (LoopMeBannerView) findViewById(R.id.video_ad_spot);
		
		/*
		 * Getting already initialized ad object or create new one with specified appKey
		 */
		mBanner = LoopMeBanner.getInstance(LoopMeBanner.TEST_MPU_BANNER, this);
		
		/*
		 * Adding listener to receive SDK notifications during the
		 * loading/displaying ad processes
		 */
		mBanner.setListener(this);
		
		/*
		 * Binding ad view 
		 */
		mBanner.bindView(mAdSpace);
		if (!mBanner.isReady()) {
			/*
			 * Start loading
			 */
			mBanner.load();
		} else {
			showAd(mBanner);
		}
	}
	
	@Override
	protected void onPause() {
		if (mBanner != null) {
			/*					
			* Pause any actions currently happening inside banner ad (f.e to pause video playback) 			 
			*/
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
			mBanner.show();
		}
		super.onResume();
	}
	
	private void showAd(final LoopMeBanner banner) {
		// Important: call show() inside onGlobalLayout() callback
		mAdSpace.setVisibility(View.VISIBLE);
		final ViewTreeObserver observer = mAdSpace.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (banner != null) {
					/*
					 * Show banner
					 */
					banner.show();
				}
				mAdSpace.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		if (mBanner != null) {
            /*
             *  Below methods (dismiss, removeListener) can be invoked in onBackPressed(),
             *  onDestroy() or even in another Activity. It depends from your integration.
             */
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
	public void onLoopMeBannerLoadFail(LoopMeBanner arg0, int arg1) {
	}

	@Override
	public void onLoopMeBannerLoadSuccess(LoopMeBanner arg0) {
		showAd(arg0);
	}

	@Override
	public void onLoopMeBannerShow(LoopMeBanner arg0) {
	}

	@Override
	public void onLoopMeBannerVideoDidReachEnd(LoopMeBanner arg0) {
	}
}
