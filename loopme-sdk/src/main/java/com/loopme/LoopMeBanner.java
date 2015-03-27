package com.loopme;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.widget.ListView;
import android.widget.ScrollView;

import com.loopme.AdView.WebviewState;
import com.loopme.Logging.LogLevel;

/**
 * The `LoopMeBanner` class provides facilities to display a custom size ads
 * during natural transition points in your application.
 *
 * It is recommended to implement `LoopMeBanner.Listener` to stay informed about ad state changes,
 * such as when an ad has been loaded or has failed to load its content, when video ad has been watched completely,
 * when an ad has been presented or dismissed from the screen, and when an ad has expired or received a tap.
 */
public class LoopMeBanner extends BaseAd {
	
	private static final String LOG_TAG = LoopMeBanner.class.getSimpleName();
	
	/**
	 * AppKey for test purposes
	 */
	public static final String TEST_MPU_BANNER = "test_mpu";
	
	private Listener mAdListener;
	
	private volatile LoopMeBannerView mBannerView;
	
	public interface Listener {
		
		void onLoopMeBannerLoadSuccess(LoopMeBanner banner);
		
		void onLoopMeBannerLoadFail(LoopMeBanner banner, int error);
		
		void onLoopMeBannerShow(LoopMeBanner banner);
		
		void onLoopMeBannerHide(LoopMeBanner banner);
		
		void onLoopMeBannerClicked(LoopMeBanner banner);
		
		void onLoopMeBannerLeaveApp(LoopMeBanner banner);
		
		void onLoopMeBannerVideoDidReachEnd(LoopMeBanner banner);
		
		void onLoopMeBannerExpired(LoopMeBanner banner);
	}
	
	/**
	 * Creates new `LoopMeBanner` object with the given appKey
	 * 
	 * @param context - activity context
	 * @param appKey - your app key
	 * 
	 * @throws IllegalArgumentException if any of parameters is null
	 */
	public LoopMeBanner(Context context, String appKey) {
		super(context, appKey);
		Logging.out(LOG_TAG, "Start creating banner with app key: " + appKey, LogLevel.INFO);

		mViewController = new ViewController(this);
		
		Utils.init(context);
        Logging.init(context);
	}
	
	private void ensureAdIsVisible() {
		if (mViewController != null) {
			mViewController.ensureAdIsVisible(mBannerView);
		}
	}
	
	@Override
	public void destroy() {
		mAdListener = null;
		destroyAdContainer();
		
		super.destroy();
	}
	
