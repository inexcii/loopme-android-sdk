# Android Mopub Bridge #

1. **[Overview](#overview)**
2. **[Register on LoopMe network](#register-on-loopme-network)**
3. **[Adding LoopMe Android SDK](#adding-loopme-android-sdk)**
4. **[Create Custom Native Network on Mopub dashboard](#create-custom-native-network-on-mopub-dashboard)**
5. **[Mediate from Mopub Interstitial to LoopMe Interstitial Ad](#mediate-from-mopub-interstitial-to-loopme-interstitial-ad)**
6. **[Mediate from Mopub banner to LoopMe Native Video Ad](#mediate-from-mopub-banner-to-loopme-native-video-ad)**
7. **[Mediate from Mopub Native Ads to LoopMe video banner](#mediate-from-mopub-native-ads-to-loopme-video-banner)**
8. **[Sample project](#sample-project)**

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

## Create Custom Native Network on Mopub dashboard ##

<b>NOTE:</b> `LoopMe` is not available as a predefine network in the Mopub tool, SDK bridge mediation needs to be manually configured with Mopub "Custom Native Network" option.

* On Mopub dashboard click Networks -> add a network. Choose “Custom Native Network”

## Mediate from Mopub Interstitial to LoopMe Interstitial Ad ##

<br><b>Configure Custom Native Network on Mopub dashboard </b>
<p><img src="mopub interstitial dashboard.png"  /></a>
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
<p><img alt="Mopub edit network.png" src="mopub banner dashboard.png"  /></a>
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

## Mediate from Mopub Native Ads to LoopMe video banner ##

Displaying `LoopMe` native video ads requires extra integration steps in order to manage ad activity, e.g. to enable video playback or HTML animation. 

<br><br><b>1 Configure Custom Native Network on Mopub dashboard </b>
<p><img alt="Mopub edit network.png" src="mopub nativeads to video dashboard.png"  /></a>
<br> Instead of test_mpu put your LoopMe app key. In "position" define at which position in `ListView` video banner will be shown.
<br><br><b>2 Copy `LoopMeEventNative` to `com.mopub.nativeads` package </b>
<br><br><b>3 Update `YourCustomAdapter` (like in `CustomBaseAdapter.java`): </b>
- implement `LoopMeAdapter` interface
- add new item type and update related methods: `getItemViewType`, `getViewTypeCount`
- add in `getView` case for LoopMe banner:
```java
case TYPE_BANNER:
           //inflate xml layout for LoopMe banner
           v = mInflater.inflate(R.layout.list_ad_layout, null);
           //assume, you are using ListView ViewHolder pattern
           holder.banner_ad = (LoopMeBannerView) v.findViewById(R.id.banner_ad_spot);
           break;
```
and bind `LoopMeBannerView` to LoopMe banner
```java
mBanner.bindView(holder.banner_ad);
```
- add `addBannerToPosition` method

<br><b>4 YourActivity </b>
- init Mopub native ads as usual
- add 
```java
LoopMeEventNative.addListener(this);
```
- implement `LoopMeEventNative.Listener`
```java
    @Override
    public void onNativeAdFailed(String appKey, int position) {
        mBanner = LoopMeBanner.getInstance(appKey, this);
        if (mBanner != null) {
            mBanner.load();
            mBanner.setListener(this);
            mBaseAdapter.addBannerToPosition(position, mBanner);
        }
    }
```
- implement `LoopMeBanner.Listener` and add in `onLoopMeBannerLoadSuccess`:
```java
    @Override
    public void onLoopMeBannerLoadSuccess(LoopMeBanner loopMeBanner) {
        //mBaseAdapter - your adapter, NOT MopubAdAdapter
        loopMeBanner.showAdIfItVisible(mBaseAdapter, mListView);
    }
```
- implement `AbsListView.OnScrollListener` and subscribe `ListView` to receive onScroll notifications
```java
mListView.setOnScrollListener(this);

@Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mBanner != null) {
            mBanner.showAdIfItVisible(mBaseAdapter, mListView);
        }
    }
```
- trigger `pause()` in Activity onPause() to pause video playback:
```java
    @Override
    protected void onPause() {
        if (mBanner != null) {
            mBanner.pause();
        }
        super.onPause();
    }
```
- trigger `showAdIfItVisible` in Activity onResume() to resume video playback:
```java
    @Override
    protected void onResume() {
        if (mBanner != null) {
            mBanner.showAdIfItVisible(mBaseAdapter, mListView);
        }
        super.onResume();
    }
```
- trigger `dismiss` and `destroy` in Activity onDestroy():
```java
@Override
    protected void onDestroy() {
        if (mBanner != null) {
            mBanner.dismiss();
            mBanner.destroy();
        }
        mAdAdapter.destroy();
        super.onDestroy();
    }
```
## Sample project ##

Check out our `LoopMeMopubBannerSampleActivity.java` and `LoopMeMopubInterstitialSampleActivity` as an integration example.
For native ads mediation check `NativeAdsToVideoBannerSample` project
