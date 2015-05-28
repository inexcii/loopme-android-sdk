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
	mInterstitial = LoopMeInterstitial.getInstance(YOUR_APPKEY, this);
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

* Update `AndroidManifest.xml`:
```xml
<activity android:name="ActivityWhereBannerLocated" android:hardwareAccelerated="true"/>
```

* Create xml layout for ad

* If you use `ListView` or `GridView` in your custom adapter implement `LoopMeAdapter` interface. 

* Creating `LoopMeBanner` and retrieving ads

```java
public class YourActivity extends Activity implements LoopMeBanner.Listener {
  private LoopMeBanner mBanner;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
  
	/**
	 * Initialize the LoopMeBanner
	 * using the unique appKey you received when registering your app via the LoopMe Dashboard.
	 * For test purposes you can use test appKeys constants defined in LoopMeBanner.java    
	*/
	mBanner = LoopMeBanner.getInstance(YOUR_APPKEY, this);

    /*
    * If you want to display minimized video when original ad is out of viewport:
    * you need to define `MinimzedMode` and pass it in `setMinimizedMode` method.
    * Note: you can change minimized mode size and margins.
    */
    MinimizedMode mode = new MinimizedMode(root); // root is parent layout where `MinimizedVideo` will be displayed
    mBanner.setMinimizedMode(mode);

	mBanner.setListener(this);
	mBanner.load();
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
	mBanner.showAdIfItVisible(mCustomAdapter, mListView);
}

@Override
 public void onLoopMeBannerLoadSuccess(LoopMeBanner arg0) {
  	arg0.showAdIfItVisible(mCustomAdapter, mListView);
 }
```

Trigger `LoopMeBanner`'s `pause()` and `showAdIfItVisible` method in appropriate activity lifecycle methods
It allows to pause/resume any actions currently happening inside banner ad (f.e to pause video playback)
```java
	@Override
	protected void onPause() {
		mBanner.pause();
		super.onPause();
	}
	

	@Override
	protected void onResume() {
		mBanner.showAdIfItVisible(mCustomAdapter, mListView);
		super.onResume();
	}
```

* Destroy banner.

Trigger destroy() method to clean up resources when ad no need anymore. It can be done in Activity onDestroy() method.
```java
mBanner.destroy();
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
- `banner-sample` as an example of `LoopMeBanner` integration within `ListView` and `ScrollView`
- `interstitial-sample` as an example of `LoopMeInterstitial` integration

## What's new ##
**Version 4.2.1 (06 of May, 2015)**

- Do not display ads if `Android` version under 4.0

**Version 4.2.0 (30 of April, 2015)**

- Displaying `minimized video` when original `banner` ad is out of viewport, with configurable size and margins.
- `Swipe-to-remove` for `minimized video`.
- `LoopMeAdHolder` class is deprecated, use `LoopMeBanner.getInstance(appkey, this);` which returns existing ad object or creates new one for a given appKey.
- `LoopMeBanner.resume()` method is deprecated, use `LoopMeBanner.showAdIfVisible`
- `SurfaceView` is replaced with `TextureView`, you don't have to create zero-height UI element in layout'

See [Changelog](CHANGELOG.md)

## License ##

See [License](LICENSE.md)
