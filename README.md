# LoopMe-Android-SDK #

[点击阅读SDK对接中文说明](README_CHINESE.md)

1. **[Overview](#overview)**
2. **[Features](#features)**
3. **[Requirements](#requirements)**
4. **[SDK Integration](#sdk-integration)**
  * **[Full screen interstitial ads](#full-screen-interstitial-ads)**
  * **[Native video ads](#native-video-ads)**
  * **[Banner ads](#banner-ads)**
5. **[Sample projects](#sample-projects)**
6. **[What's new](#whats-new)**

## Overview ##

LoopMe is the largest mobile video DSP and Ad Network, reaching over 1 billion consumers world-wide. LoopMe’s full-screen video and rich media ad formats deliver more engaging mobile advertising experiences to consumers on smartphones and tablets.

`loopme-android-sdk` is distributed as a `jar` file and provides facilities to retrieve, display ads in your application.

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

* Download latest version `loopme-android-sdk`
* Copy the `loopme-sdk-x.x.x.jar` to `libs` folder of your project
* Make sure `android-support-v4.jar` is also added to `libs` folder, otherwise add it
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
* Declare activities in `manifest.xml`
```xml
<activity android:name="com.loopme.AdActivity" android:configChanges="orientation|keyboardHidden|screenSize"
android:hardwareAccelerated="true"/>
<activity android:name="com.loopme.AdBrowserActivity" />
<activity android:name="com.loopme.PlayerActivity" android:configChanges="orientation|keyboardHidden|screenSize"
android:hardwareAccelerated="true"/>
```
## Full screen interstitial ads ##

The `LoopMenterstitial` class provides the facilities to display a full-screen ad during natural transition points in your application.

* Create `LoopMeInterstitial` instance and retrieve ads
```java
public class YourActivity extends Activity implements LoopMeInterstitial.Listener {
  
  private LoopMeInterstitial mInterstitial;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	/* ... */
	mInterstitial = new LoopMeInterstitial(this, YOUR_APPKEY); // YOUR_APPKEY received during registering your app via the LoopMe Dashboard
	mInterstitial.addListener(this);
	mInterstitial.load(); // Use this method to preload the interstitial for display (we also recommend to trigger it in -interstitialDidDisappear delegate method to keep content up-to-date)
  }
}
```

* Display interstitial ads

Displaying the `LoopMe` interstitial can be user-initiated (e.g press on button) or publisher-initiated (e.g. end of game level)
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

## Native video ads ##

`LoopMeNativeVideoAd` represents video ad inside small container aims to be inserted inside scrollable content.
`LoopMeSDK` doesn't override `ListView`/`GridView` adapter. 
Check out `NativeVideoAdDemo` as an example of `NativeVideoAd` within `ScrollView` and `ListView`.

* Create xml layout for ad

* If you use `ListView` or `GridView` in your custom adapter implement `LoopMeAdapter` interface. 

* Creating `LoopMeNativeVideoAd` and retrieving ads

Video is rendered in `SurfaceView` dynamically, in order to avoid "flash screen" effect add zero-height `SurfaceView` in activity layout:
```xml
<SurfaceView 
	android:id="@+id/surf"
    android:layout_width="match_parent"
    android:layout_height="0dp"/>
```

```java
public class YourActivity extends Activity implements LoopMeNativeVideoAd.Listener {
  private LoopMeNativeVideoAd mVideoAd;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
  
	/* ... */
	mVideoAd = new LoopMeNativeVideoAd(this, YOUR_APPKEY);
	mVideoAd.addListener(this);
	mVideoAd.load(); // Use this method to preload the ad for display (we also recommend to trigger it in `onLoopMeVideoAdHide` callback method to run next preloading ad process
  }
}
```

* Displaying video ads

If you display ad inside scrollable content, subscribe to scroll notifications
```java
mListView.setOnScrollListener(this);
```
and trigger `showAdIfItVisible()` inside `onScroll()` as well in `onLoopMeVideoAdLoadSuccess`, this provides ability for SDK for instant displaying ads as soon as it's loaded and visible enough, observe ad visibility and  manage video playback during user scrolling 
```java
@Override
public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	if (mVideoAd != null) {
		mVideoAd.showAdIfItVisible(mCustomAdapter, mListView);
	}
}

@Override
 public void onLoopMeVideoAdLoadSuccess(LoopMeNativeVideoAd arg0) {
  arg0.showAdIfItVisible(mCustomAdapter, mListView);
 }
```

Trigger `LoopMeNativeVideoAd`'s `pause()` and `resume()` method in appropriate activity lifecycle methods 
```java
	@Override
	protected void onPause() {
		mVideoAd.pause();
		super.onPause();
	}
	

	@Override
	protected void onResume() {
		mVideoAd.resume(mListView, mCustomAdapter)
		super.onResume();
	}
```

* Destroy
Trigger destroy() method to clean up resources when ad no need anymore. It can be done in Activity onDestroy() method.
```java
mVideoAd.destroy();
```

* Native video ad notifications:

Implement `LoopMeNativeVideoAd.Listener` in order to receive notifications during the loading/displaying ad processes, that you can use to trigger subsequent in-app events:
 * `-onLoopMeVideoAdLoadSuccess`: triggered when native video has been loaded the ad content
 * `-onLoopMeVideoAdLoadFail`: triggered when native video failed to load the ad content
 * `-onLoopMeVideoAdShow`: triggered when native video ad appeared on the screen
 * `-onLoopMeVideoAdHide`: triggered when native video ad disappeared from the screen
 * `-onLoopMeVideoAdVideoDidReachEnd`: triggered when native video video ad has been completely watched
 * `-onLoopMeVideoAdClicked`: triggered when native video ad was clicked
 * `-onLoopMeVideoAdExpired`: triggered when native video ad is expired, it is recommended to re-load
 * `-onLoopMeVideoAdLeaveApp`: triggered if SDK initiated app switching. E.g after click on ad user is redirected to market (or any other native app)

## Banner ads ##
* Add LoopMeBannerView to your layout (Set size and position of view in layout before displaying)
 ```xml
 <com.loopme.LoopMeBannerView
 android:id="@+id/banner_view_id"
 android:layout_width="320dp"
 android:layout_height="50dp"
 android:visibility="gone" />
 ```

* Create banner instance 
```java
public class YourActivity extends Activity implements LoopMeBanner.Listener {
  private LoopMeBanner mBanner;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
  
	/* ... */
	LoopMeBannerView view = (LoopMeBannerView)  findViewById(R.id.banner_view_id);
	mBanner= new LoopMeBanner(this, YOUR_APPKEY, view);
	mBanner.addListener(this);
  }
}
```

* Display banner ads
Trigger show() method to display Banner ad. It remains on the screen and refreshes automatically. If the user leaves screen this is developer's responsibility to hide banner and stop sending ad requests.
```java 
  mBanner.show();
```

* Banner notifications:
Implement `LoopMeBanner.Listener` in order to receive notifications during the loading/displaying ad processes, that you can use to trigger subsequent in-app events:
 * `-onLoopMeBannerLoadFail`: triggered when interstitial failed to load the ad content
 * `-onLoopMeBannerShow:`: triggered when banner ad appeared on the screen
 * `-onLoopMeBannerHide`: triggered when banner ad disappeared from the screen
 * `-onLoopMeBannerClicked`: triggered when banner ad was clicked
 
 
## Sample projects ##

Check out our project samples:
- `NativeVideoAdDemo` as an example of `LoopMeNativeVideoAd` integration within `ListView` and `ScrollView`
- `InterstitialDemo` as an example of `LoopMeInterstitial` integration

## What's new ##
**v4.0.7**

* Native video ads
* Small bug fixes

## License ##

see [License](LICENSE.md)
