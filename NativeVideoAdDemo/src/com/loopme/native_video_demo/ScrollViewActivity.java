package com.loopme.native_video_demo;

import com.example.listviewcustomdemo.R;
import com.loopme.LoopMeAdHolder;
import com.loopme.LoopMeBanner;
import com.loopme.LoopMeBannerView;
import com.loopme.LoopMeError;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;
import android.widget.Toast;

public class ScrollViewActivity extends Activity implements LoopMeBanner.Listener{
	
	private LoopMeBanner mBanner;
	private ScrollView mScrollView;
	private LoopMeBannerView mAdSpace;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scrollview_layout);
		
		// Getting already initialized ad object or create new one with
		// specified appKey
		mBanner = (LoopMeBanner) LoopMeAdHolder.getAd(LoopMeBanner.TEST_MPU_BANNER);
		if (mBanner == null) {
			// Create banner object
			mBanner = new LoopMeBanner(this, LoopMeBanner.TEST_MPU_BANNER);
			// Start loading immediately
			mBanner.load();
		}
		/*
		 * Adding listener to receive SDK notifications during the
		 * loading/displaying ad processes
		 */
		mBanner.setListener(this);
		
		// find ad space in layout
		mAdSpace = (LoopMeBannerView) findViewById(R.id.video_ad_spot);
		mScrollView = (ScrollView) findViewById(R.id.scrollview);
		
		mScrollView.getViewTreeObserver().addOnScrollChangedListener(
				new ViewTreeObserver.OnScrollChangedListener() {
			
			@Override
			public void onScrollChanged() {
				if (mBanner != null) {
					/**
					 * Manages the ad visibility inside the scrollView during scrolling
					 * It automatically calculates the ad area visibility
					 * and pauses any actions currently happening inside the banner ad 
					 * (whether it's a video or animations) if the ad is less than 50% visible, otherwise resumes
					 */
					mBanner.showAdIfItVisible(mScrollView);
				}
			}
		});

		// Binding banner view
		mBanner.bindView(mAdSpace);

		// Show ad
		mBanner.show();
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
			* Resume any actions currently happening inside banner ad (f.e to resume video playback) 			 
			*/
			mBanner.resume(mScrollView);
		}
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		if (mBanner != null) {
			/*
			 * Clean up resources if ad was already displayed
			 */
			if (mBanner.isShowing()) {
				mBanner.destroy();
			} else {
				/*
				 * Don't destroy ad If it wasn't yet displayed or still loading
				 */
				mBanner.removeListener();
			}
		}
		super.onDestroy();
	}
	
	private void toast(String mess) {
		Toast.makeText(this, mess, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onLoopMeBannerClicked(LoopMeBanner arg0) {
		toast("onLoopMeBannerClicked");
	}

	@Override
	public void onLoopMeBannerExpired(LoopMeBanner arg0) {
		toast("onLoopMeBannerExpired");
	}

	@Override
	public void onLoopMeBannerHide(LoopMeBanner arg0) {
		toast("onLoopMeBannerHide");
	}

	@Override
	public void onLoopMeBannerLeaveApp(LoopMeBanner arg0) {
		toast("onLoopMeBannerLeaveApp");
	}

	@Override
	public void onLoopMeBannerLoadFail(LoopMeBanner arg0, int arg1) {
		toast("onLoopMeBannerLoadFail");
	}

	@Override
	public void onLoopMeBannerLoadSuccess(LoopMeBanner arg0) {
		toast("onLoopMeBannerLoadSuccess");
		mAdSpace.setVisibility(View.VISIBLE);
		
		final ViewTreeObserver observer = mAdSpace.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (mBanner != null) {
					// Manages the ad visibility if no scrolling happened before 
					mBanner.showAdIfItVisible(mScrollView);
				}
				observer.removeGlobalOnLayoutListener(this);
			}
		});
	}

	@Override
	public void onLoopMeBannerShow(LoopMeBanner arg0) {
		toast("onLoopMeBannerShow");
	}

	@Override
	public void onLoopMeBannerVideoDidReachEnd(LoopMeBanner arg0) {
		toast("onLoopMeBannerVideoDidReachEnd");
	}
}
