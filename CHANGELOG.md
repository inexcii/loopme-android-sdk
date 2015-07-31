## Version 4.2.2.2 (10 of July, 2015)

- Fix for ad clicks (redirect with deep links)

## Version 4.2.2.1 (09 of July, 2015)

- Hotfix for ad clicks

## Version 4.2.2 (03 of June, 2015)

- Do not cache video on Cellular network by default (with possibility to configure it)

## Version 4.2.1 (06 of May, 2015)

- Do not display ads if `Android` version under 4.0


## Version 4.2.0 (30 of April, 2015)

- Displaying `minimized video` when original `banner` ad is out of viewport, with configurable size and margins.
- `Swipe-to-remove` for `minimized video`.
- `LoopMeAdHolder` class is deprecated, use `LoopMeBanner.getInstance(appkey, this);` which returns existing ad object or creates new one for a given appKey.
- `LoopMeBanner.resume()` method is deprecated, use `LoopMeBanner.showAdIfVisible`
- `SurfaceView` is replaced with `TextureView`, you don't have to create zero-height UI element in layout'

**Bug fix:**
- Fire ad `onLoopMeBannerLoadFail` or `onLoopMeInterstitialLoadFail` instantly if video source can't be downloaded
- `Illegal state` fixed for video ad playback

## Version 4.1.0 (27 of March, 2015)

- Improved ad loading process by caching video.
- Ability to pass custom params as part of ad request.
- Ability to dismiss ad programmatically.
- Small bug fixes





