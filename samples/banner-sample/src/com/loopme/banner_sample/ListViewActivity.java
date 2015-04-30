package com.loopme.banner_sample;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListViewActivity extends Activity implements LoopMeBanner.Listener,
OnScrollListener {

	private LoopMeBanner mBanner;
	private CustomAdapter mCustomAdapter;
	private ListView mListView;
	
	private List<CustomListItem> mList = new ArrayList<CustomListItem>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.listview_layout);
		
		fillList();
		
		/*
		 * Getting already initialized ad object or create new one with specified appKey
		 */
		mBanner = LoopMeBanner.getInstance(LoopMeBanner.TEST_MPU_BANNER, this);
		
		/*
		 * Start loading immediately
		 */
		mBanner.load();
		
		/*
		 * Adding listener to receive SDK notifications
		 * during the loading/displaying ad processes 
		 */
		mBanner.setListener(this);
		
		mCustomAdapter = new CustomAdapter(mList);
		
		// Setting advertisement to be 1st item in list view
		mCustomAdapter.addBannerToPosition(0);		
		
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
			 * Resume actions inside banner (f.e. resume video playback)
			 */
			mBanner.showAdIfItVisible(mCustomAdapter, mListView);
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
	
	private void fillList() {
        mList.add(new CustomListItem("THE GREY (2012)", "Liam Neeson", R.drawable.poster1));
        mList.add(new CustomListItem("MAN OF STEEL (2013)", "Henry Cavill, Amy Adams", R.drawable.poster2));
        mList.add(new CustomListItem("AVATAR (2009)", "Sam Worthington", R.drawable.poster3));
        mList.add(new CustomListItem("SHERLOCK HOLMES (2009)", "Robert Downey, Jr.", R.drawable.poster4));
        mList.add(new CustomListItem("ALICE IN WONDERLAND (2010)", "Johnny Depp", R.drawable.poster5));
        mList.add(new CustomListItem("INCEPTION (2010)", "Leonardo DiCaprio", R.drawable.poster6));
        mList.add(new CustomListItem("THE SILENCE OF THE LAMBS (1991)", "Jodie Foster", R.drawable.poster7));
        mList.add(new CustomListItem("MR. POPPER'S PENGUINS (2011)", "Jim Carrey", R.drawable.poster8));
        mList.add(new CustomListItem("LAST EXORCISM (2010)", "Patrick Fabian", R.drawable.poster9));
	}
	
	private class CustomAdapter extends BaseAdapter implements LoopMeAdapter {
		
		static final int TYPE_DATA = 0;
		static final int TYPE_BANNER = 1;
		
		static final int TYPE_COUNT = 2;

		private LayoutInflater mInflater;
		private List<Object> mData = new ArrayList<Object>();
		
		private TreeSet mBannerSet = new TreeSet();
		
		public CustomAdapter(List data) {
			mData = data;
			mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		public void addBannerToPosition(int position) {
			if (position >= 0 && position < mData.size()) {
				mData.add(position, mBanner);
				mBannerSet.add(position);
				notifyDataSetChanged();
			}
		}
		
		@Override
        public int getItemViewType(int position) {
            if (mBannerSet.contains(position)) {
            	return TYPE_BANNER;
            }             	
			return TYPE_DATA;
        }
		
		@Override
		public boolean isAd(int position) {
			// Check whether current item ad or not
			return mBannerSet.contains(position);
		}
		
		@Override
        public int getViewTypeCount() {
            return TYPE_COUNT;
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
			View v = view;
			ViewHolder holder = null;
            int type = getItemViewType(position);
            
            if (v == null || v.getTag() == null) {
            	holder = new ViewHolder();
            	switch (type) {
            	case TYPE_DATA:
            		v = mInflater.inflate(R.layout.list_data_layout, null);
            		holder.title = (TextView) v.findViewById(R.id.title);
                    holder.subtitle = (TextView) v.findViewById(R.id.subtitle);
                    holder.icon = (ImageView) v.findViewById(R.id.icon);
            		break;
            		
            	case TYPE_BANNER:
            		/*
            		 * Inflate banner ad layout
            		 */
            		v = mInflater.inflate(R.layout.list_ad_layout, null);

            		/*
            		 * Find banner ad view in layout
            		 */
            		holder.banner_ad = (LoopMeBannerView) v.findViewById(R.id.banner_ad_spot);
            		break;
            	}
            	v.setTag(holder);
            } else {
            	holder = (ViewHolder) v.getTag();
            }
            
            if (holder.banner_ad != null) {
            	if (mBanner != null) {
            		/*
            		 * Binding banner view
            		 */
        			mBanner.bindView(holder.banner_ad);
        		}
            }
            if (holder.title != null) {
            	holder.title.setText(((CustomListItem)mData.get(position)).getTitle());
            }
            if (holder.subtitle != null) {
            	holder.subtitle.setText(((CustomListItem)mData.get(position)).getSubtitle());
            }
            if (holder.icon != null) {
            	holder.icon.setImageResource(((CustomListItem)mData.get(position)).getIconId());
            }
			return v;
		}
	}
	
	public static class ViewHolder {
		public TextView title;
        public TextView subtitle;
        public ImageView icon;
        
        public LoopMeBannerView banner_ad;
    }
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		
		if (mBanner != null) {
			/*
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
	public void onLoopMeBannerLoadFail(LoopMeBanner arg0, int arg1) {
		Toast.makeText(getApplicationContext(), LoopMeError.getCodeMessage(arg1), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onLoopMeBannerLoadSuccess(LoopMeBanner arg0) {
		
		/*
		 * Manages the ad visibility if no scrolling happened before 
		 */
		arg0.showAdIfItVisible(mCustomAdapter, mListView);
	}

	@Override
	public void onLoopMeBannerShow(LoopMeBanner arg0) {
	}

	@Override
	public void onLoopMeBannerVideoDidReachEnd(LoopMeBanner arg0) {
	}
}
