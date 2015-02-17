# LoopMe-Android-SDK #

[点击阅读SDK对接中文说明](README_CHINESE.md)

## Overview ##

LoopMe is the largest mobile video DSP and Ad Network, reaching over 1 billion consumers world-wide. LoopMe’s full-screen video and rich media ad formats deliver more engaging mobile advertising experiences to consumers on smartphones and tablets.
`loopme-android-sdk` is distributed as a `jar` and provides facilities to retrieve, display ads in your application.

If you have questions please contact us at support@loopmemedia.com.

## Features ##

* Full-screen image interstitials
* Full-screen rich media interstitials
* Banner ads
* Preloaded video ads
* In-app ad reward notifications, including video view completed

## Requirements ##

An appKey is required to use the `loopme-android-sdk`. The appKey uniquely identifies your app to the LoopMe ad network. (Example appKey: 7643ba4d53.) To get an appKey visit the **[LoopMe Dashboard](http://loopme.me/)**.

Requires `Android` 4.0 and up

## Usage ##

Integrating the `loopme-android-sdk` is very simple and should take less than 10 minutes.

* Download `loopme-android-sdk`
* Copy the `loopme-sdk-x.x.x.jar` to `libs` folder of your project
* Make sure `android-support-v4.jar` is also added to `libs` folder, otherwise add it
* Update `AndroidManifest.xml` with perfmissions
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
* Display full-screen interstitial or video ads:
```java
public class YourActivity extends Activity implements LoopMeInterstitial.Listener {
  private LoopMeInterstitial mInterstitial;

  /* ... */

// Initialize the LoopMe interstitial,
// using the unique appKey you received when registering your app via the LoopMe Dashboard:
  mInterstitial = new LoopMeInterstitial(this, YOUR_APPKEY);
  mInterstitial.addListener(this);

  /* ... */

// Use this method to preload the interstitial for display
// (we also recommend to trigger it in -interstitialDidDisappear delegate method to keep content up-to-date)
  mInterstitial.load();

  /* ... */

// Displaying the LoopMe Interstitial can be user-initiated (e.g press on button)
// or publisher-initiated (e.g. end of game level)
  mInterstitial.show();

  /* ... */

// Destroy when it is no longer needed to clean up resources
  mInterstitial.destroy();
```
* Implement `LoopMeInterstitial.Listener` in order to receive notifications during the loading/displaying ad processes, that you can use to trigger subsequent in-app events:
 * `-onLoopMeInterstitialLoadSuccess`: triggered when interstitial has been loaded the ad content
 * `-onLoopMeInterstitialLoadFail`: triggered when interstitial failed to load the ad content
 * `-onLoopMeInterstitialShow`: triggered when interstitial ad appeared on the screen
 * `-onLoopMeInterstitialHide`: triggered when interstitial ad disappeared from the screen
 * `-onLoopMeInterstitialVideoDidReachEnd`: triggered when interstitial video ad has been completely watched
 * `-onLoopMeInterstitialClicked`: triggered when interstitial ad was clicked
 * `-onLoopMeInterstitialExpired`: triggered when interstitial ad is expired, it is recommended to re-load


* Display banner ads:
 * Add LoopMeBannerView to your layout
 ```xml
 <com.loopme.LoopMeBannerView
 android:id="@+id/banner_view_id"
 android:layout_width="320dp"
 android:layout_height="50dp"
 android:visibility="gone" />
 ```
 * Set size and position of view in layout before displaying.
```java
public class YourActivity extends Activity implements LoopMeBanner.Listener {
  private LoopMeBanner mBanner;

  /* ... */

  LoopMeBannerView view = (LoopMeBannerView)  findViewById(R.id.banner_view_id);
  mBanner= new LoopMeBanner(this, YOUR_APPKEY, view);
  mBanner.addListener(this);

  /* ... */

 // Trigger -show method to display Banner ad. It remains on the screen and refreshes automatically.
 // If the user leaves screen this is developer's responsibility to hide banner and stop sending ad requests.
  mBanner.show();
```
* Implement `LoopMeBanner.Listener` in order to receive notifications during the loading/displaying ad processes, that you can use to trigger subsequent in-app events:
 * `-onLoopMeBannerLoadFail`: triggered when interstitial failed to load the ad content
 * `-onLoopMeBannerShow:`: triggered when banner ad appeared on the screen
 * `-onLoopMeBannerHide`: triggered when banner ad disappeared from the screen
 * `-onLoopMeBannerClicked`: triggered when banner ad was clicked

## Sample project ##

Check out our `loopme-example` project as an example of `loopme-android-sdk` integration.

## What's new ##
**v4.0.5**

* Video preloading performance improvements
* New video ad UI design
* Completed video view notification added

## License ##

see [License](LICENSE.md)
