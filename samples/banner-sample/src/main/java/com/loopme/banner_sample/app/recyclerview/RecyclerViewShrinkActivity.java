package com.loopme.banner_sample.app.recyclerview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.RelativeLayout;
import com.loopme.LoopMeError;
import com.loopme.banner_sample.app.CustomListItem;
import com.loopme.banner_sample.app.R;
import com.loopme.LoopMeBanner;
import com.loopme.MinimizedMode;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewShrinkActivity extends AppCompatActivity implements
        LoopMeBanner.Listener, CustomRecyclerViewAdapter.BindViewListener {

    private RecyclerView mRecyclerView;
    private CustomRecyclerViewAdapter mAdapter;
    private List<CustomListItem> mList = new ArrayList<CustomListItem>();

    private LoopMeBanner mBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view_layout);
        setTitle("RecyclerView with Shrink mode");

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        fillList();

        mAdapter = new CustomRecyclerViewAdapter(mList, this);
        mRecyclerView.setAdapter(mAdapter);

        mBanner = LoopMeBanner.getInstance(LoopMeBanner.TEST_MPU_BANNER, getApplicationContext());
        if (mBanner != null) {
            mBanner.setListener(this);
            mBanner.load();

            /**
             * Minimized mode
             */
            RelativeLayout root = (RelativeLayout) findViewById(R.id.root_view);
            MinimizedMode mode = new MinimizedMode(root);
            mBanner.setMinimizedMode(mode);
        }

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (mBanner != null) {
                    mBanner.show(mAdapter, mRecyclerView);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mBanner != null) {
            mBanner.destroy();
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBanner != null) {
            mBanner.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBanner != null) {
            mBanner.show(mAdapter, mRecyclerView);
        }
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
        mAdapter.addBannerToPosition(1, mBanner);
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

    @Override
    public void onViewBinded() {
        if (mBanner != null) {
            mBanner.show(mAdapter, mRecyclerView);
        }
    }
}