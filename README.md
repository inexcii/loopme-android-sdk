# LoopMe-Android-SDK #

[点击阅读SDK对接中文说明](README_CHINESE.md)

1. **[Overview](#overview)**
2. **[Features](#features)**
3. **[Requirements](#requirements)**
4. **[SDK Integration](#sdk-integration)**
  * **[Full screen interstitial ads](#full-screen-interstitial-ads)**
  * **[Banner ads](#banner-ads)**
5. **[Sample projects](#sample-projects)**
6. **[What's new](#whats-new)**

## Overview ##

LoopMe is the largest mobile video DSP and Ad Network, reaching over 1 billion consumers world-wide. LoopMe’s full-screen video and rich media ad formats deliver more engaging mobile advertising experiences to consumers on smartphones and tablets.

If you have questions please contact us at support@loopmemedia.com.

## Features ##

* Full-screen image interstitials
* Full-screen rich media interstitials
* Preloaded video ads
* Banner ads
* Minimized video mode
* Expand to fullscreen mode
* In-app ad reward notifications, including video view completed

## Requirements ##

An appKey is required to use the `loopme-android-sdk`. The appKey uniquely identifies your app to the LoopMe ad network. (Example appKey: 7643ba4d53.) To get an appKey visit the **[LoopMe Dashboard](http://loopme.me/)**.

Requires `Android` 4.0 and up

## SDK Integration ##

* Download latest version of loopme-sdk
* Add dependency to loopme-sdk project
* Update `AndroidManifest.xml` with perfmissions:
```xml
//Required permissions
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>

//Optional permissions
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.GET_ACCOUNTS"/>
<uses-permission android:name="android.permission.READ_PHONE_STATE"/>
```
activities and receiver:
```xml
<activity android:name="com.loopme.AdActivity" 
            android:theme="@android:style/Theme.Translucent"
            android:configChanges="orientation|keyboardHidden|screenSize" 
            android:hardwareAccelerated="true"/>
<activity android:name="com.loopme.AdBrowserActivity" />
<receiver android:name="com.loopme.data.LoopMeReceiver"/>
```
## Full screen interstitial ads ##
* Create `LoopMeInterstitial` instance and retrieve ads. For test purposes use `LoopMeInterstitial.TEST_PORT_INTERSTITIAL` and `LoopMeInterstitial.TEST_LAND_INTERSTITIAL` app keys.
```java
public class YourActivity extends Activity implements LoopMeInterstitial.Listener {
  
  private LoopMeInterstitial mInterstitial;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	/**
	 * Initialize the interstitial ad
 	 * using the unique appKey you received when registering your app via the LoopMe Dashboard.
 	 * For test purposes you can use test appKeys constants defined in LoopMeInterstitial.java
 	*/
	mInterstitial = LoopMeInterstitial.getInstance(YOUR_APPKEY, getApplicationContext());
	mInterstitial.setListener(this);
	
	/**
 	 * Starts loading ad content process.
 	 * It is recommended triggering it in advance to have interstitial ad ready 
 	 * and to be able to display instantly in your application.
 	*/
	mInterstitial.load();
  }
}
```

* Display interstitial ads

Displaying the `LoopMeInterstitial` can be user-initiated (e.g press on button) or publisher-initiated (e.g. end of game level)
```java
  mInterstitial.show();
```

* Destroy interstitial

Destroy when it is no longer needed to clean up resources
```java
  mInterstitial.destroy();
```

* Interstitial notifications:

Implement `LoopMeInterstitial.Listener` in order to receive notifications during the loading/displaying ad processes, that you can use to trigger subsequent in-app events:
 * `-onLoopMeInterstitialLoadSuccess`: triggered when interstitial has been loaded the ad content
 * `-onLoopMeInterstitialLoadFail`: triggered when interstitial failed to load the ad content
 * `-onLoopMeInterstitialShow`: triggered when interstitial ad appeared on the screen
 * `-onLoopMeInterstitialHide`: triggered when interstitial ad disappeared from the screen
 * `-onLoopMeInterstitialVideoDidReachEnd`: triggered when interstitial video ad has been completely watched
 * `-onLoopMeInterstitialClicked`: triggered when interstitial ad was clicked
 * `-onLoopMeInterstitialExpired`: triggered when interstitial ad is expired, it is recommended to re-load

## Banner ads ##

`LoopMeBanner` class provides facilities to display a custom size ads during natural transition points in your application.

* Update `AndroidManifest.xml`:
```xml
<activity android:name="ActivityWhereBannerLocated" android:hardwareAccelerated="true"/>
```

### Banner inside ListView/RecyclerView ###

* Create xml layout for ad.
```xml  
list_ad_row.xml

<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/container"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">

    <com.loopme.LoopMeBannerView
        android:id="@+id/lm_banner_view"
        android:layout_width="300dp"
        android:layout_height="250dp"
        android:layout_centerHorizontal="true"/>
</RelativeLayout>
```

* Init `NativeVideoAdapter` (For test purposes you can use test app key constant defined in LoopMeBanner.java). 

```java
public class YourActivity extends Activity implements LoopMeBanner.Listener {
  
  private ListView mListView;
  private NativeVideoAdapter mNativeVideoAdapter;
  private String mAppKey = YOUR_APPKEY;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
  
  	//...
  	
	YourCustomAdapter adapter = new YourCustomAdapter(this, mList);

        //Init LoopMe adapter
        mNativeVideoAdapter = new NativeVideoAdapter(adapter, this, mListView);
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
}
```
Ad will be shown automaticly after load complete. 

### Banner in non-scrollable content ###

* Add `LoopMeBannerView` in layout xml
* Init `LoopMeBanner`
```java
public class SimpleBannerActivity extends AppCompatActivity implements LoopMeBanner.Listener {

    private LoopMeBanner mBanner;
    private LoopMeBannerView mAdSpace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //some code

        mAdSpace = (LoopMeBannerView) findViewById(R.id.video_ad_spot);

        mBanner = LoopMeBanner.getInstance(YOUR_APPKEY, this);
        mBanner.setListener(this);
        mBanner.bindView(mAdSpace);
        mBanner.load();
    }

    @Override
    protected void onPause() {
        mBanner.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mBanner.resume();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        mBanner.destroy();
        super.onBackPressed();
    }
}
```
* Display banner
```java
mBanner.show();
```

* `LoopMeBanner` notifications:

Implement `LoopMeBanner.Listener` in order to receive notifications during the loading/displaying ad processes, that you can use to trigger subsequent in-app events:
 * `-onLoopMeBannerLoadSuccess`: triggered when banner has been loaded
 * `-onLoopMeBannerLoadFail`: triggered when banner failed to load the ad content
 * `-onLoopMeBannerShow`: triggered when banner appeared on the screen
 * `-onLoopMeBannerHide`: triggered when banner disappeared from the screen
 * `-onLoopMeBannerVideoDidReachEnd`: triggered when video in banner has been completely watched
 * `-onLoopMeBannerClicked`: triggered when banner was clicked
 * `-onLoopMeBannerExpired`: triggered when banner is expired, it is recommended to re-load
 * `-onLoopMeBannerLeaveApp`: triggered if SDK initiated app switching. E.g after click on ad user is redirected to market (or any other native app)

## Sample projects ##

Check out our project samples:
- `banner-sample` as an example of `LoopMeBanner` integration within `ListView` and `RecyclerView`
- `interstitial-sample` as an example of `LoopMeInterstitial` integration

## What's new ##

**Version 4.8.0**
- Preload 25%

See [Changelog](CHANGELOG.md)

## License ##

See [License](LICENSE.md)
