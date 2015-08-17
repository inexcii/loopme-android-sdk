package com.loopme;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import com.loopme.Logging.LogLevel;
import com.loopme.utilites.Drawables;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

/**
 * Browser Activity. Starts when ad click happened. 
 */
public final class AdBrowserActivity extends Activity {
	
	private static final String LOG_TAG = AdBrowserActivity.class.getSimpleName();
	
	private static final String KEY_APPKEY = "appkey";
	private static final String KEY_FORMAT = "format";
	private static final String KEY_URL = "url";
	
	private BrowserWebView mAdBrowserWebview;
	private AdBrowserLayout mLayout;
    
    private BaseAd mBaseAd;
    private boolean mIsBackFromMarket = false;
    
    private View mProgress;
    private Button mBackButton;
    
    private String mUrl;
    
    private AdBrowserWebViewClient.Listener mWebClientListener;

    @Override
    public final void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		if (isValidExtras()) {

			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);

			mLayout = new AdBrowserLayout(this.getApplicationContext());
			setContentView(mLayout);

			mProgress = mLayout.getProgressBar();
			mBackButton = mLayout.getBackButton();

			mAdBrowserWebview = mLayout.getWebView();
			initWebView(mAdBrowserWebview);

			if (bundle != null) {
				mAdBrowserWebview.restoreState(bundle);
			} else {
				mAdBrowserWebview.loadUrl(mUrl);
			}

			initButtonListeners(mAdBrowserWebview);
		} else {
			finish();
		}
    }
    
    private boolean isValidExtras() {
    	String appKey = getIntent().getStringExtra(KEY_APPKEY);
    	String format = getIntent().getStringExtra(KEY_FORMAT);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mUrl = extras.getString(KEY_URL);
		}
		if (format.equalsIgnoreCase("banner")) {
			mBaseAd = LoopMeAdHolder.getBanner(appKey, null);
		} else {
			mBaseAd = LoopMeAdHolder.getInterstitial(appKey, null);
		}
		return (mBaseAd != null) && !TextUtils.isEmpty(mUrl);
    }
    
	@Override
	protected final void onPause() {
		super.onPause();
		Logging.out(LOG_TAG, "onPause", LogLevel.DEBUG);
		if (mAdBrowserWebview != null) {
			mAdBrowserWebview.onPause();
		}
	}

	@Override
	protected void onDestroy() {
		Logging.out(LOG_TAG, " onDestroy", LogLevel.DEBUG);
		if (mAdBrowserWebview != null) {
			mAdBrowserWebview.clearCache(true);
		}
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Logging.out(LOG_TAG, "onResume", LogLevel.DEBUG);
		if (mIsBackFromMarket) {
			finish();
		}
		mIsBackFromMarket = true;
		mLayout.getProgressBar().setVisibility(View.INVISIBLE);
    }

    private void initWebView(BrowserWebView webView) {
    	mWebClientListener = initAdBrowserClientListener();
    	AdBrowserWebViewClient client = new AdBrowserWebViewClient(mWebClientListener);
		webView.setWebViewClient(client);
		webView.getSettings().setBuiltInZoomControls(false);
    }
    
    private AdBrowserWebViewClient.Listener initAdBrowserClientListener() {
    	return new AdBrowserWebViewClient.Listener() {
			
			@Override
			public void onReceiveError() {
				finish();
			}
			
			@Override
			public void onPageStarted() {
				mProgress.setVisibility(View.VISIBLE);
			}
			
			@Override
			@SuppressLint("NewApi")
			public void onPageFinished(boolean canGoBack) {
				mProgress.setVisibility(View.INVISIBLE);
				if (canGoBack) {
					setImage(mBackButton, Drawables.BTN_BACK_ACTIVE);
				} else {
					setImage(mBackButton, Drawables.BTN_BACK_INACTIVE);
				}
			}
			
			@Override
			public void onLeaveApp() {
				mBaseAd.onAdLeaveApp();
			}
		};
    }

	@SuppressLint("NewApi")
	private void setImage(Button button, Drawables image) {
		if (Build.VERSION.SDK_INT < 16) {
			button.setBackgroundDrawable(image.decodeImage());
		} else {
			button.setBackground(image.decodeImage());
		}
	}
    	
    private void initButtonListeners(final WebView webView){

	    mLayout.getBackButton().setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (webView.canGoBack()) {
					mLayout.getProgressBar().setVisibility(View.VISIBLE);
					webView.goBack();
				}
			}
		});
        
	    mLayout.getCloseButton().setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
    	    }
		});
        
	    mLayout.getRefreshButton().setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				mLayout.getProgressBar().setVisibility(View.VISIBLE);
				webView.reload();
    	    }
		});
        
	    mLayout.getNativeButton().setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				String uriString = webView.getUrl();
				if (uriString != null) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriString));

					boolean isActivityResolved = getPackageManager()
							.resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY) != null
							? true : false;
					if (isActivityResolved) {
						startActivity(browserIntent);
						mBaseAd.onAdLeaveApp();
					}
				}
    	    }
		});
    }

	@Override
    public final boolean onKeyDown(int keyCode, KeyEvent event) {
		
    	 if(keyCode == KeyEvent.KEYCODE_BACK) {
    		if (mAdBrowserWebview.canGoBack()){
         		mAdBrowserWebview.goBack();
         		return true;
    		}
    		finish();
			return true;
    	 }
    	 return super.onKeyDown(keyCode, event);
    }
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		mIsBackFromMarket = false;
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		mAdBrowserWebview.saveState(outState);
		super.onSaveInstanceState(outState);
	}
}
