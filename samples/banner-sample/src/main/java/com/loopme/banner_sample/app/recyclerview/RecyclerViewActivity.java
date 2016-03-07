package com.loopme.banner_sample.app.recyclerview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.loopme.LoopMeBanner;
import com.loopme.NativeVideoBinder;
import com.loopme.NativeVideoRecyclerAdapter;
import com.loopme.common.LoopMeError;
import com.loopme.banner_sample.app.CustomListItem;
import com.loopme.banner_sample.app.R;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewActivity extends AppCompatActivity implements
        LoopMeBanner.Listener {

    private RecyclerView mRecyclerView;
    private List<CustomListItem> mList = new ArrayList<>();
    private NativeVideoRecyclerAdapter mNativeVideoRecyclerAdapter;
    private String mAppKey = LoopMeBanner.TEST_MPU_BANNER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view_layout);
        setTitle("RecyclerView");

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        fillList();
        CustomRecyclerViewAdapter adapter = new CustomRecyclerViewAdapter(this, mList);

        mNativeVideoRecyclerAdapter = new NativeVideoRecyclerAdapter(adapter, this, mRecyclerView);
        mNativeVideoRecyclerAdapter.putAdWithAppKeyToPosition(mAppKey, 1);
        NativeVideoBinder binder = new NativeVideoBinder.Builder(R.layout.ad_row)
                .setLoopMeBannerViewId(R.id.lm_banner_view)
                .build();
        mNativeVideoRecyclerAdapter.setViewBinder(binder);

        mRecyclerView.setAdapter(mNativeVideoRecyclerAdapter);
        mNativeVideoRecyclerAdapter.loadAds();
    }

    @Override
    public void onBackPressed() {
        mNativeVideoRecyclerAdapter.destroy();
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNativeVideoRecyclerAdapter.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNativeVideoRecyclerAdapter.onResume();
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
        mList.add(new CustomListItem("BRAVE (2012)", "Kelly Macdonald", R.drawable.poster10));
        mList.add(new CustomListItem("THOR (2011)", "Chris Hemsworth", R.drawable.poster11));
        mList.add(new CustomListItem("PAIN & GAIN (2013)", "Mark Wahlberg", R.drawable.poster12));
        mList.add(new CustomListItem("127 HOURS (2010)", "James Franco", R.drawable.poster13));
        mList.add(new CustomListItem("PAUL (2011)", "Seth Rogens", R.drawable.poster14));
    }

    @Override
    public void onLoopMeBannerLoadSuccess(LoopMeBanner loopMeBanner) {
    }

    @Override
    public void onLoopMeBannerLoadFail(LoopMeBanner loopMeBanner, LoopMeError i) {

    }

    @Override
    public void onLoopMeBannerShow(LoopMeBanner loopMeBanner) {

    }

    @Override
    public void onLoopMeBannerHide(LoopMeBanner loopMeBanner) {

    }

    @Override
    public void onLoopMeBannerClicked(LoopMeBanner loopMeBanner) {

    }

    @Override
    public void onLoopMeBannerLeaveApp(LoopMeBanner loopMeBanner) {

    }

    @Override
    public void onLoopMeBannerVideoDidReachEnd(LoopMeBanner loopMeBanner) {

    }

    @Override
    public void onLoopMeBannerExpired(LoopMeBanner loopMeBanner) {

    }
}

