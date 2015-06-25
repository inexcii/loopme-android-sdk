package com.example.mopub_nativead;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;
import com.loopme.LoopMeBanner;
import com.loopme.LoopMeError;
import com.mopub.nativeads.*;

import java.util.ArrayList;
import java.util.List;

public class MyActivity extends Activity implements LoopMeBanner.Listener, AbsListView.OnScrollListener,
LoopMeEventNative.Listener {

    private static final String ADUNIT_ID = "2a3a831619954b529834f419687aaae9";

    private ListView mListView;
    private List mList = new ArrayList<Object>();
    private CustomBaseAdapter mBaseAdapter;
    private MoPubAdAdapter mAdAdapter;

    private LoopMeBanner mBanner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        fillList();

        mListView = (ListView) findViewById(R.id.listview);
        mListView.setOnScrollListener(this);

        mBaseAdapter = new CustomBaseAdapter(this, mList);

        ViewBinder viewBinder = new ViewBinder.Builder(R.layout.native_ad_layout)
                .iconImageId(R.id.icon)
                .titleId(R.id.title)
                .textId(R.id.subtitle)
                .build();

        MoPubNativeAdRenderer adRenderer = new MoPubNativeAdRenderer(viewBinder);
        MoPubNativeAdPositioning.MoPubServerPositioning adPositioning =
                MoPubNativeAdPositioning.serverPositioning();

        mAdAdapter = new MoPubAdAdapter(this, mBaseAdapter, adPositioning);
        mAdAdapter.registerAdRenderer(adRenderer);
        mListView.setAdapter(mAdAdapter);

        mAdAdapter.loadAds(ADUNIT_ID);
        LoopMeEventNative.addListener(this);
    }

    @Override
    protected void onPause() {
        if (mBanner != null) {
            mBanner.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mBanner != null) {
            mBanner.showAdIfItVisible(mBaseAdapter, mListView);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mBanner != null) {
            mBanner.dismiss();
            mBanner.destroy();
        }
        mAdAdapter.destroy();
        super.onDestroy();
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
        loopMeBanner.showAdIfItVisible(mBaseAdapter, mListView);
    }

    @Override
    public void onLoopMeBannerLoadFail(LoopMeBanner loopMeBanner, int i) {
        Toast.makeText(getApplicationContext(), LoopMeError.getCodeMessage(i), Toast.LENGTH_SHORT).show();
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
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        if (mBanner != null) {
            mBanner.showAdIfItVisible(mBaseAdapter, mListView);
        }
    }

    @Override
    public void onNativeAdFailed(String appKey, int position) {
        Log.d("LoopMeEventNative", "onNativeAdFailed");
        mBanner = LoopMeBanner.getInstance(appKey, this);
        if (mBanner != null) {
            mBanner.load();
            mBanner.setListener(this);
            mBaseAdapter.addBannerToPosition(position, mBanner);
        }
    }
}
