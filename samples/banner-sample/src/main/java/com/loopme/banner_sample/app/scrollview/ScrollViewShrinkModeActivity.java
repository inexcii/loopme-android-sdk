package com.loopme.banner_sample.app.scrollview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.loopme.banner_sample.app.R;
import com.loopme.LoopMeBanner;
import com.loopme.LoopMeBannerView;
import com.loopme.LoopMeError;
import com.loopme.MinimizedMode;

public class ScrollViewShrinkModeActivity extends AppCompatActivity implements LoopMeBanner.Listener{
	
	private LoopMeBanner mBanner;
	private ScrollView mScrollView;
	private LoopMeBannerView mAdSpace;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scrollview_layout);
		setTitle("ScrollView with Shrink mode");
		
		mAdSpace = (LoopMeBannerView) findViewById(R.id.video_ad_spot);
		mScrollView = (ScrollView) findViewById(R.id.scrollview);
		
		/*
		 * Getting already initialized ad object or create new one with specified appKey
		 */
		mBanner = LoopMeBanner.getInstance(LoopMeBanner.TEST_MPU_BANNER, this.getApplicationContext());

		if (mBanner != null) {
			/*
			* Optional: setup minimized mode
			 */
			RelativeLayout root = (RelativeLayout) findViewById(R.id.scrollview_root);
			MinimizedMode mode = new MinimizedMode(root);
			mBanner.setMinimizedMode(mode);

			mBanner.setListener(this);
			mBanner.bindView(mAdSpace);
			mBanner.load();
		}
		
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
					mBanner.show(null, mScrollView);
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
			mBanner.show(null, mScrollView);
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
	public void onLoopMeBannerLoadFail(LoopMeBanner arg0, LoopMeError arg1) {
		Toast.makeText(getApplicationContext(), arg1.getMessage(), Toast.LENGTH_SHORT).show();
		
	}

	@Override
	public void onLoopMeBannerLoadSuccess(LoopMeBanner arg0) {
		arg0.show(null, mScrollView);
	}
	
	@Override
	public void onLoopMeBannerShow(LoopMeBanner arg0) {
	}

	@Override
	public void onLoopMeBannerVideoDidReachEnd(LoopMeBanner arg0) {
	}
}