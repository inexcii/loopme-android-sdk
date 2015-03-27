package com.loopme.native_video_demo;

import com.example.listviewcustomdemo.R;
import com.loopme.LoopMeAdHolder;
import com.loopme.LoopMeBanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	private LoopMeBanner mBanner;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button btn = (Button) findViewById(R.id.listview_btn);
		btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, ListViewActivity.class));
			}
		});
		
		Button scrollActBtn = (Button) findViewById(R.id.scrollview_btn);
		scrollActBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, ScrollViewActivity.class));
			}
		});
	}
	
	@Override
	protected void onResume() {
		// Getting already initialized ad object or create new one with specified appKey
		mBanner = (LoopMeBanner)LoopMeAdHolder.getAd(LoopMeBanner.TEST_MPU_BANNER); 
		if (mBanner == null) {
			// Create banner object
			mBanner = new LoopMeBanner(MainActivity.this, LoopMeBanner.TEST_MPU_BANNER);
		}
		/*
		 * Preloading ad
		 * It is recommended triggering `loadAd` method in advance to be able to display instantly in your application. 
		 */	
		mBanner.load();
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		if (mBanner != null) {
			mBanner.destroy();
		}
		super.onDestroy();
	}
}
