package com.example.listviewcustomdemo;

import java.util.ArrayList;
import java.util.TreeSet;

import com.loopme.LoopMeAdHolder;
import com.loopme.LoopMeAdapter;
import com.loopme.LoopMeError;
import com.loopme.LoopMeNativeVideoAd;

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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity2 extends Activity implements LoopMeNativeVideoAd.Listener,
OnScrollListener {

	private LoopMeNativeVideoAd mVideoAd;
	private CustomAdapter mCustomAdapter;
	private ListView mListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main_layout);

		if (LoopMeAdHolder.getAd(Constants.APP_KEY) != null) {
			mVideoAd = (LoopMeNativeVideoAd) LoopMeAdHolder.getAd(Constants.APP_KEY);
			mVideoAd.addListener(this);
		}
		
		mCustomAdapter = new CustomAdapter();
		
		mCustomAdapter.addImageItem("image1");
		mCustomAdapter.addItem("Hello");
		mCustomAdapter.addItem("Welcome");
		mCustomAdapter.addItem("to");
		mCustomAdapter.addItem("LoopMe");
		mCustomAdapter.addItem("Demo");
		mCustomAdapter.addImageItem("image2");
		mCustomAdapter.addVideoAd(Constants.APP_KEY);
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
		if (mVideoAd != null) {
			mVideoAd.pause();
		}
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		if (mVideoAd != null) {
			mVideoAd.resume(mListView, mCustomAdapter);
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
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
            int type = getItemViewType(position);
            
            if (convertView == null) {
            	holder = new ViewHolder();
            	switch (type) {
            	case TYPE_ITEM:
            		convertView = mInflater.inflate(R.layout.item1, null);
            		holder.textView = (TextView) convertView.findViewById(R.id.item1_textview);
            		break;
            		
            	case TYPE_SEPARATOR:
            		convertView = mInflater.inflate(R.layout.item2, null);
            		holder.view = (View) convertView.findViewById(R.id.item2_ok);
            		break;
            		
            	case TYPE_BANNER:
            		convertView = mInflater.inflate(R.layout.item3, null);
            		holder.videoAd = (RelativeLayout) convertView.findViewById(R.id.item3_banner);
            		break;
            	}
            	convertView.setTag(holder);
            } else {
            	holder = (ViewHolder) convertView.getTag();
            	return convertView;
            }
            
            if (holder.videoAd != null) {
            	if (mVideoAd != null) {
        			mVideoAd.bindView(holder.videoAd);
        		}
            }
            if (holder.textView != null) {
            	holder.textView.setText(mData.get(position));
            }
			return convertView;
		}
	}
	
	public static class ViewHolder {
        public TextView textView;
        public View view;
        public RelativeLayout videoAd;
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
		arg0.showAdIfItVisible(mCustomAdapter, mListView);
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

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		
		if (mVideoAd != null) {
			mVideoAd.showAdIfItVisible(mCustomAdapter, mListView);
		}
	}
	
}
