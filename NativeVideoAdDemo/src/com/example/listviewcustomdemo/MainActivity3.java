package com.example.listviewcustomdemo;

import com.loopme.LoopMeAdHolder;
import com.loopme.LoopMeError;
import com.loopme.LoopMeNativeVideoAd;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

public class MainActivity3 extends Activity implements LoopMeNativeVideoAd.Listener{
	
	private LoopMeNativeVideoAd mVideoAd;
	private ScrollView mScrollView;
	private RelativeLayout mAdSpace;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout3);
		
		if (LoopMeAdHolder.getAd(Constants.APP_KEY) != null) {
			mVideoAd = (LoopMeNativeVideoAd) LoopMeAdHolder.getAd(Constants.APP_KEY);
			mVideoAd.addListener(this);
		}
		
		mAdSpace = (RelativeLayout) findViewById(R.id.video_ad_spot);
		mScrollView = (ScrollView) findViewById(R.id.scrollview);
		mScrollView.getViewTreeObserver().addOnScrollChangedListener(
				new ViewTreeObserver.OnScrollChangedListener() {
			
			@Override
			public void onScrollChanged() {
//				if (mVideoAd == null) {
//					return;
//				}
//				if (checkVisibilityOnScreen()) {
//					mVideoAd.show();
//				} else {
//					mVideoAd.pause();
//				}
				if (mVideoAd != null) {
					mVideoAd.showAdIfItVisible(mScrollView);
				}
			}
		});
		if (mVideoAd != null) {
			mVideoAd.bindView(mAdSpace);
			mVideoAd.show();
		}
	}
	
//	private boolean checkVisibilityOnScreen() {
//		Rect scrollBounds = new Rect();
//		mScrollView.getHitRect(scrollBounds);
//		if (mAdSpace.getLocalVisibleRect(scrollBounds)) {
//		    return true;
//		} else {
//		    return false;
//		}
//	}
	
	@Override
	protected void onPause() {
		if (mVideoAd != null) {
			mVideoAd.pause();
		}
		super.onPause();
	}
	
	@Override
	protected void onResume() {
//		if (checkVisibilityOnScreen()) {
//			if (mVideoAd != null) {
//				mVideoAd.resume();
//			}
//		}
		if (mVideoAd != null) {
			mVideoAd.resume(mScrollView);
		}
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		if (mVideoAd != null) {
			mVideoAd.destroy();
		}
		super.onDestroy();
	}
	
	private void toast(String mess) {
		Toast.makeText(this, mess, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onLoopMeVideoAdClicked(LoopMeNativeVideoAd arg0) {
		toast("onLoopMeVideoAdClicked");
	}

	@Override
	public void onLoopMeVideoAdExpired(LoopMeNativeVideoAd arg0) {
		toast("onLoopMeVideoAdExpired");
	}

	@Override
	public void onLoopMeVideoAdHide(LoopMeNativeVideoAd arg0) {
		toast("onLoopMeVideoAdHide");
	}

	@Override
	public void onLoopMeVideoAdLeaveApp(LoopMeNativeVideoAd arg0) {
		toast("onLoopMeVideoAdLeaveApp");
	}

	@Override
	public void onLoopMeVideoAdLoadFail(LoopMeNativeVideoAd arg0,
			LoopMeError arg1) {
		toast("onLoopMeVideoAdLoadFail");
	}

	@Override
	public void onLoopMeVideoAdLoadSuccess(LoopMeNativeVideoAd arg0) {
		toast("onLoopMeVideoAdLoadSuccess");
		mAdSpace.setVisibility(View.VISIBLE);
		
		final ViewTreeObserver observer = mAdSpace.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
//				if (checkVisibilityOnScreen()) {
//					mVideoAd.show();
//				}
				if (mVideoAd != null) {
					mVideoAd.showAdIfItVisible(mScrollView);
				}
				observer.removeGlobalOnLayoutListener(this);
			}
		});
	}

	@Override
	public void onLoopMeVideoAdShow(LoopMeNativeVideoAd arg0) {
		toast("onLoopMeVideoAdShow");
	}

	@Override
	public void onLoopMeVideoAdVideoDidReachEnd(LoopMeNativeVideoAd arg0) {
		toast("onLoopMeVideoAdVideoDidReachEnd");
	}
}
