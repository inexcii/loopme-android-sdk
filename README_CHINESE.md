# LoopMe-Android-SDK #

## 概览 ##

LoopMe是最大的移动视频DSP和广告网络，全球覆盖受众超过10亿。LoopMe的全屏视频和富媒体广告格式给受众带来互动性强的移动广告经验。

如果您有任何问题，请联系support@loopmemedia.com.

## 特点 ##

* 全屏静态图片插屏广告
* 全屏富媒体插屏广告
* 横幅广告
* 预加载视频广告
* 应用内置奖励提醒（包括视频完整浏览）

## 要求 ##

在您使用`loopme-android-sdk`之前，您需要前往我们的**[系统后台](http://loopme.me/)**注册并获取appKey。appKey是用来在我们的广告网络中识别您的应用的。（示例appKey：7643ba4d53）

要求`Android` 4.0 及更高系统

## 使用 ##

`loopme-android-sdk`的对接非常简单，能在10分钟内完成。

* 下载 `loopme-android-sdk`
* 复制 `loopme-sdk-x.x.x.jar`到您项目的`libs`文件夹
* 请确保在`libs` 文件夹加入 `android-support-v4.jar`
* 许可更新 `AndroidManifest.xml`
```xml
//需要的许可
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

//可选的许可
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.VIBRATE" />
```
* 在`manifest.xml`中声明活动
```xml
<activity android:name="com.loopme.AdActivity" android:configChanges="orientation|keyboardHidden|screenSize"
android:hardwareAccelerated="true"/>
<activity android:name="com.loopme.AdBrowserActivity" />
<activity android:name="com.loopme.PlayerActivity" android:configChanges="orientation|keyboardHidden|screenSize"
android:hardwareAccelerated="true"/>
```
* 展示全屏插屏或视频广告:
```java
public class YourActivity extends Activity implements LoopMeInterstitial.Listener {
  private LoopMeInterstitial mInterstitial;

  /* ... */

// 初始化LoopMe的插屏广告,
// 使用您在LoopMe后台注册app后得到的appKey:
  mInterstitial = new LoopMeInterstitial(this, YOUR_APPKEY);
  mInterstitial.addListener(this);

  /* ... */

// 用这个方法来预加载插屏
// （我们也推荐在-interstitialDidDisappear delegate方法中触发以便内容更新）
  mInterstitial.load();

  /* ... */

// LoopMe插屏广告的展示可以为用户发起的（比如：点击播放按钮）
// 或开发者发起（如游戏回合结束后）
  mInterstitial.show();

  /* ... */

// 当不需要时可以摧毁以便清理资源
  mInterstitial.destroy();
```
* 实现 `LoopMeInterstitial.Listener`在载入/展示广告过程中接受通知，以便您触发随后的应用内置事件
 * `-onLoopMeInterstitialLoadSuccess`: 当插屏广告载入广告内容时触发
 * `-onLoopMeInterstitialLoadFail`: 当插屏广告载入广告内容失败时触发
 * `-onLoopMeInterstitialShow`: 当插屏广告完成展示时触发
 * `-onLoopMeInterstitialHide`: 当插屏广告将在屏幕消失时触发
 * `-onLoopMeInterstitialVideoDidReachEnd`: 当插屏视频广告被完整观看时触发
 * `-onLoopMeInterstitialClicked`: 当插屏广告被点击时触发
 * `-onLoopMeInterstitialExpired`: 当插屏广告失效并推荐重新载入时触发


* 展示横幅广告:
 * 添加LoopMeBannerView到您的布局
 ```xml
 <com.loopme.LoopMeBannerView
 android:id="@+id/banner_view_id"
 android:layout_width="320dp"
 android:layout_height="50dp"
 android:visibility="gone" />
 ```
 * 展示前在布局中设置好尺寸和位置
```java
public class YourActivity extends Activity implements LoopMeBanner.Listener {
  private LoopMeBanner mBanner;

  /* ... */

  LoopMeBannerView view = (LoopMeBannerView)  findViewById(R.id.banner_view_id);
  mBanner= new LoopMeBanner(this, YOUR_APPKEY, view);
  mBanner.addListener(this);

  /* ... */

 // 触发 - 展示方法来展现横幅广告。横幅广告会停留在屏幕上并自动更新。
 // 当用户离开屏幕时，开发者有责任隐藏横幅广告并且停止发送广告请求
  mBanner.show();
```
* 实现`LoopMeBanner.Listener` 在载入/展示广告过程中接受通知，以便您触发随后的应用内置事件：
 * `-onLoopMeBannerLoadFail`: 当横幅广告加载广告内容失败时触发
 * `-onLoopMeBannerShow:`: 当横幅广告完成展示时触发
 * `-onLoopMeBannerHide`:当横幅广告完成从屏幕消失时触发
 * `-onLoopMeBannerClicked`: 当横幅广告被点击时触发

## 示例 ##

请查看我们的demo`loopme-example` 对接后示例。

## 更新 ##
**v4.0.5**

* 视频预加载表现改善
* 新视频广告UI设计
* 添加了完整视频播放提示

## 许可 ##

详见 [License](LICENSE.md)
