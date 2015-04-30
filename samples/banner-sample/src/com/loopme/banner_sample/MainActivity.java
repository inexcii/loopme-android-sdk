package com.loopme.banner_sample;

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
		
		Button simpleBannerBtn = (Button) findViewById(R.id.simplebanner_btn);
		simpleBannerBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, SimpleBannerActivity.class));
			}
		});
		
		Button listviewWithShrinkModeBtn = (Button) findViewById(R.id.shrink_mode_btn);
		listviewWithShrinkModeBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, ListViewShrinkModeActivity.class));
			}
		});
		
		Button scrollviewWithShrinkModeBtn = (Button) findViewById(R.id.shrink_mode_scrollview_btn);
		scrollviewWithShrinkModeBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, ScrollViewShrinkModeActivity.class));
			}
		});

        Button listviewWithShrinkModLastPosBtn = (Button) findViewById(R.id.shrink_mode_btn_last_position);
        listviewWithShrinkModLastPosBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ListViewShrinkModeActivity2.class));
            }
        });
	}
	
	@Override
	protected void onResume() {
		/*
		 * Getting already initialized ad object or create new one with specified appKey
		 */
		mBanner = LoopMeBanner.getInstance(LoopMeBanner.TEST_MPU_BANNER, this);
		/*
		 * Preloading ad
		 * It is recommended triggering `load` method in advance to be able to display instantly in your application. 
		 */	
		mBanner.load();
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		if (mBanner != null) {
            /*
             * Clear ad resources
             */
			mBanner.destroy();
		}
		super.onDestroy();
	}
}
