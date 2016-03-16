## Version 4.7.0 (7 of March, 2016)
- Simplify banner integration
- In-SDK error messages
- Added method clearCache()
- Added hash id
- removed `DOWNLOAD_WITHOUT_NOTIFICATION` permission
- Bug fixes

## Version 4.6.2 (16 of December, 2015)

- Added optional SDK settings to live debug report
- Bug fixes 

## Version 4.6.1 (8 of December, 2015)

- Bug fixes 

## Version 4.6.0 (23 of November, 2015)

- Expand to fullscreen
- Improved loading process (require 2 new permissions `android.permission.DOWNLOAD_WITHOUT_NOTIFICATION` and `android.permission.WRITE_EXTERNAL_STORAGE`)
- Live debugging
- Bug fixes for end card, wi-fi name 

## Version 4.5.2 (29 of October, 2015)

- New request parameters

## Version 4.5.1 (25 of September, 2015)

- Support `market://` urls if Play Market not installed
- Deep link handling

## Version 4.5.0 (7 of September, 2015)

- Support `https` ad requests
- Updated project structure
- Removed `WRITE_EXTERNAL_STORAGE` permission

## Version 4.4.0 (17 of August, 2015)

- Support video banner in `RecyclerView`
- Deprecated method `showAdIfItVisible()`. Instead use `show()` with proper parameters
- Adjust video volume to sytem volume
- Bug fixes

## Version 4.3.0 (30 of July, 2015)

- Support video banner in floating window
- Fixed issue with Activity context

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