	private void destroyAdContainer() {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				if (mBannerView != null) {
					mBannerView.removeAllViews();// should be in UI thread
					mBannerView = null;
				}
			}
		});
	}
	
	/**
	 * Links (@link LoopMeBannerView) view to banner. 
	 * If ad doesn't linked to @link LoopMeBannerView, it can't be display. 
	 * 
	 * @param viewGroup - @link LoopMeBannerView (container for ad) where ad will be displayed.
	 */
	public void bindView(LoopMeBannerView viewGroup) {
		mBannerView = viewGroup;
	}
	
	/**
	 * Checks whether any view already binded to ad or not.
	 * 
	 * @return true - if binded,
	 * 		   false - otherwise.
	 */
	public boolean isViewBinded() {
		return mBannerView != null;
	}
	
	/**
	 * Pauses video ad
	 * Needs to be triggered on appropriate Activity life-cycle method "onPause()".
	 */
	public void pause() {
		if (mViewController != null && 
				mViewController.getCurrentViewState() != WebviewState.HIDDEN && 
				mViewController.getCurrentVideoState() == VideoState.PLAYING) {

			Logging.out(LOG_TAG, "pause Ad", LogLevel.DEBUG);
			mViewController.onAdHidden();
		}
	}
	
	/**
	 * Sets listener in order to receive notifications during the loading/displaying ad processes
	 */
	public void setListener(Listener listener) {
		mAdListener = listener;
	}
	
	/**
	 * Removes listener.
	 */
	public void removeListener() {
		mAdListener = null;
	}
	
	/**
	 * Resumes video ad inside non-scrollable content
	 * Needs to be triggered on appropriate Activity lifycicle method "onResume()".
	**/
	public void resume() {
		if (mViewController != null && 
				mViewController.getCurrentVideoState() == VideoState.PAUSED &&
				mViewController.isAdVisibleEnough(mBannerView)) {

			Logging.out(LOG_TAG, "resume Ad", LogLevel.DEBUG);
			mViewController.onAdAppear();
		} 
	}
	
	/**
	 * Shows banner. 
	 * This method intended to be used for displaying ad if your ad not inserted in scrollable content
	 * Otherwise see "showAdIfVisible()" methods 
	 *
	 * As a result you'll receive onLoopMeBannerShow() callback
	 */
	public void show() {
		if (mAdState == AdState.SHOWING) {
			ensureAdIsVisible();
			return;
		}
		if (isReady() && mBannerView != null) {
			Logging.out(LOG_TAG, "Banner did start showing ad", LogLevel.INFO);
			mAdState = AdState.SHOWING;
			stopExpirationTimer();
			
			if (mViewController.isVideoPresented() && 
					mViewController.getCurrentVideoState() == VideoState.READY) {
				mViewController.buildVideoAdView(mBannerView);
			} else {
				mViewController.buildStaticAdView(mBannerView);
			}

			if (mBannerView.getVisibility() != View.VISIBLE) {
				mBannerView.setVisibility(View.VISIBLE);
			}
			
			onLoopMeBannerShow(this);

			ensureAdIsVisible();
		}
	}
	
	/**
	 * Show video ad. This method intended to be used for displaying ad inside scrollable content.
	 * Calculates video ad visibility inside "listView" content to manage video playback
	 * For better user experience video is paused if it's out of viewport and resumes when it's in viewport 
	 * "out of viewport" means less then 50% of ad is visible on scrollable content, othervise it's "in viewport"
	 *  
	 * @param adapter - custom adapter which implements @link LoopMeAdapter interface.
	 * @param listview - listview in which native video ad is displayed.
	 */
	public void showAdIfItVisible(LoopMeAdapter adapter, ListView listview) {
		if (adapter == null || listview == null) {
			return;
		}
		boolean isAmongVisibleElements = false;
		int first = listview.getFirstVisiblePosition();
		int last = listview.getLastVisiblePosition();
		for (int i = first; i <= last; i++) {
			if (adapter.isAd(i)) {
				show();
				isAmongVisibleElements = true;
			}
		}
		if (!isAmongVisibleElements) {
			pause();
		}
	}
	
	/**
	 * Show video ad. This method intended to be used for displaying ad inside scrollable content.
	 * Calculates video ad visibility inside "scrollView" content to manage video playback
	 * For better user experience video is paused if it's out of viewport and resumes when it's in viewport 
	 * "out of viewport" means less then 50% of ad is visible on scrollable content, othervise it's "in viewport"
	 *  
	 * @param scrollview - scrollview in which native video ad is displayed.
	 */
	public void showAdIfItVisible(ScrollView scrollview) {
		if (checkVisibilityOnScreen(scrollview)) {
			show();
		} else {
			pause();
		}
	}
	
	/**
	 * Resumes video ad inside "ListView"
	 * Needs to be triggered on appropriate Activity lifycicle method "onResume()".
	 * 
	 * @param listview - listview in which native video ad is displayed
	 * @param adapter - custom adapter which implements LoopMeAdapter interface.
	 */
	public void resume(ListView listview, LoopMeAdapter adapter) {
		if (listview == null || adapter == null) {
			return;
		}
		int first = listview.getFirstVisiblePosition(); 
		int last = listview.getLastVisiblePosition();
		for (int i = first; i <= last; i++) {
			if (adapter.isAd(i)) {
				resume();
			}
		}
	}
	
	/**
	 * Resumes video ad inside "ScrollView"
	 * Needs to be triggered on appropriate Activity lifycicle method "onResume()".
	 * 
	 * @param scrollview - scrollview in which native video ad is displayed.
	 */
	public void resume(ScrollView scrollview) {
		if (checkVisibilityOnScreen(scrollview)) {
			resume();
		}
	}
	
	private boolean checkVisibilityOnScreen(ScrollView scrollview) {
		if (scrollview == null || mBannerView == null) {
			return false;
		}
		Rect scrollBounds = new Rect();
		scrollview.getHitRect(scrollBounds);
		if (mBannerView.getLocalVisibleRect(scrollBounds)) {
		    return true;
		} else {
		    return false;
		}
	}
	
	/**
	 * Dismisses an banner ad
	 * This method dismisses an banner ad and only if it is currently presented.
	 * 
	 * After it banner ad requires "loading process" to be ready for displaying
	 * 
	 * As a result you'll receive onLoopMeBannerHide() notification
	 */
	public void dismiss() {
		Logging.out(LOG_TAG, "Banner will be dismissed", LogLevel.DEBUG);
		if (mBannerView != null && mBannerView.getVisibility() == View.VISIBLE) {
			((Activity) getContext()).runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					if (mBannerView != null) {
						mBannerView.setVisibility(View.GONE);
						mBannerView.removeAllViews();// should be in UI thread
					}
					if (mViewController != null) {
						mViewController.onAdDisappear();
					}
				}
			});
			LoopMeAdHolder.removeAd(getAppKey());
			onLoopMeBannerHide(this);
		} else {
			Logging.out(LOG_TAG, "Can't dismiss ad, it's not displaying", LogLevel.DEBUG);
		}
	}
	
	@Override
	public AdFormat getAdFormat() {
		return AdFormat.BANNER;
	}
	
	/**
	 * Triggered when banner ad failed to load ad content
	 * 
	 * @param banner - banner object - the sender of message
	 * @param error - error of unsuccesful ad loading attempt
	 */
	void onLoopMeBannerLoadFail(LoopMeBanner banner, final int error) {
		Logging.out(LOG_TAG, "Ad fails to load: " + LoopMeError.getCodeMessage(error), LogLevel.INFO);
		mAdState = AdState.NONE;
		mIsReady = false;
		stopFetcherTimer();
		if(mAdListener != null) {
			mAdListener.onLoopMeBannerLoadFail(this, error);
		}
	}
	
	/**
     * Triggered when the banner has successfully loaded the ad content
     * 
     * @param banner - banner object the sender of message
     */
	void onLoopMeBannerSuccessLoad(LoopMeBanner banner) {
		Logging.out(LOG_TAG, "Ad successfully loaded ", LogLevel.INFO);
		mIsReady = true;
		mAdState = AdState.NONE;
		stopFetcherTimer();
		if(mAdListener != null) {
			mAdListener.onLoopMeBannerLoadSuccess(this);
		}
	}

	/**
	 * Triggered when the banner ad appears on the screen
	 * 
	 * @param banner - banner object the sender of message
	 */
	void onLoopMeBannerShow(LoopMeBanner banner) {
		Logging.out(LOG_TAG, "Ad appeared on screen", LogLevel.INFO);
		if(mAdListener != null) {
			mAdListener.onLoopMeBannerShow(this);
		}
	}

	/**
	 * Triggered when the banner ad disappears on the screen
	 * 
	 * @param banner - banner object the sender of message
	 */
	void onLoopMeBannerHide(LoopMeBanner banner) {
		Logging.out(LOG_TAG, "Ad disappeared from screen", LogLevel.INFO);
		mIsReady = false;
		mAdState = AdState.NONE;
		releaseViewController(false);
		if(mAdListener != null) {
			mAdListener.onLoopMeBannerHide(this);
		}
	}

	/**
	 * Triggered when the user taps the banner ad and the banner is about to perform extra actions
	 * Those actions may lead to displaying a modal browser or leaving your application.
	 * 
	 * @param banner - banner object the sender of message
	 */
	void onLoopMeBannerClicked(LoopMeBanner banner) {
		Logging.out(LOG_TAG, "Ad received click event", LogLevel.INFO);
		if(mAdListener != null) {
			mAdListener.onLoopMeBannerClicked(this);
		}
	}

	/**
	 * Triggered when your application is about to go to the background, initiated by the SDK.
	 * This may happen in various ways, f.e if user wants open the SDK's browser web page in native browser or clicks 
	 * on `mailto:` links...
	 * 
	 * @param banner - banner object the sender of message
	 */
	void onLoopMeBannerLeaveApp(LoopMeBanner banner) {
		Logging.out(LOG_TAG, "Leaving application", LogLevel.INFO);
		if(mAdListener != null) {
			mAdListener.onLoopMeBannerLeaveApp(LoopMeBanner.this);
		}
	}
	
	/**
	 * Triggered only when banner's video was played until the end.
	 * It won't be sent if the video was skipped or the banner was dissmissed during the displaying process
	 * 
	 * @param banner - banner object - the sender of message
	 */
	void onLoopMeBannerVideoDidReachEnd(LoopMeBanner banner) {
		Logging.out(LOG_TAG, "Video did reach end", LogLevel.INFO);
		if (mAdListener != null) {
			mAdListener.onLoopMeBannerVideoDidReachEnd(this);
		}
	}
	
	/**
	 * Triggered when the banner's loaded ad content is expired.
	 * Expiration happens when loaded ad content wasn't displayed during some period of time, approximately one hour.
	 * Once the banner is presented on the screen, the expiration is no longer tracked and banner won't 
	 * receive this message
	 * 
	 * @param banner - banner object the sender of message
	 */
	void onLoopMeBannerExpired(LoopMeBanner banner) {
		Logging.out(LOG_TAG, "Ad content is expired", LogLevel.INFO);
		mExpirationTimer = null;
		mIsReady = false;
		mAdState = AdState.NONE;
		releaseViewController(false);
		if(mAdListener != null){
			mAdListener.onLoopMeBannerExpired(this);
		}
	}
	
	@Override
	void onAdExpired() {
		onLoopMeBannerExpired(this);
	}

	@Override
	void onAdLoadSuccess() {
		onLoopMeBannerSuccessLoad(this);
	}

	@Override
	void onAdLoadFail(int error) {
		onLoopMeBannerLoadFail(this, error);
	}

	@Override
	void onAdLeaveApp() {
		onLoopMeBannerLeaveApp(this);
	}

	@Override
	void onAdClicked() {
		onLoopMeBannerClicked(this);
	}

	@Override
	void onAdVideoDidReachEnd() {
		onLoopMeBannerVideoDidReachEnd(this);
	}

	@Override
	int detectWidth() {
		android.view.ViewGroup.LayoutParams params = mBannerView.getLayoutParams();
		return params.width;
	}

	@Override
	int detectHeight() {
		android.view.ViewGroup.LayoutParams params = mBannerView.getLayoutParams();
		return params.height;
	}
}
