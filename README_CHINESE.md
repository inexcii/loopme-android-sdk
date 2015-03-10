# LoopMe-Android-SDK #



1. **[概览](#概览)**
2. **[特点](#特点)**
3. **[要求](#要求)**
4. **[SDK对接](#SDK对接)**
  * **[全屏插屏广告](#全屏插屏广告)**
  * **[原生视频广告](#原生视频广告)**
  * **[横幅广告](#横幅广告)**
5. **[示例](#示例)**
6. **[更新](#更新)**

## 概览 ##

LoopMe是最大的移动视频DSP和广告网络，全球覆盖受众超过10亿。LoopMe的全屏视频和富媒体广告格式给受众带来互动性强的移动广告经验。

`loopme-android-sdk`以`jar`格式传播并且能够在你的应用里检索、展示广告。

如果您有任何问题，请联系support@loopmemedia.com.

## 特点 ##

* 全屏图片插屏广告
* 全屏富媒体插屏广告
* 预加载视频广告
* 横幅广告
* 应用内置奖励提醒（包括视频完整浏览）

## 要求 ##

在您使用`loopme-android-sdk`之前，您需要前往我们的**[系统后台](http://loopme.me/)**注册并获取appKey。appKey是用来在我们的广告网络中识别您的应用的。（示例appKey：7643ba4d53）

要求`Android` 4.0 及更高系统

## SDK对接 ##

* 下载最新版本的 `loopme-android-sdk`
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
## 全屏插屏广告 ##
* 创建 `LoopMeInterstitial` 实例并且检索广告
```java
public class YourActivity extends Activity implements LoopMeInterstitial.Listener {

  private LoopMeInterstitial mInterstitial;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
  super.onCreate(savedInstanceState);

  /* ... */
  mInterstitial = new LoopMeInterstitial(this, YOUR_APPKEY); // YOUR_APPKEY received during registering your app via the LoopMe Dashboard
  mInterstitial.addListener(this);
  mInterstitial.load(); // 用这个方法来预加载展示插屏广告(我们也推荐在-interstitialDidDisappear委托方法里触发它来保持更新)
  }
}
```

* 展示插屏广告

LoopMe插屏广告的展示可以为用户发起的（比如：点击播放按钮）或开发者发起（如游戏回合结束后）
```java
  mInterstitial.show();
```

* 清除插屏广告

当不需要时可以清除以便清理资源
```java
  mInterstitial.destroy();
```

* 插屏广告通知:

* 实现 `LoopMeInterstitial.Listener`在载入/展示广告过程中接受通知，以便您触发随后的应用内置事件
 * `-onLoopMeInterstitialLoadSuccess`: 当插屏广告载入广告内容时触发
 * `-onLoopMeInterstitialLoadFail`: 当插屏广告载入广告内容失败时触发
 * `-onLoopMeInterstitialShow`: 当插屏广告完成展示时触发
 * `-onLoopMeInterstitialHide`: 当插屏广告将在屏幕消失时触发
 * `-onLoopMeInterstitialVideoDidReachEnd`: 当插屏视频广告被完整观看时触发
 * `-onLoopMeInterstitialClicked`: 当插屏广告被点击时触发
 * `-onLoopMeInterstitialExpired`: 当插屏广告失效并推荐重新载入时触发

 ## 原生视频广告 ##

 `LoopMeNativeVideoAd` 指在可滑动内容区域中插入的一个视频广告窗口。
 `LoopMeSDK`不会覆盖`ListView`/`GridView` 适配器。
 请在`ScrollView`和`ListView`内参考`NativeVideoAdDemo`示例。

 * 为广告创建xml布局

 * 如果你在自定适配器内使用`ListView`或`GridView`，实现`LoopMeAdapter`界面。

 * 创建`LoopMeNativeVideoAd`并且检索广告

 视频是在`SurfaceView`里动态渲染的，为了避免"闪屏"效果，在活动布局里的`SurfaceView`添加zero-height
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
   mVideoAd.load(); // 用这个方法来预加载广告（我们也推荐在`onLoopMeVideoAdHide`里用回调方法触发以进行下一次广告的预加载）
   }
 }
 ```

 * 展示视频广告

 如果你在可滑动内容里展示广告，订阅滚动通知
 ```java
 mListView.setOnScrollListener(this);
 ```
 并触发`onScroll()`和`onLoopMeVideoAdLoadSuccess`里的 `showAdIfItVisible()`,这个给SDK提供载入后立即展现广告的能力，可观察广告的可见度，并在用户滑动屏幕时进行视频回放的操作。
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

在合适的生命周期方法中触发`LoopMeNativeVideoAd`的`pause()`和`resume()`方法
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

 * 清除
 当不再需要广告时，触发 destroy()方法来清理资源。它可以在Activity onDestroy() 方法中实现。
 ```java
 mVideoAd.destroy();
 ```

 * 原生视频广告通知:

 实现`LoopMeNativeVideoAd.Listener`在载入/展示广告时接收通知，以便您触发随后的应用内置事件：
  * `-onLoopMeVideoAdLoadSuccess`: 当原生视频被载入时触发
  * `-onLoopMeVideoAdLoadFail`: 当原生视频载入失败时触发
  * `-onLoopMeVideoAdShow`: 当原生视频广告在屏幕上出现时触发
  * `-onLoopMeVideoAdHide`: 当原生视频广告从屏幕上消失时触发
  * `-onLoopMeVideoAdVideoDidReachEnd`: 当原生视频广告被完整收看时触发
  * `-onLoopMeVideoAdClicked`: 当原生视频广告被点击时触发
  * `-onLoopMeVideoAdExpired`: 当原生视频广告失效时触发，推荐再次载入
  * `-onLoopMeVideoAdLeaveApp`: 当接入了SDK的应用被转换，例如点击后广告后跳转到应用商城（或其他原生应用里）时触发

  ## 横幅广告 ##
  * 添加LoopMeBannerView到您的布局(展示前在布局中设置好尺寸和位置)
   ```xml
   <com.loopme.LoopMeBannerView
   android:id="@+id/banner_view_id"
   android:layout_width="320dp"
   android:layout_height="50dp"
   android:visibility="gone" />
   ```

  * 创建横幅广告实例
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

  * 展示横幅广告
  触发show() 方法来展示横幅广告。它会停留在屏幕上并且自动刷新。如果用户离开屏幕，开发者有权利隐藏横幅广告并且停止发送广告请求。
  ```java
    mBanner.show();
  ```
```
* 实现`LoopMeBanner.Listener` 在载入/展示广告过程中接受通知，以便您触发随后的应用内置事件：
 * `-onLoopMeBannerLoadFail`: 当横幅广告加载广告内容失败时触发
 * `-onLoopMeBannerShow:`: 当横幅广告完成展示时触发
 * `-onLoopMeBannerHide`:当横幅广告完成从屏幕消失时触发
 * `-onLoopMeBannerClicked`: 当横幅广告被点击时触发



## 示例 ##

请查看我们的demo`loopme-example` 对接后示例。

## 更新 ##
**v4.0.7**

* 视频预加载表现改善
* 新视频广告UI设计
* 添加了完整视频播放提示

## 许可 ##

详见 [License](LICENSE.md)
