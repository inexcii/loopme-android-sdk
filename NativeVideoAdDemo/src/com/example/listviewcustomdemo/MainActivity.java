package com.example.listviewcustomdemo;

import com.loopme.LoopMeNativeVideoAd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

	private LoopMeNativeVideoAd mVideoAd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mVideoAd = new LoopMeNativeVideoAd(MainActivity.this,	Constants.APP_KEY);
		
		Button load = (Button) findViewById(R.id.reloadBtn);
		load.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
					mVideoAd.load();
			}
		});
		
		Button btn = (Button) findViewById(R.id.openActivityBtn);
		btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, MainActivity2.class));
			}
		});
		
		Button scrollActBtn = (Button) findViewById(R.id.openActivityBtn2);
		scrollActBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, MainActivity3.class));
			}
		});
	}
}
