package com.loopme.banner_sample.app;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.loopme.LoopMeBanner;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	private LoopMeBanner mBanner;
	private List<String> mData = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		RecyclerView recyclerView = (RecyclerView) findViewById(R.id.main_view);
		recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
		initList();
		MainAdapter adapter = new MainAdapter(this, mData);
		recyclerView.setAdapter(adapter);
	}

	private void initList() {
		mData.add(Constants.SIMPLE);
		mData.add(Constants.LISTVIEW);
		mData.add(Constants.LISTVIEW_SHRINK);
		mData.add(Constants.SCROLLVIEW);
		mData.add(Constants.SCROLLVIEW_SHRINK);
		mData.add(Constants.RECYCLERVIEW);
		mData.add(Constants.RECYCLERVIEW_SHRINK);
	}
	
	@Override
	protected void onResume() {
		/*
		 * Getting already initialized ad object or create new one with specified appKey
		 */
		mBanner = LoopMeBanner.getInstance(LoopMeBanner.TEST_MPU_BANNER,
				this.getApplicationContext());
		/*
		 * Preloading ad
		 * It is recommended triggering `load` method in advance to be able to display instantly in your application. 
		 */	
		if (mBanner != null) {
			mBanner.load();
		}
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
