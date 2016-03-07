package com.loopme.banner_sample.app.listview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import com.loopme.LoopMeBanner;
import com.loopme.NativeVideoAdapter;
import com.loopme.NativeVideoBinder;
import com.loopme.common.LoopMeError;
import com.loopme.banner_sample.app.CustomListItem;
import com.loopme.banner_sample.app.R;

import java.util.ArrayList;
import java.util.List;

public class ListViewActivity extends AppCompatActivity implements LoopMeBanner.Listener {

    private ListView mListView;
    private List<CustomListItem> mList = new ArrayList<CustomListItem>();
    private NativeVideoAdapter mNativeVideoAdapter;
    private String mAppKey = LoopMeBanner.TEST_MPU_BANNER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.listview_layout);
        setTitle("ListView");

        mListView = (ListView) findViewById(R.id.listview);
        fillList();
        CustomAdapter adapter = new CustomAdapter(this, mList);

        //Init NativeVideoAdapter
        mNativeVideoAdapter = new NativeVideoAdapter(adapter, this, mListView);
        //define position for ad
        mNativeVideoAdapter.putAdWithAppKeyToPosition(mAppKey, 1);
        mNativeVideoAdapter.setAdListener(this);
        NativeVideoBinder binder = new NativeVideoBinder.Builder(R.layout.list_ad_row)
                .setLoopMeBannerViewId(R.id.lm_banner_view)
                .build();
        mNativeVideoAdapter.setViewBinder(binder);

        mListView.setAdapter(mNativeVideoAdapter);
        mNativeVideoAdapter.loadAds();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNativeVideoAdapter.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNativeVideoAdapter.onResume();
    }

    @Override
    public void onBackPressed() {
        mNativeVideoAdapter.destroy();
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
        Toast.makeText(getApplicationContext(), "LoadFail " + arg1.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoopMeBannerLoadSuccess(LoopMeBanner arg0) {
        Toast.makeText(getApplicationContext(), "LoadSuccess", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoopMeBannerShow(LoopMeBanner arg0) {
    }

    @Override
    public void onLoopMeBannerVideoDidReachEnd(LoopMeBanner arg0) {
    }
}
