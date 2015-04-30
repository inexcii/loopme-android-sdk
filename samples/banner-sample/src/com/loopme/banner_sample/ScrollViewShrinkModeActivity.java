package com.loopme.banner_sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.loopme.LoopMeBanner;
import com.loopme.LoopMeBannerView;
import com.loopme.LoopMeError;
import com.loopme.MinimizedMode;

public class ScrollViewShrinkModeActivity extends Activity implements LoopMeBanner.Listener{
	
	private LoopMeBanner mBanner;
	private ScrollView mScrollView;
	private LoopMeBannerView mAdSpace;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scrollview_layout);
		
		/*
		 * find ad space in layout
		 */
		mAdSpace = (LoopMeBannerView) findViewById(R.id.video_ad_spot);
		mScrollView = (ScrollView) findViewById(R.id.scrollview);
		
		/*
		 * Getting already initialized ad object or create new one with specified appKey
		 */
		mBanner = LoopMeBanner.getInstance(LoopMeBanner.TEST_MPU_BANNER, this);
		
		RelativeLayout root = (RelativeLayout) findViewById(R.id.scrollview_root);
		MinimizedMode mode = new MinimizedMode(root);
		mBanner.setMinimizedMode(mode);
		/*
		 * Adding listener to receive SDK notifications during the
		 * loading/displaying ad processes
		 */
		mBanner.setListener(this);

		/*
		 * Binding banner view
		 */
		mBanner.bindView(mAdSpace);

		/*
		 * Load ad. If ad already loaded it will not cause new loading process
		 */
		mBanner.load();
		
		mScrollView.getViewTreeObserver().addOnScrollChangedListener(
				new ViewTreeObserver.OnScrollChangedListener() {
			
			@Override
			public void onScrollChanged() {
				if (mBanner != null) {
					/*
					 * Manages the ad visibility inside the scrollView during scrolling
					 * It automatically calculates the ad area visibility
					 * and pauses any actions currently happening inside the banner ad 
					 * (whether it's a video or animations) if the ad is less than 50% visible, otherwise resumes
					 */
					mBanner.showAdIfItVisible(mScrollView);
				}
			}
		});
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
			mBanner.showAdIfItVisible(mScrollView);
		}
		super.onResume();
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
		Toast.makeText(getApplicationContext(), LoopMeError.getCodeMessage(arg1), Toast.LENGTH_SHORT).show();
		
	}

	@Override
	public void onLoopMeBannerLoadSuccess(LoopMeBanner arg0) {
		// Important: call showAdIfItVisible() inside onGlobalLayout() callback 
		mAdSpace.setVisibility(View.VISIBLE);
		
		final ViewTreeObserver observer = mAdSpace.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (mBanner != null) {
					// Manages the ad visibility if no scrolling happened before 
					mBanner.showAdIfItVisible(mScrollView);
				}
				mAdSpace.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});
	}
	
	@Override
	public void onLoopMeBannerShow(LoopMeBanner arg0) {
	}

	@Override
	public void onLoopMeBannerVideoDidReachEnd(LoopMeBanner arg0) {
	}
}

