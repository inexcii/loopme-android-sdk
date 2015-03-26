package com.example.listviewcustomdemo;

import com.loopme.LoopMeError;
import com.loopme.LoopMeNativeVideoAd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements LoopMeNativeVideoAd.Listener {

	private LoopMeNativeVideoAd mVideoAd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mVideoAd = new LoopMeNativeVideoAd(MainActivity.this,	Constants.APP_KEY);
		mVideoAd.addListener(this);
		
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
	
	private void toast(String mess) {
		Toast.makeText(this, mess, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onLoopMeVideoAdClicked(LoopMeNativeVideoAd arg0) {
		toast("onLoopMeVideoAdClicked");
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
	public void onLoopMeVideoAdLoadFail(LoopMeNativeVideoAd arg0, LoopMeError arg1) {
		toast("onLoopMeVideoAdLoadFail");
	}

	@Override
	public void onLoopMeVideoAdLoadSuccess(LoopMeNativeVideoAd arg0) {
		toast("onLoopMeVideoAdLoadSuccess");
	}

	@Override
	public void onLoopMeVideoAdShow(LoopMeNativeVideoAd arg0) {
		toast("onLoopMeVideoAdShow");
	}

	@Override
	public void onLoopMeVideoAdVideoDidReachEnd(LoopMeNativeVideoAd arg0) {
		toast("onLoopMeVideoAdVideoDidReachEnd");
	}
	
	@Override
	public void onLoopMeVideoAdExpired(LoopMeNativeVideoAd arg0) {
		toast("onLoopMeVideoAdVideoDidReachEnd");
	}
}
