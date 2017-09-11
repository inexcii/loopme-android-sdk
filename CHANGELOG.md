## Version 5.1.15 (11 of September, 2017)
- Ads targeting improvements

## Version 5.1.14 (7 of September, 2017)
- Bug fix

## Version 5.1.13 (1 of September, 2017)
- Ads targeting improvements

## Version 5.1.12 (21 of August, 2017)
- Added MOAT SDK viewability measurement support for Native and Rich Media ads

## Version 5.1.11 (27 of June, 2017)
- Ads targeting improvements

## Version 5.1.10 (27 of June, 2017)
- Bug fixes

## Version 5.1.9 (26 of June, 2017)
- Bug fixes. Added AMR support

## Version 5.1.8 (19 of June, 2017)
- Performance improvements. Bug fixes.
- MRAID compliancy improvements

## Version 5.1.7 (4 of May, 2017)
- Ads targeting improvements: better fit to device type and screens

## Version 5.1.6 (25 of April, 2017)
- Bug fixes

## Version 5.1.5 (14 of April, 2017)
- Bug fixing
- Ads delivery performance improvements

## Version 5.1.4 (27 of March, 2017)
- Added Corona plugin support

## Version 5.1.3 (23 of March, 2017)
- Bug fixes

## Version 5.1.2 (9 of March, 2017)
- Ads delivery performance improvements

## Version 5.1.1 (25 of January, 2017)
- Bug fixes

## Version 5.1.0 (14 of December, 2016)
- Updated error log
- Bug fixes

## Version 5.0 (19 of May, 2016)

- INTRODUCING LOOPME 360 VIDEO, THE ONLY FULL-SCREEN 360 SOLUTION DELIVERING GLOBAL SCALE
- Bug fixes (mailto links, sound, etc.)

## Version 4.8.0 (31 of March, 2016)
- Preload 25%

## Version 4.7.1 (23 of March, 2016)
- Battery info
- Bug fixes

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





