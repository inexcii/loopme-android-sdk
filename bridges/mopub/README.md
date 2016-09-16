# Android Mopub Bridge #

1. **[Overview](#overview)**
2. **[Register on LoopMe network](#register-on-loopme-network)**
3. **[Adding LoopMe Android SDK](#adding-loopme-android-sdk)**
4. **[Create Custom Native Network on Mopub dashboard](#create-custom-native-network-on-mopub-dashboard)**
5. **[Mediate from Mopub Interstitial to LoopMe Interstitial Ad](#mediate-from-mopub-interstitial-to-loopme-interstitial-ad)**
6. **[Mediate from Mopub banner to LoopMe Native Video Ad](#mediate-from-mopub-banner-to-loopme-native-video-ad)**
7. **[Sample project](#sample-project)**

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

Add the following to your build.gradle:
```java
repositories {
    jcenter()
}

dependencies {
    compile 'com.loopme:loopme-sdk:5.0@aar'
}
```

## Create Custom Native Network on Mopub dashboard ##

<b>NOTE:</b> `LoopMe` is not available as a predefine network in the Mopub tool, SDK bridge mediation needs to be manually configured with Mopub "Custom Native Network" option.

* On Mopub dashboard click Networks -> add a network. Choose “Custom Native Network”

## Mediate from Mopub Interstitial to LoopMe Interstitial Ad ##

<br><b>Configure Custom Native Network on Mopub dashboard </b>
<p><img src="images/mopub interstitial dashboard.png"  /></a>
<br> Instead of test_interstitial_p put your LoopMe app key.

* Download and copy `LoopMeMopubInterstitial` bridge class to the `com.mopub.mobileads` package in your project. 
* Load `MoPubInterstitial` as usual
```java
mInterstitial = new MoPubInterstitial(LoopMeMopubInterstitialSampleActivity.this, AD_UNIT_ID);
mInterstitial.setInterstitialAdListener(LoopMeMopubInterstitialSampleActivity.this);
mInterstitial.load();
```
* implement in your Activity `MoPubInterstitial.InterstitialAdListener` interface
* Show interstitial ad
```java
mInterstitial.show();
```

## Mediate from Mopub banner to LoopMe Native Video Ad ##

Displaying `LoopMe` native video ads requires extra integration steps in order to manage ad activity, e.g. to enable video playback or HTML animation.

<br><b>1 Configure Custom Native Network on Mopub dashboard </b>
<p><img src="images/mopub banner dashboard.png"  /></a>
<br> Instead of test_mpu put your LoopMe app key.

* Download and copy `LoopMeMopubBanner` bridge class to the `com.mopub.mobileads` package in your project. 
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
```java
@Override
protected void onResume() {
    LoopMeMopubBanner.resume();
    super.onResume();
}
```

## Sample project ##

Check out our `BannerSampleActivity.java` and `InterstitialSampleActivity` as an integration examples.

