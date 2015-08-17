package com.loopme.banner_sample.app.scrollview;

import android.support.v7.app.AppCompatActivity;
import com.loopme.banner_sample.app.R;
import com.loopme.LoopMeBanner;
import com.loopme.LoopMeBannerView;
import com.loopme.LoopMeError;

import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;
import android.widget.Toast;

public class ScrollViewActivity extends AppCompatActivity implements LoopMeBanner.Listener{
	
	private LoopMeBanner mBanner;
	private ScrollView mScrollView;
	private LoopMeBannerView mAdSpace;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scrollview_layout);
		setTitle("ScrollView");
		
		mAdSpace = (LoopMeBannerView) findViewById(R.id.video_ad_spot);
		mScrollView = (ScrollView) findViewById(R.id.scrollview);
		
		/*
		 * Getting already initialized ad object or create new one with specified appKey
		 */
		mBanner = LoopMeBanner.getInstance(LoopMeBanner.TEST_MPU_BANNER, this);

		if (mBanner != null) {
			mBanner.setListener(this);
			mBanner.bindView(mAdSpace);
			mBanner.load();
		}
		
		mScrollView.getViewTreeObserver().addOnScrollChangedListener(
				new ViewTreeObserver.OnScrollChangedListener() {
			
			@Override
			public void onScrollChanged() {
				if (mBanner != null) {
					mBanner.show(null, mScrollView);
				}
			}
		});
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
			mBanner.show(null, mScrollView);
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
		Toast.makeText(getApplicationContext(), arg1.getMessage(), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onLoopMeBannerLoadSuccess(LoopMeBanner banner) {
		banner.show(null, mScrollView);
	}
	
	@Override
	public void onLoopMeBannerShow(LoopMeBanner arg0) {
	}

	@Override
	public void onLoopMeBannerVideoDidReachEnd(LoopMeBanner arg0) {
	}
}
