package com.loopme.native_video_demo;

import java.util.ArrayList;
import java.util.TreeSet;

import com.example.listviewcustomdemo.R;
import com.loopme.LoopMeAdHolder;
import com.loopme.LoopMeAdapter;
import com.loopme.LoopMeBanner;
import com.loopme.LoopMeBannerView;
import com.loopme.LoopMeError;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListViewActivity extends Activity implements LoopMeBanner.Listener,
OnScrollListener {

	private LoopMeBanner mBanner;
	private CustomAdapter mCustomAdapter;
	private ListView mListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.listview_layout);
		
		// Getting already initialized ad object or create new one with specified appKey
		mBanner = (LoopMeBanner)LoopMeAdHolder.getAd(LoopMeBanner.TEST_MPU_BANNER); 
		// If we didn't create ad object before, create new one
		if (mBanner == null) {
			mBanner = new LoopMeBanner(this, LoopMeBanner.TEST_MPU_BANNER);
			// Start loading immediately
			mBanner.load();
		}	
		/*
		 * Adding listener to receive SDK notifications
		 * during the loading/displaying ad processes 
		 */
		mBanner.setListener(this);
			
		/*
		 * Fill list items
		 */		
		mCustomAdapter = new CustomAdapter();
		mCustomAdapter.addImageItem("image1");
		mCustomAdapter.addItem("Hello");
		// Setting advertisement to be 3rd item in list view
		mCustomAdapter.addVideoAd("Ad");		
		mCustomAdapter.addItem("Welcome");
		mCustomAdapter.addItem("to");
		mCustomAdapter.addItem("LoopMe");
		mCustomAdapter.addItem("Demo");
		mCustomAdapter.addImageItem("image1");
		mCustomAdapter.addItem("Hello");
		mCustomAdapter.addItem("Welcome");
		mCustomAdapter.addItem("to");
		mCustomAdapter.addItem("LoopMe");
		mCustomAdapter.addItem("Demo");
		mCustomAdapter.addImageItem("image2");
		
		mListView = (ListView) findViewById(R.id.listview);
		mListView.setAdapter(mCustomAdapter);
		mListView.setOnScrollListener(this);
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
			mBanner.resume(mListView, mCustomAdapter);
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
	
	private class CustomAdapter extends BaseAdapter implements LoopMeAdapter {
		
		static final int TYPE_ITEM = 0;
		static final int TYPE_SEPARATOR = 1;
		static final int TYPE_BANNER = 2;
		
		static final int TYPE_MAX_COUNT = 3;

		private LayoutInflater mInflater;
		private ArrayList<String> mData = new ArrayList<String>();
		
		private TreeSet mImageSeparatorSet = new TreeSet();
		private TreeSet mBannerSet = new TreeSet();
		
		public CustomAdapter() {
			mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		public void addItem(final String item) {
            mData.add(item);
            notifyDataSetChanged();
        }
		
		public void addImageItem(final String item) {
            mData.add(item);
            mImageSeparatorSet.add(mData.size() - 1);
            notifyDataSetChanged();
        }
		
		public void addVideoAd(final String item) {
			mData.add(item);
            mBannerSet.add(mData.size() - 1);
            notifyDataSetChanged();
		}
		
		@Override
        public int getItemViewType(int position) {
            if (mImageSeparatorSet.contains(position)) {
            	return TYPE_SEPARATOR;
            } 
            if (mBannerSet.contains(position)) {
            	return TYPE_BANNER;
            }             	
			return TYPE_ITEM;
        }
		
		@Override
		public boolean isAd(int position) {
			// Check whether current item ad or not
			return mBannerSet.contains(position);
		}
		
		@Override
        public int getViewTypeCount() {
            return TYPE_MAX_COUNT;
        }
		
		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Object getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			ViewHolder holder = null;
            int type = getItemViewType(position);
            
            if (view == null) {
            	holder = new ViewHolder();
            	switch (type) {
            	case TYPE_ITEM:
            		view = mInflater.inflate(R.layout.list_item1, null);
            		holder.textView = (TextView) view.findViewById(R.id.item1_textview);
            		break;
            		
            	case TYPE_SEPARATOR:
            		view = mInflater.inflate(R.layout.list_item2, null);
            		holder.view = (View) view.findViewById(R.id.item2_ok);
            		break;
            		
            	case TYPE_BANNER:
            		// Inflate banner ad layout
            		view = mInflater.inflate(R.layout.list_item3, null);
            		// Find banner ad view in layout
            		holder.videoAd = (LoopMeBannerView) view.findViewById(R.id.item3_banner);
            		break;
            	}
            	view.setTag(holder);
            } else {
            	holder = (ViewHolder) view.getTag();
            	return view;
            }
            
            if (holder.videoAd != null) {
            	if (mBanner != null) {
            		// Binding banner view
        			mBanner.bindView(holder.videoAd);
        		}
            }
            if (holder.textView != null) {
            	holder.textView.setText(mData.get(position));
            }
			return view;
		}
	}
	
	public static class ViewHolder {
        public TextView textView;
        public View view;
        public LoopMeBannerView videoAd;
    }
	
	private void toast(String mess) {
		Toast.makeText(this, mess, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		
		if (mBanner != null) {
			/**
			 * Manages the ad visibility inside the listView during scrolling
			 * It automatically calculates the ad area visibility
			 * and pauses any actions currently happening inside the banner ad 
			 * (whether it's a video or animations) if the ad is less than 50% visible, otherwise resumes
			 */
			mBanner.showAdIfItVisible(mCustomAdapter, mListView);
		}
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
		/**
		 * Manages the ad visibility if no scrolling happened before 
		 */
		arg0.showAdIfItVisible(mCustomAdapter, mListView);
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
