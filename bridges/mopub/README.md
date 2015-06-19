# Android Mopub Bridge #

## Overview ##

LoopMe is the largest mobile video DSP and Ad Network, reaching over 1 billion consumers world-wide. LoopMe’s full-screen video and rich media ad formats deliver more engaging mobile advertising experiences to consumers on smartphones and tablets.
LoopMe supports SDK bridges to ad mediation platforms. The LoopMe SDK bridge allows you to control the use of the LoopMe SDK via your existing mediation platform.

`LoopMe Android bridge` allows publishers monitize applications using `Mopub mediation ad platform`.

<b>NOTE:</b> This page assumes you already have account on `Mopub` platform and integrated with the `Mopub` Android SDK

If you have questions please contact us at support@loopme.com.

## Register on LoopMe network ##

To use and setup the SDK bridge, register your app on the LoopMe network via the LoopMe Dashboard to retrieve a unique LoopMe app key for your app. The app key uniquely identifies your app in the LoopMe ad network (Example app key: 51bb33e7cb). 
You will need the app key during next steps of integration.

## Adding LoopMe Android SDK ##

* Download latest version of `loopme-sdk`
* Add dependency to `loopme-sdk` project
* Update `AndroidManifest.xml` with permissions:
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
and add the following activities:
```xml
<activity android:name="com.loopme.AdActivity" 
            android:theme="@android:style/Theme.Translucent"
            android:configChanges="orientation|keyboardHidden|screenSize" 
            android:hardwareAccelerated="true"/>
<activity android:name="com.loopme.AdBrowserActivity" />
```
## Adding LoopMe Mopub Android bridge ##
* Download and copy bridge classes to the `com.mopub.mobileads` package in your project. 

## Configure LoopMe network on Mopub dashboard ##

<b>NOTE:</b> `LoopMe` is not available as a predefine network in the Mopub tool, SDK bridge mediation needs to be manually configured with Mopub "Custom Native Network" option.

* On Mopub dashboard click Networks -> add a network. Choose “Custom Native Network”:
<p><img alt="Mopub edit network.png" src="mopub_dashboard.png" width="1044" height="509" /></a>
<p><b>Custom Event Class Data:</b> requires parameter "app_key" - value you received after registering your app on the LoopMe dashboard. E.g {"app_key":"298f62c196"} 
<p><b>Custom Event Class</b> should be "com.mopub.mobileads.LoopMeMopubInterstitial" for fullscreen ads or "com.mopub.mobileads.LoopMeMopubBanner" for native video ads.

## Displaying LoopMe Interstitial Ad ##

To display `LoopMe` interstitial ads, please follow `Mopub` interstitial integration guide: https://github.com/mopub/mopub-android-sdk/wiki/Interstitial-Integration

## Displaying LoopMe Native Video Ad ##

Displaying `LoopMe` native video ads requires extra integration steps in order to manage ad activity, e.g. to enable video playback or HTML animation.

* Define `LoopMeBannerView` in xml
* Init `MoPubView`
```java
MoPubView mBanner = (MoPubView) findViewById(R.id.mopubView);
mBanner.setAdUnitId(AD_UNIT_ID);
mBanner.setBannerAdListener(this);
```
* Add `LoopMeBannerView` in localExtras as "bannerView"
```java
LoopMeBannerView loopmeView = (LoopMeBannerView) findViewById(R.id.loopme_view);
Map extras = new HashMap();
extras.put("bannerView", loopmeView);
mBanner.setLocalExtras(extras);
```
* Load ad
```java
mBanner.loadAd();
```
* Add `LoopMeMopubBanner.pause()` in onPause() to pause video
```java
@Override
protected void onPause() {
    LoopMeMopubBanner.pause();
    super.onPause();
}
```
* Add `LoopMeMopubBanner.resume()` in onResume() to resume video
<br/><b>NOTE:</b> if you are using banner inside `ScrollView` trigger `LoopMeMopubBanner.resume(scrollview)`;
if you are using banner inside `ListView` trigger `LoopMeMopubBanner.resume(loopMeAdapter, listview)`
```java
@Override
protected void onResume() {
    LoopMeMopubBanner.resume();
    super.onResume();
}
```
* Subscribe to receive onScroll notifications (only for `ScrollView` and `ListView`) and trigger appropriate `resume` method in onScroll()
(`LoopMeMopubInterstitialSampleActivity` is an example explaining how to use `LoopMeMopubInterstitial`. 
Same for `LoopMeMopubBanner` and `LoopMeMopubBannerSampleActivity`)

## Sample project ##

Check out our `LoopMeMopubBannerSampleActivity.java` and `LoopMeMopubInterstitialSampleActivity` as an integration example.
