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

`loopme-android-sdk` is distributed as a source code and provides facilities to retrieve, display ads in your application.


If you have questions please contact us at support@loopmemedia.com.

## Features ##

* Full-screen image interstitials
* Full-screen rich media interstitials
* Preloaded video ads
* Banner ads
* In-app ad reward notifications, including video view completed

## Requirements ##

An appKey is required to use the `loopme-android-sdk`. The appKey uniquely identifies your app to the LoopMe ad network. (Example appKey: 7643ba4d53.) To get an appKey visit the **[LoopMe Dashboard](http://loopme.me/)**.

Requires `Android` 4.0 and up

## SDK Integration ##

* Download latest version of loopme-sdk. Note: after import `loopme-sdk` project update Java Build Path (replace `loopme-sdk/src` to `loopme-sdk/src/main/java`)
* Add dependency to loopme-sdk project
* Update `AndroidManifest.xml` with perfmissions:
```xml
//Required permissions
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

//Optional permissions
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.VIBRATE" />
```
and activities:
```xml
<activity android:name="com.loopme.AdActivity" 
            android:theme="@android:style/Theme.Translucent"
            android:configChanges="orientation|keyboardHidden|screenSize" 
            android:hardwareAccelerated="true"/>
<activity android:name="com.loopme.AdBrowserActivity" />
```
## Full screen interstitial ads ##
* Create `LoopMeInterstitial` instance and retrieve ads
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
	mInterstitial = new LoopMeInterstitial(this, YOUR_APPKEY);
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

<b>Note:</b> Integration instructions to display banner ads inside scrollable content.
`LoopMeSDK` doesn't override `ListView`/`GridView` adapter. 

* Create xml layout for ad

* If you use `ListView` or `GridView` in your custom adapter implement `LoopMeAdapter` interface. 

* Creating `LoopMeBanner` and retrieving ads

Video is rendered in `SurfaceView` dynamically, in order to avoid "flash screen" effect add zero-height `SurfaceView` in activity layout:
```xml
<SurfaceView 
	android:id="@+id/surf"
    android:layout_width="match_parent"
    android:layout_height="0dp"/>
```

```java
public class YourActivity extends Activity implements LoopMeBanner.Listener {
  private LoopMeBanner mVideoAd;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
  
	/**
	 * Initialize the LoopMeBanner
	 * using the unique appKey you received when registering your app via the LoopMe Dashboard.
	 * For test purposes you can use test appKeys constants defined in LoopMeBanner.java    
	*/
	mVideoAd = new LoopMeBanner(this, YOUR_APPKEY);
	mVideoAd.setListener(this);
	mVideoAd.load();
  }
}
```
* Bind view banner ad 

```java
/**
 * You need to bind view to banner before displaying ad
*/
LoopMeBannerView mView = (LoopMeBannerView) findViewById(R.id.banner_ad_spot);
mBanner.bindView(mView);
```

* Displaying banner ads

If you display ad inside scrollable content, subscribe to scroll notifications
```java
mListView.setOnScrollListener(this);
```
and trigger `showAdIfItVisible()` inside `onScroll()` as well as in `onLoopMeBannerLoadSuccess`.

It manages the ad visibility inside the scrollable content and automatically calculates the ad area visibility and pauses any activity currently happening inside the ad (whether it's a video or animations) if the ad is less than 50% visible, otherwise resumes
```java
@Override
public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	mVideoAd.showAdIfItVisible(mCustomAdapter, mListView);
}

@Override
 public void onLoopMeBannerLoadSuccess(LoopMeBanner arg0) {
  	arg0.showAdIfItVisible(mCustomAdapter, mListView);
 }
```

Trigger `LoopMeBanner`'s `pause()` and `resume()` method in appropriate activity lifecycle methods
It allows to pause/resume any actions currently happening inside banner ad (f.e to pause video playback)
```java
	@Override
	protected void onPause() {
		mVideoAd.pause();
		super.onPause();
	}
	

	@Override
	protected void onResume() {
		mVideoAd.resume(mListView, mCustomAdapter);
		super.onResume();
	}
```

* Destroy banner.

Trigger destroy() method to clean up resources when ad no need anymore. It can be done in Activity onDestroy() method.
```java
mVideoAd.destroy();
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
- `NativeVideoAdDemo` as an example of `LoopMeBanner` integration within `ListView` and `ScrollView`
- `InterstitialDemo` as an example of `LoopMeInterstitial` integration

## What's new ##
**v4.1.0**

* Improved ad loading process by caching video
* Ability to pass custom params as part of ad request
* Small bug fixes

## License ##

see [License](LICENSE.md)
