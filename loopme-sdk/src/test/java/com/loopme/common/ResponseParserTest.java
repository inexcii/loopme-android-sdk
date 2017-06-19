package com.loopme.common;

import com.loopme.constants.AdFormat;

import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.junit.Assert.*;
import org.junit.Test;

@RunWith(PowerMockRunner.class)
public class ResponseParserTest {

    @Test
    public void responseWrongTest() {

        ResponseParser.Listener listener = new ResponseParser.Listener() {
            @Override
            public void onParseError(LoopMeError message) {
                assertNotNull(message);
            }
        };
        ResponseParser parser = new ResponseParser(listener, AdFormat.BANNER);
        assertNotNull(parser);
        String responseStr = "bla";
        AdParams params = parser.getAdParams(responseStr);
        assertNull(params);
    }

    @Test
    public void responseCorrectTest() {

        ResponseParser.Listener listener = new ResponseParser.Listener() {
            @Override
            public void onParseError(LoopMeError message) {
            }
        };
        ResponseParser parser = new ResponseParser(listener, AdFormat.BANNER);
        assertNotNull(parser);
        String responseStr = "{\"script\":\"<html><head><meta charset=\\\"utf-8\\\"><meta " +
                "name=\\\"viewport\\\" content=\\\"initial-scale=1.0, user-scalable=0, " +
                "minimum-scale=1.0, maximum-scale=1.0\\\"><title>LoopMe</title></head><body>" +
                "<script>lmCampaigns = {\\\"eventLinkTemplate\\\":\\\"https://track.loopme.me/sj/tr?et=" +
                "__EVENT_TYPE__&rid=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "id=5761a2a5a4c0b0e43f010b55&name=__CUSTOM_EVENT_NAME__\\\",\\\"apiCreativesLink\\\"" +
                ":\\\"https://loopme.me/ad/jsonp?di=HdrUubFPSGijz6pYGSswxZ9hYDRjpkfr1yAyfqDOg8g&" +
                "id=5761a2a5a4c0b0e43f010b55&rid=5761a2a5a4c0b0e43f010b55&callback=__CALLBACK__&" +
                "tpl_path=https%3A%2F%2Fi.loopme.me%2Fhtml%2Fmpu_video_tpl%2Fvideo_tpl.html\\\",\\\"" +
                "requestId\\\":\\\"5761a2a5a4c0b0e43f010b55\\\",\\\"ads\\\":[\\\"5761a2a5a4c0b0e43f010b55\\\"]," +
                "\\\"package_ids\\\":[]}</script><script src=\\\"https://loopme.me/lm.bridge.js?cb=1466016421246\\\">" +
                "</script><script>if (!L || !L.bridge) {window.location = \\\"loopme://webview/fail\\\";}" +
                "</script><div id=\\\"LOOPME_widget\\\"><script id=\\\"LOOPME_script\\\" " +
                "src=\\\"https://loopme.me/lm.main.js?_=1466016421246 \\\"data-app-key\\\"=\\\"94b8115817\\\" " +
                "data-delay=\\\"2000\\\"></script></div></body></html>\",\"tracking\":{\"error\":" +
                "[\"https://loopme.me/sj/tr?et=ERROR&id=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "error_type={error_type}\"]},\"settings\":{\"format\":\"banner\",\"orientation\":" +
                "\"default\",\"token\":\"5761a2a5a4c0b0e43f010b55\",\"v360\":0,\"ad_expiry_time\":3600," +
                "\"package_ids\":[]}}";
        AdParams params = parser.getAdParams(responseStr);
        assertNotNull(params);

        assertTrue(params.getAdFormat().equalsIgnoreCase(StaticParams.BANNER_TAG));
        assertNotNull(params.getHtml());
        assertNull(params.getAdOrientation());
        assertTrue(params.getToken().equalsIgnoreCase("5761a2a5a4c0b0e43f010b55"));
        assertFalse(params.isVideo360());
        assertEquals(params.getExpiredTime(), 3600 * 1000);
        assertNotNull(params.getPackageIds());
        assertEquals(params.getPackageIds().size(), 0);
    }

    @Test
    public void responseWithoutExpTimeTest() {

        ResponseParser.Listener listener = new ResponseParser.Listener() {
            @Override
            public void onParseError(LoopMeError message) {
            }
        };
        ResponseParser parser = new ResponseParser(listener, AdFormat.BANNER);
        assertNotNull(parser);
        String responseStr = "{\"script\":\"<html><head><meta charset=\\\"utf-8\\\"><meta " +
                "name=\\\"viewport\\\" content=\\\"initial-scale=1.0, user-scalable=0, " +
                "minimum-scale=1.0, maximum-scale=1.0\\\"><title>LoopMe</title></head><body>" +
                "<script>lmCampaigns = {\\\"eventLinkTemplate\\\":\\\"https://track.loopme.me/sj/tr?et=" +
                "__EVENT_TYPE__&rid=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "id=5761a2a5a4c0b0e43f010b55&name=__CUSTOM_EVENT_NAME__\\\",\\\"apiCreativesLink\\\"" +
                ":\\\"https://loopme.me/ad/jsonp?di=HdrUubFPSGijz6pYGSswxZ9hYDRjpkfr1yAyfqDOg8g&" +
                "id=5761a2a5a4c0b0e43f010b55&rid=5761a2a5a4c0b0e43f010b55&callback=__CALLBACK__&" +
                "tpl_path=https%3A%2F%2Fi.loopme.me%2Fhtml%2Fmpu_video_tpl%2Fvideo_tpl.html\\\",\\\"" +
                "requestId\\\":\\\"5761a2a5a4c0b0e43f010b55\\\",\\\"ads\\\":[\\\"5761a2a5a4c0b0e43f010b55\\\"]," +
                "\\\"package_ids\\\":[]}</script><script src=\\\"https://loopme.me/lm.bridge.js?cb=1466016421246\\\">" +
                "</script><script>if (!L || !L.bridge) {window.location = \\\"loopme://webview/fail\\\";}" +
                "</script><div id=\\\"LOOPME_widget\\\"><script id=\\\"LOOPME_script\\\" " +
                "src=\\\"https://loopme.me/lm.main.js?_=1466016421246 \\\"data-app-key\\\"=\\\"94b8115817\\\" " +
                "data-delay=\\\"2000\\\"></script></div></body></html>\",\"tracking\":{\"error\":" +
                "[\"https://loopme.me/sj/tr?et=ERROR&id=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "error_type={error_type}\"]},\"settings\":{\"format\":\"banner\",\"orientation\":" +
                "\"default\",\"token\":\"5761a2a5a4c0b0e43f010b55\",\"v360\":0," +
                "\"package_ids\":[]}}";
        AdParams params = parser.getAdParams(responseStr);
        assertNotNull(params);

        assertTrue(params.getAdFormat().equalsIgnoreCase(StaticParams.BANNER_TAG));
        assertNotNull(params.getHtml());
        assertNull(params.getAdOrientation());
        assertTrue(params.getToken().equalsIgnoreCase("5761a2a5a4c0b0e43f010b55"));
        assertFalse(params.isVideo360());
        assertEquals(params.getExpiredTime(), StaticParams.DEFAULT_EXPIRED_TIME);
        assertNotNull(params.getPackageIds());
        assertEquals(params.getPackageIds().size(), 0);
    }

    @Test
    public void responseTooLowExpTimeTest() {

        ResponseParser.Listener listener = new ResponseParser.Listener() {
            @Override
            public void onParseError(LoopMeError message) {
            }
        };
        ResponseParser parser = new ResponseParser(listener, AdFormat.BANNER);
        assertNotNull(parser);
        String responseStr = "{\"script\":\"<html><head><meta charset=\\\"utf-8\\\"><meta " +
                "name=\\\"viewport\\\" content=\\\"initial-scale=1.0, user-scalable=0, " +
                "minimum-scale=1.0, maximum-scale=1.0\\\"><title>LoopMe</title></head><body>" +
                "<script>lmCampaigns = {\\\"eventLinkTemplate\\\":\\\"https://track.loopme.me/sj/tr?et=" +
                "__EVENT_TYPE__&rid=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "id=5761a2a5a4c0b0e43f010b55&name=__CUSTOM_EVENT_NAME__\\\",\\\"apiCreativesLink\\\"" +
                ":\\\"https://loopme.me/ad/jsonp?di=HdrUubFPSGijz6pYGSswxZ9hYDRjpkfr1yAyfqDOg8g&" +
                "id=5761a2a5a4c0b0e43f010b55&rid=5761a2a5a4c0b0e43f010b55&callback=__CALLBACK__&" +
                "tpl_path=https%3A%2F%2Fi.loopme.me%2Fhtml%2Fmpu_video_tpl%2Fvideo_tpl.html\\\",\\\"" +
                "requestId\\\":\\\"5761a2a5a4c0b0e43f010b55\\\",\\\"ads\\\":[\\\"5761a2a5a4c0b0e43f010b55\\\"]," +
                "\\\"package_ids\\\":[]}</script><script src=\\\"https://loopme.me/lm.bridge.js?cb=1466016421246\\\">" +
                "</script><script>if (!L || !L.bridge) {window.location = \\\"loopme://webview/fail\\\";}" +
                "</script><div id=\\\"LOOPME_widget\\\"><script id=\\\"LOOPME_script\\\" " +
                "src=\\\"https://loopme.me/lm.main.js?_=1466016421246 \\\"data-app-key\\\"=\\\"94b8115817\\\" " +
                "data-delay=\\\"2000\\\"></script></div></body></html>\",\"tracking\":{\"error\":" +
                "[\"https://loopme.me/sj/tr?et=ERROR&id=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "error_type={error_type}\"]},\"settings\":{\"format\":\"banner\",\"orientation\":" +
                "\"default\",\"token\":\"5761a2a5a4c0b0e43f010b55\",\"v360\":0,\"ad_expiry_time\":30," +
                "\"package_ids\":[]}}";
        AdParams params = parser.getAdParams(responseStr);
        assertNotNull(params);

        assertTrue(params.getAdFormat().equalsIgnoreCase(StaticParams.BANNER_TAG));
        assertNotNull(params.getHtml());
        assertNull(params.getAdOrientation());
        assertTrue(params.getToken().equalsIgnoreCase("5761a2a5a4c0b0e43f010b55"));
        assertFalse(params.isVideo360());
        assertEquals(params.getExpiredTime(), StaticParams.DEFAULT_EXPIRED_TIME);
        assertNotNull(params.getPackageIds());
        assertEquals(params.getPackageIds().size(), 0);
    }

    @Test
    public void responseV360TrueTest() {

        ResponseParser.Listener listener = new ResponseParser.Listener() {
            @Override
            public void onParseError(LoopMeError message) {
            }
        };
        ResponseParser parser = new ResponseParser(listener, AdFormat.BANNER);
        assertNotNull(parser);
        String responseStr = "{\"script\":\"<html><head><meta charset=\\\"utf-8\\\"><meta " +
                "name=\\\"viewport\\\" content=\\\"initial-scale=1.0, user-scalable=0, " +
                "minimum-scale=1.0, maximum-scale=1.0\\\"><title>LoopMe</title></head><body>" +
                "<script>lmCampaigns = {\\\"eventLinkTemplate\\\":\\\"https://track.loopme.me/sj/tr?et=" +
                "__EVENT_TYPE__&rid=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "id=5761a2a5a4c0b0e43f010b55&name=__CUSTOM_EVENT_NAME__\\\",\\\"apiCreativesLink\\\"" +
                ":\\\"https://loopme.me/ad/jsonp?di=HdrUubFPSGijz6pYGSswxZ9hYDRjpkfr1yAyfqDOg8g&" +
                "id=5761a2a5a4c0b0e43f010b55&rid=5761a2a5a4c0b0e43f010b55&callback=__CALLBACK__&" +
                "tpl_path=https%3A%2F%2Fi.loopme.me%2Fhtml%2Fmpu_video_tpl%2Fvideo_tpl.html\\\",\\\"" +
                "requestId\\\":\\\"5761a2a5a4c0b0e43f010b55\\\",\\\"ads\\\":[\\\"5761a2a5a4c0b0e43f010b55\\\"]," +
                "\\\"package_ids\\\":[]}</script><script src=\\\"https://loopme.me/lm.bridge.js?cb=1466016421246\\\">" +
                "</script><script>if (!L || !L.bridge) {window.location = \\\"loopme://webview/fail\\\";}" +
                "</script><div id=\\\"LOOPME_widget\\\"><script id=\\\"LOOPME_script\\\" " +
                "src=\\\"https://loopme.me/lm.main.js?_=1466016421246 \\\"data-app-key\\\"=\\\"94b8115817\\\" " +
                "data-delay=\\\"2000\\\"></script></div></body></html>\",\"tracking\":{\"error\":" +
                "[\"https://loopme.me/sj/tr?et=ERROR&id=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "error_type={error_type}\"]},\"settings\":{\"format\":\"banner\",\"orientation\":" +
                "\"default\",\"token\":\"5761a2a5a4c0b0e43f010b55\",\"v360\":1,\"ad_expiry_time\":3600," +
                "\"package_ids\":[]}}";
        AdParams params = parser.getAdParams(responseStr);
        assertNotNull(params);

        assertTrue(params.getAdFormat().equalsIgnoreCase(StaticParams.BANNER_TAG));
        assertNotNull(params.getHtml());
        assertNull(params.getAdOrientation());
        assertTrue(params.getToken().equalsIgnoreCase("5761a2a5a4c0b0e43f010b55"));
        assertTrue(params.isVideo360());
        assertEquals(params.getExpiredTime(), 3600 * 1000);
        assertNotNull(params.getPackageIds());
        assertEquals(params.getPackageIds().size(), 0);
    }

    @Test
    public void responseV360AbsentTest() {

        ResponseParser.Listener listener = new ResponseParser.Listener() {
            @Override
            public void onParseError(LoopMeError message) {
            }
        };
        ResponseParser parser = new ResponseParser(listener, AdFormat.BANNER);
        assertNotNull(parser);
        String responseStr = "{\"script\":\"<html><head><meta charset=\\\"utf-8\\\"><meta " +
                "name=\\\"viewport\\\" content=\\\"initial-scale=1.0, user-scalable=0, " +
                "minimum-scale=1.0, maximum-scale=1.0\\\"><title>LoopMe</title></head><body>" +
                "<script>lmCampaigns = {\\\"eventLinkTemplate\\\":\\\"https://track.loopme.me/sj/tr?et=" +
                "__EVENT_TYPE__&rid=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "id=5761a2a5a4c0b0e43f010b55&name=__CUSTOM_EVENT_NAME__\\\",\\\"apiCreativesLink\\\"" +
                ":\\\"https://loopme.me/ad/jsonp?di=HdrUubFPSGijz6pYGSswxZ9hYDRjpkfr1yAyfqDOg8g&" +
                "id=5761a2a5a4c0b0e43f010b55&rid=5761a2a5a4c0b0e43f010b55&callback=__CALLBACK__&" +
                "tpl_path=https%3A%2F%2Fi.loopme.me%2Fhtml%2Fmpu_video_tpl%2Fvideo_tpl.html\\\",\\\"" +
                "requestId\\\":\\\"5761a2a5a4c0b0e43f010b55\\\",\\\"ads\\\":[\\\"5761a2a5a4c0b0e43f010b55\\\"]," +
                "\\\"package_ids\\\":[]}</script><script src=\\\"https://loopme.me/lm.bridge.js?cb=1466016421246\\\">" +
                "</script><script>if (!L || !L.bridge) {window.location = \\\"loopme://webview/fail\\\";}" +
                "</script><div id=\\\"LOOPME_widget\\\"><script id=\\\"LOOPME_script\\\" " +
                "src=\\\"https://loopme.me/lm.main.js?_=1466016421246 \\\"data-app-key\\\"=\\\"94b8115817\\\" " +
                "data-delay=\\\"2000\\\"></script></div></body></html>\",\"tracking\":{\"error\":" +
                "[\"https://loopme.me/sj/tr?et=ERROR&id=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "error_type={error_type}\"]},\"settings\":{\"format\":\"banner\",\"orientation\":" +
                "\"default\",\"token\":\"5761a2a5a4c0b0e43f010b55\",\"ad_expiry_time\":3600," +
                "\"package_ids\":[]}}";
        AdParams params = parser.getAdParams(responseStr);
        assertNotNull(params);

        assertTrue(params.getAdFormat().equalsIgnoreCase(StaticParams.BANNER_TAG));
        assertNotNull(params.getHtml());
        assertNull(params.getAdOrientation());
        assertTrue(params.getToken().equalsIgnoreCase("5761a2a5a4c0b0e43f010b55"));
        assertFalse(params.isVideo360());
        assertEquals(params.getExpiredTime(), 3600 * 1000);
        assertNotNull(params.getPackageIds());
        assertEquals(params.getPackageIds().size(), 0);
    }

    // ---------- part preload -------------
    @Test
    public void responsePreloadAbsentTest() {

        ResponseParser.Listener listener = new ResponseParser.Listener() {
            @Override
            public void onParseError(LoopMeError message) {
            }
        };
        ResponseParser parser = new ResponseParser(listener, AdFormat.BANNER);
        assertNotNull(parser);
        String responseStr = "{\"script\":\"<html><head><meta charset=\\\"utf-8\\\"><meta " +
                "name=\\\"viewport\\\" content=\\\"initial-scale=1.0, user-scalable=0, " +
                "minimum-scale=1.0, maximum-scale=1.0\\\"><title>LoopMe</title></head><body>" +
                "<script>lmCampaigns = {\\\"eventLinkTemplate\\\":\\\"https://track.loopme.me/sj/tr?et=" +
                "__EVENT_TYPE__&rid=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "id=5761a2a5a4c0b0e43f010b55&name=__CUSTOM_EVENT_NAME__\\\",\\\"apiCreativesLink\\\"" +
                ":\\\"https://loopme.me/ad/jsonp?di=HdrUubFPSGijz6pYGSswxZ9hYDRjpkfr1yAyfqDOg8g&" +
                "id=5761a2a5a4c0b0e43f010b55&rid=5761a2a5a4c0b0e43f010b55&callback=__CALLBACK__&" +
                "tpl_path=https%3A%2F%2Fi.loopme.me%2Fhtml%2Fmpu_video_tpl%2Fvideo_tpl.html\\\",\\\"" +
                "requestId\\\":\\\"5761a2a5a4c0b0e43f010b55\\\",\\\"ads\\\":[\\\"5761a2a5a4c0b0e43f010b55\\\"]," +
                "\\\"package_ids\\\":[]}</script><script src=\\\"https://loopme.me/lm.bridge.js?cb=1466016421246\\\">" +
                "</script><script>if (!L || !L.bridge) {window.location = \\\"loopme://webview/fail\\\";}" +
                "</script><div id=\\\"LOOPME_widget\\\"><script id=\\\"LOOPME_script\\\" " +
                "src=\\\"https://loopme.me/lm.main.js?_=1466016421246 \\\"data-app-key\\\"=\\\"94b8115817\\\" " +
                "data-delay=\\\"2000\\\"></script></div></body></html>\",\"tracking\":{\"error\":" +
                "[\"https://loopme.me/sj/tr?et=ERROR&id=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "error_type={error_type}\"]},\"settings\":{\"format\":\"banner\",\"orientation\":" +
                "\"default\",\"token\":\"5761a2a5a4c0b0e43f010b55\",\"ad_expiry_time\":3600," +
                "\"package_ids\":[]}}";
        AdParams params = parser.getAdParams(responseStr);
        assertNotNull(params);

        assertTrue(params.getAdFormat().equalsIgnoreCase(StaticParams.BANNER_TAG));
        assertNotNull(params.getHtml());
        assertNull(params.getAdOrientation());
        assertTrue(params.getToken().equalsIgnoreCase("5761a2a5a4c0b0e43f010b55"));
        assertFalse(params.isVideo360());
        assertEquals(params.getExpiredTime(), 3600 * 1000);
        assertNotNull(params.getPackageIds());
        assertEquals(params.getPackageIds().size(), 0);
        assertFalse(params.getPartPreload());
    }

    @Test
    public void responsePreloadTrueTest() {

        ResponseParser.Listener listener = new ResponseParser.Listener() {
            @Override
            public void onParseError(LoopMeError message) {
            }
        };
        ResponseParser parser = new ResponseParser(listener, AdFormat.BANNER);
        assertNotNull(parser);
        String responseStr = "{\"script\":\"<html><head><meta charset=\\\"utf-8\\\"><meta " +
                "name=\\\"viewport\\\" content=\\\"initial-scale=1.0, user-scalable=0, " +
                "minimum-scale=1.0, maximum-scale=1.0\\\"><title>LoopMe</title></head><body>" +
                "<script>lmCampaigns = {\\\"eventLinkTemplate\\\":\\\"https://track.loopme.me/sj/tr?et=" +
                "__EVENT_TYPE__&rid=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "id=5761a2a5a4c0b0e43f010b55&name=__CUSTOM_EVENT_NAME__\\\",\\\"apiCreativesLink\\\"" +
                ":\\\"https://loopme.me/ad/jsonp?di=HdrUubFPSGijz6pYGSswxZ9hYDRjpkfr1yAyfqDOg8g&" +
                "id=5761a2a5a4c0b0e43f010b55&rid=5761a2a5a4c0b0e43f010b55&callback=__CALLBACK__&" +
                "tpl_path=https%3A%2F%2Fi.loopme.me%2Fhtml%2Fmpu_video_tpl%2Fvideo_tpl.html\\\",\\\"" +
                "requestId\\\":\\\"5761a2a5a4c0b0e43f010b55\\\",\\\"ads\\\":[\\\"5761a2a5a4c0b0e43f010b55\\\"]," +
                "\\\"package_ids\\\":[]}</script><script src=\\\"https://loopme.me/lm.bridge.js?cb=1466016421246\\\">" +
                "</script><script>if (!L || !L.bridge) {window.location = \\\"loopme://webview/fail\\\";}" +
                "</script><div id=\\\"LOOPME_widget\\\"><script id=\\\"LOOPME_script\\\" " +
                "src=\\\"https://loopme.me/lm.main.js?_=1466016421246 \\\"data-app-key\\\"=\\\"94b8115817\\\" " +
                "data-delay=\\\"2000\\\"></script></div></body></html>\",\"tracking\":{\"error\":" +
                "[\"https://loopme.me/sj/tr?et=ERROR&id=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "error_type={error_type}\"]},\"settings\":{\"format\":\"banner\",\"orientation\":" +
                "\"default\",\"token\":\"5761a2a5a4c0b0e43f010b55\",\"ad_expiry_time\":3600," +
                "\"preload25\":1," +
                "\"package_ids\":[]}}";
        AdParams params = parser.getAdParams(responseStr);
        assertNotNull(params);

        assertTrue(params.getAdFormat().equalsIgnoreCase(StaticParams.BANNER_TAG));
        assertNotNull(params.getHtml());
        assertNull(params.getAdOrientation());
        assertTrue(params.getToken().equalsIgnoreCase("5761a2a5a4c0b0e43f010b55"));
        assertFalse(params.isVideo360());
        assertEquals(params.getExpiredTime(), 3600 * 1000);
        assertNotNull(params.getPackageIds());
        assertEquals(params.getPackageIds().size(), 0);
        assertTrue(params.getPartPreload());
    }

    @Test
    public void responsePreloadFalseTest() {

        ResponseParser.Listener listener = new ResponseParser.Listener() {
            @Override
            public void onParseError(LoopMeError message) {
            }
        };
        ResponseParser parser = new ResponseParser(listener, AdFormat.BANNER);
        assertNotNull(parser);
        String responseStr = "{\"script\":\"<html><head><meta charset=\\\"utf-8\\\"><meta " +
                "name=\\\"viewport\\\" content=\\\"initial-scale=1.0, user-scalable=0, " +
                "minimum-scale=1.0, maximum-scale=1.0\\\"><title>LoopMe</title></head><body>" +
                "<script>lmCampaigns = {\\\"eventLinkTemplate\\\":\\\"https://track.loopme.me/sj/tr?et=" +
                "__EVENT_TYPE__&rid=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "id=5761a2a5a4c0b0e43f010b55&name=__CUSTOM_EVENT_NAME__\\\",\\\"apiCreativesLink\\\"" +
                ":\\\"https://loopme.me/ad/jsonp?di=HdrUubFPSGijz6pYGSswxZ9hYDRjpkfr1yAyfqDOg8g&" +
                "id=5761a2a5a4c0b0e43f010b55&rid=5761a2a5a4c0b0e43f010b55&callback=__CALLBACK__&" +
                "tpl_path=https%3A%2F%2Fi.loopme.me%2Fhtml%2Fmpu_video_tpl%2Fvideo_tpl.html\\\",\\\"" +
                "requestId\\\":\\\"5761a2a5a4c0b0e43f010b55\\\",\\\"ads\\\":[\\\"5761a2a5a4c0b0e43f010b55\\\"]," +
                "\\\"package_ids\\\":[]}</script><script src=\\\"https://loopme.me/lm.bridge.js?cb=1466016421246\\\">" +
                "</script><script>if (!L || !L.bridge) {window.location = \\\"loopme://webview/fail\\\";}" +
                "</script><div id=\\\"LOOPME_widget\\\"><script id=\\\"LOOPME_script\\\" " +
                "src=\\\"https://loopme.me/lm.main.js?_=1466016421246 \\\"data-app-key\\\"=\\\"94b8115817\\\" " +
                "data-delay=\\\"2000\\\"></script></div></body></html>\",\"tracking\":{\"error\":" +
                "[\"https://loopme.me/sj/tr?et=ERROR&id=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "error_type={error_type}\"]},\"settings\":{\"format\":\"banner\",\"orientation\":" +
                "\"default\",\"token\":\"5761a2a5a4c0b0e43f010b55\",\"ad_expiry_time\":3600," +
                "\"preload25\":0," +
                "\"package_ids\":[]}}";
        AdParams params = parser.getAdParams(responseStr);
        assertNotNull(params);

        assertTrue(params.getAdFormat().equalsIgnoreCase(StaticParams.BANNER_TAG));
        assertNotNull(params.getHtml());
        assertNull(params.getAdOrientation());
        assertTrue(params.getToken().equalsIgnoreCase("5761a2a5a4c0b0e43f010b55"));
        assertFalse(params.isVideo360());
        assertEquals(params.getExpiredTime(), 3600 * 1000);
        assertNotNull(params.getPackageIds());
        assertEquals(params.getPackageIds().size(), 0);
        assertFalse(params.getPartPreload());
    }

    // ------------- html ---------------
    @Test
    public void responseHtmlAbsentTest() {

        ResponseParser.Listener listener = new ResponseParser.Listener() {
            @Override
            public void onParseError(LoopMeError message) {
            }
        };
        ResponseParser parser = new ResponseParser(listener, AdFormat.BANNER);
        assertNotNull(parser);
        String responseStr = "{\"tracking\":{\"error\":" +
                "[\"https://loopme.me/sj/tr?et=ERROR&id=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "error_type={error_type}\"]},\"settings\":{\"format\":\"banner\",\"orientation\":" +
                "\"default\",\"token\":\"5761a2a5a4c0b0e43f010b55\",\"ad_expiry_time\":3600," +
                "\"preload25\":0," +
                "\"package_ids\":[]}}";
        AdParams params = parser.getAdParams(responseStr);
        assertNotNull(params);

        assertTrue(params.getAdFormat().equalsIgnoreCase(StaticParams.BANNER_TAG));
        assertNull(params.getHtml());
        assertNull(params.getAdOrientation());
        assertTrue(params.getToken().equalsIgnoreCase("5761a2a5a4c0b0e43f010b55"));
        assertFalse(params.isVideo360());
        assertEquals(params.getExpiredTime(), 3600 * 1000);
        assertNotNull(params.getPackageIds());
        assertEquals(params.getPackageIds().size(), 0);
        assertFalse(params.getPartPreload());
    }

    // ------------ orientation --------------
    @Test
    public void responseOrientationPortraitTest() {

        ResponseParser.Listener listener = new ResponseParser.Listener() {
            @Override
            public void onParseError(LoopMeError message) {
            }
        };
        ResponseParser parser = new ResponseParser(listener, AdFormat.BANNER);
        assertNotNull(parser);
        String responseStr = "{\"script\":\"<html><head><meta charset=\\\"utf-8\\\"><meta " +
                "name=\\\"viewport\\\" content=\\\"initial-scale=1.0, user-scalable=0, " +
                "minimum-scale=1.0, maximum-scale=1.0\\\"><title>LoopMe</title></head><body>" +
                "<script>lmCampaigns = {\\\"eventLinkTemplate\\\":\\\"https://track.loopme.me/sj/tr?et=" +
                "__EVENT_TYPE__&rid=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "id=5761a2a5a4c0b0e43f010b55&name=__CUSTOM_EVENT_NAME__\\\",\\\"apiCreativesLink\\\"" +
                ":\\\"https://loopme.me/ad/jsonp?di=HdrUubFPSGijz6pYGSswxZ9hYDRjpkfr1yAyfqDOg8g&" +
                "id=5761a2a5a4c0b0e43f010b55&rid=5761a2a5a4c0b0e43f010b55&callback=__CALLBACK__&" +
                "tpl_path=https%3A%2F%2Fi.loopme.me%2Fhtml%2Fmpu_video_tpl%2Fvideo_tpl.html\\\",\\\"" +
                "requestId\\\":\\\"5761a2a5a4c0b0e43f010b55\\\",\\\"ads\\\":[\\\"5761a2a5a4c0b0e43f010b55\\\"]," +
                "\\\"package_ids\\\":[]}</script><script src=\\\"https://loopme.me/lm.bridge.js?cb=1466016421246\\\">" +
                "</script><script>if (!L || !L.bridge) {window.location = \\\"loopme://webview/fail\\\";}" +
                "</script><div id=\\\"LOOPME_widget\\\"><script id=\\\"LOOPME_script\\\" " +
                "src=\\\"https://loopme.me/lm.main.js?_=1466016421246 \\\"data-app-key\\\"=\\\"94b8115817\\\" " +
                "data-delay=\\\"2000\\\"></script></div></body></html>\",\"tracking\":{\"error\":" +
                "[\"https://loopme.me/sj/tr?et=ERROR&id=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "error_type={error_type}\"]},\"settings\":{\"format\":\"banner\",\"orientation\":" +
                "\"portrait\",\"token\":\"5761a2a5a4c0b0e43f010b55\",\"ad_expiry_time\":3600," +
                "\"preload25\":0," +
                "\"package_ids\":[]}}";
        AdParams params = parser.getAdParams(responseStr);
        assertNotNull(params);

        assertTrue(params.getAdFormat().equalsIgnoreCase(StaticParams.BANNER_TAG));
        assertNotNull(params.getHtml());
        assertTrue(params.getAdOrientation().equalsIgnoreCase("portrait"));
        assertTrue(params.getToken().equalsIgnoreCase("5761a2a5a4c0b0e43f010b55"));
        assertFalse(params.isVideo360());
        assertEquals(params.getExpiredTime(), 3600 * 1000);
        assertNotNull(params.getPackageIds());
        assertEquals(params.getPackageIds().size(), 0);
        assertFalse(params.getPartPreload());
    }

    @Test
    public void responseOrientationLandTest() {

        ResponseParser.Listener listener = new ResponseParser.Listener() {
            @Override
            public void onParseError(LoopMeError message) {
            }
        };
        ResponseParser parser = new ResponseParser(listener, AdFormat.BANNER);
        assertNotNull(parser);
        String responseStr = "{\"script\":\"<html><head><meta charset=\\\"utf-8\\\"><meta " +
                "name=\\\"viewport\\\" content=\\\"initial-scale=1.0, user-scalable=0, " +
                "minimum-scale=1.0, maximum-scale=1.0\\\"><title>LoopMe</title></head><body>" +
                "<script>lmCampaigns = {\\\"eventLinkTemplate\\\":\\\"https://track.loopme.me/sj/tr?et=" +
                "__EVENT_TYPE__&rid=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "id=5761a2a5a4c0b0e43f010b55&name=__CUSTOM_EVENT_NAME__\\\",\\\"apiCreativesLink\\\"" +
                ":\\\"https://loopme.me/ad/jsonp?di=HdrUubFPSGijz6pYGSswxZ9hYDRjpkfr1yAyfqDOg8g&" +
                "id=5761a2a5a4c0b0e43f010b55&rid=5761a2a5a4c0b0e43f010b55&callback=__CALLBACK__&" +
                "tpl_path=https%3A%2F%2Fi.loopme.me%2Fhtml%2Fmpu_video_tpl%2Fvideo_tpl.html\\\",\\\"" +
                "requestId\\\":\\\"5761a2a5a4c0b0e43f010b55\\\",\\\"ads\\\":[\\\"5761a2a5a4c0b0e43f010b55\\\"]," +
                "\\\"package_ids\\\":[]}</script><script src=\\\"https://loopme.me/lm.bridge.js?cb=1466016421246\\\">" +
                "</script><script>if (!L || !L.bridge) {window.location = \\\"loopme://webview/fail\\\";}" +
                "</script><div id=\\\"LOOPME_widget\\\"><script id=\\\"LOOPME_script\\\" " +
                "src=\\\"https://loopme.me/lm.main.js?_=1466016421246 \\\"data-app-key\\\"=\\\"94b8115817\\\" " +
                "data-delay=\\\"2000\\\"></script></div></body></html>\",\"tracking\":{\"error\":" +
                "[\"https://loopme.me/sj/tr?et=ERROR&id=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "error_type={error_type}\"]},\"settings\":{\"format\":\"banner\",\"orientation\":" +
                "\"landscape\",\"token\":\"5761a2a5a4c0b0e43f010b55\",\"ad_expiry_time\":3600," +
                "\"preload25\":0," +
                "\"package_ids\":[]}}";
        AdParams params = parser.getAdParams(responseStr);
        assertNotNull(params);

        assertTrue(params.getAdFormat().equalsIgnoreCase(StaticParams.BANNER_TAG));
        assertNotNull(params.getHtml());
        assertTrue(params.getAdOrientation().equalsIgnoreCase("landscape"));
        assertTrue(params.getToken().equalsIgnoreCase("5761a2a5a4c0b0e43f010b55"));
        assertFalse(params.isVideo360());
        assertEquals(params.getExpiredTime(), 3600 * 1000);
        assertNotNull(params.getPackageIds());
        assertEquals(params.getPackageIds().size(), 0);
        assertFalse(params.getPartPreload());
    }

    @Test
    public void responseOrientationAbsentTest() {

        ResponseParser.Listener listener = new ResponseParser.Listener() {
            @Override
            public void onParseError(LoopMeError message) {
            }
        };
        ResponseParser parser = new ResponseParser(listener, AdFormat.BANNER);
        assertNotNull(parser);
        String responseStr = "{\"script\":\"<html><head><meta charset=\\\"utf-8\\\"><meta " +
                "name=\\\"viewport\\\" content=\\\"initial-scale=1.0, user-scalable=0, " +
                "minimum-scale=1.0, maximum-scale=1.0\\\"><title>LoopMe</title></head><body>" +
                "<script>lmCampaigns = {\\\"eventLinkTemplate\\\":\\\"https://track.loopme.me/sj/tr?et=" +
                "__EVENT_TYPE__&rid=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "id=5761a2a5a4c0b0e43f010b55&name=__CUSTOM_EVENT_NAME__\\\",\\\"apiCreativesLink\\\"" +
                ":\\\"https://loopme.me/ad/jsonp?di=HdrUubFPSGijz6pYGSswxZ9hYDRjpkfr1yAyfqDOg8g&" +
                "id=5761a2a5a4c0b0e43f010b55&rid=5761a2a5a4c0b0e43f010b55&callback=__CALLBACK__&" +
                "tpl_path=https%3A%2F%2Fi.loopme.me%2Fhtml%2Fmpu_video_tpl%2Fvideo_tpl.html\\\",\\\"" +
                "requestId\\\":\\\"5761a2a5a4c0b0e43f010b55\\\",\\\"ads\\\":[\\\"5761a2a5a4c0b0e43f010b55\\\"]," +
                "\\\"package_ids\\\":[]}</script><script src=\\\"https://loopme.me/lm.bridge.js?cb=1466016421246\\\">" +
                "</script><script>if (!L || !L.bridge) {window.location = \\\"loopme://webview/fail\\\";}" +
                "</script><div id=\\\"LOOPME_widget\\\"><script id=\\\"LOOPME_script\\\" " +
                "src=\\\"https://loopme.me/lm.main.js?_=1466016421246 \\\"data-app-key\\\"=\\\"94b8115817\\\" " +
                "data-delay=\\\"2000\\\"></script></div></body></html>\",\"tracking\":{\"error\":" +
                "[\"https://loopme.me/sj/tr?et=ERROR&id=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "error_type={error_type}\"]},\"settings\":{\"format\":\"banner\"" +
                ",\"token\":\"5761a2a5a4c0b0e43f010b55\",\"ad_expiry_time\":3600," +
                "\"preload25\":0," +
                "\"package_ids\":[]}}";
        AdParams params = parser.getAdParams(responseStr);
        assertNotNull(params);

        assertTrue(params.getAdFormat().equalsIgnoreCase(StaticParams.BANNER_TAG));
        assertNotNull(params.getHtml());
        assertNull(params.getAdOrientation());
        assertTrue(params.getToken().equalsIgnoreCase("5761a2a5a4c0b0e43f010b55"));
        assertFalse(params.isVideo360());
        assertEquals(params.getExpiredTime(), 3600 * 1000);
        assertNotNull(params.getPackageIds());
        assertEquals(params.getPackageIds().size(), 0);
        assertFalse(params.getPartPreload());
    }

    // ----------------- token --------------

    @Test
    public void responseTokenAbsentTest() {

        ResponseParser.Listener listener = new ResponseParser.Listener() {
            @Override
            public void onParseError(LoopMeError message) {
            }
        };
        ResponseParser parser = new ResponseParser(listener, AdFormat.BANNER);
        assertNotNull(parser);
        String responseStr = "{\"script\":\"<html><head><meta charset=\\\"utf-8\\\"><meta " +
                "name=\\\"viewport\\\" content=\\\"initial-scale=1.0, user-scalable=0, " +
                "minimum-scale=1.0, maximum-scale=1.0\\\"><title>LoopMe</title></head><body>" +
                "<script>lmCampaigns = {\\\"eventLinkTemplate\\\":\\\"https://track.loopme.me/sj/tr?et=" +
                "__EVENT_TYPE__&rid=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "id=5761a2a5a4c0b0e43f010b55&name=__CUSTOM_EVENT_NAME__\\\",\\\"apiCreativesLink\\\"" +
                ":\\\"https://loopme.me/ad/jsonp?di=HdrUubFPSGijz6pYGSswxZ9hYDRjpkfr1yAyfqDOg8g&" +
                "id=5761a2a5a4c0b0e43f010b55&rid=5761a2a5a4c0b0e43f010b55&callback=__CALLBACK__&" +
                "tpl_path=https%3A%2F%2Fi.loopme.me%2Fhtml%2Fmpu_video_tpl%2Fvideo_tpl.html\\\",\\\"" +
                "requestId\\\":\\\"5761a2a5a4c0b0e43f010b55\\\",\\\"ads\\\":[\\\"5761a2a5a4c0b0e43f010b55\\\"]," +
                "\\\"package_ids\\\":[]}</script><script src=\\\"https://loopme.me/lm.bridge.js?cb=1466016421246\\\">" +
                "</script><script>if (!L || !L.bridge) {window.location = \\\"loopme://webview/fail\\\";}" +
                "</script><div id=\\\"LOOPME_widget\\\"><script id=\\\"LOOPME_script\\\" " +
                "src=\\\"https://loopme.me/lm.main.js?_=1466016421246 \\\"data-app-key\\\"=\\\"94b8115817\\\" " +
                "data-delay=\\\"2000\\\"></script></div></body></html>\",\"tracking\":{\"error\":" +
                "[\"https://loopme.me/sj/tr?et=ERROR&id=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "error_type={error_type}\"]},\"settings\":{\"format\":\"banner\"" +
                ",\"ad_expiry_time\":3600," +
                "\"preload25\":0," +
                "\"package_ids\":[]}}";
        AdParams params = parser.getAdParams(responseStr);
        assertNotNull(params);

        assertTrue(params.getAdFormat().equalsIgnoreCase(StaticParams.BANNER_TAG));
        assertNotNull(params.getHtml());
        assertNull(params.getAdOrientation());
        assertNull(params.getToken());
        assertFalse(params.isVideo360());
        assertEquals(params.getExpiredTime(), 3600 * 1000);
        assertNotNull(params.getPackageIds());
        assertEquals(params.getPackageIds().size(), 0);
        assertFalse(params.getPartPreload());
    }

    // --------------- package ids --------------
    @Test
    public void response3PackagesTest() {

        ResponseParser.Listener listener = new ResponseParser.Listener() {
            @Override
            public void onParseError(LoopMeError message) {
            }
        };
        ResponseParser parser = new ResponseParser(listener, AdFormat.BANNER);
        assertNotNull(parser);
        String responseStr = "{\"script\":\"<html><head><meta charset=\\\"utf-8\\\"><meta " +
                "name=\\\"viewport\\\" content=\\\"initial-scale=1.0, user-scalable=0, " +
                "minimum-scale=1.0, maximum-scale=1.0\\\"><title>LoopMe</title></head><body>" +
                "<script>lmCampaigns = {\\\"eventLinkTemplate\\\":\\\"https://track.loopme.me/sj/tr?et=" +
                "__EVENT_TYPE__&rid=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "id=5761a2a5a4c0b0e43f010b55&name=__CUSTOM_EVENT_NAME__\\\",\\\"apiCreativesLink\\\"" +
                ":\\\"https://loopme.me/ad/jsonp?di=HdrUubFPSGijz6pYGSswxZ9hYDRjpkfr1yAyfqDOg8g&" +
                "id=5761a2a5a4c0b0e43f010b55&rid=5761a2a5a4c0b0e43f010b55&callback=__CALLBACK__&" +
                "tpl_path=https%3A%2F%2Fi.loopme.me%2Fhtml%2Fmpu_video_tpl%2Fvideo_tpl.html\\\",\\\"" +
                "requestId\\\":\\\"5761a2a5a4c0b0e43f010b55\\\",\\\"ads\\\":[\\\"5761a2a5a4c0b0e43f010b55\\\"]," +
                "\\\"package_ids\\\":[]}</script><script src=\\\"https://loopme.me/lm.bridge.js?cb=1466016421246\\\">" +
                "</script><script>if (!L || !L.bridge) {window.location = \\\"loopme://webview/fail\\\";}" +
                "</script><div id=\\\"LOOPME_widget\\\"><script id=\\\"LOOPME_script\\\" " +
                "src=\\\"https://loopme.me/lm.main.js?_=1466016421246 \\\"data-app-key\\\"=\\\"94b8115817\\\" " +
                "data-delay=\\\"2000\\\"></script></div></body></html>\",\"tracking\":{\"error\":" +
                "[\"https://loopme.me/sj/tr?et=ERROR&id=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "error_type={error_type}\"]},\"settings\":{\"format\":\"banner\"" +
                ",\"ad_expiry_time\":3600," +
                "\"preload25\":0," +
                "\"package_ids\":[com.package1, com.package2, com.package3]}}";
        AdParams params = parser.getAdParams(responseStr);
        assertNotNull(params);

        assertTrue(params.getAdFormat().equalsIgnoreCase(StaticParams.BANNER_TAG));
        assertNotNull(params.getHtml());
        assertNull(params.getAdOrientation());
        assertNull(params.getToken());
        assertFalse(params.isVideo360());
        assertEquals(params.getExpiredTime(), 3600 * 1000);
        assertNotNull(params.getPackageIds());
        assertEquals(params.getPackageIds().size(), 3);
    }

    @Test
    public void response1PackageTest() {

        ResponseParser.Listener listener = new ResponseParser.Listener() {
            @Override
            public void onParseError(LoopMeError message) {
            }
        };
        ResponseParser parser = new ResponseParser(listener, AdFormat.BANNER);
        assertNotNull(parser);
        String responseStr = "{\"script\":\"<html><head><meta charset=\\\"utf-8\\\"><meta " +
                "name=\\\"viewport\\\" content=\\\"initial-scale=1.0, user-scalable=0, " +
                "minimum-scale=1.0, maximum-scale=1.0\\\"><title>LoopMe</title></head><body>" +
                "<script>lmCampaigns = {\\\"eventLinkTemplate\\\":\\\"https://track.loopme.me/sj/tr?et=" +
                "__EVENT_TYPE__&rid=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "id=5761a2a5a4c0b0e43f010b55&name=__CUSTOM_EVENT_NAME__\\\",\\\"apiCreativesLink\\\"" +
                ":\\\"https://loopme.me/ad/jsonp?di=HdrUubFPSGijz6pYGSswxZ9hYDRjpkfr1yAyfqDOg8g&" +
                "id=5761a2a5a4c0b0e43f010b55&rid=5761a2a5a4c0b0e43f010b55&callback=__CALLBACK__&" +
                "tpl_path=https%3A%2F%2Fi.loopme.me%2Fhtml%2Fmpu_video_tpl%2Fvideo_tpl.html\\\",\\\"" +
                "requestId\\\":\\\"5761a2a5a4c0b0e43f010b55\\\",\\\"ads\\\":[\\\"5761a2a5a4c0b0e43f010b55\\\"]," +
                "\\\"package_ids\\\":[]}</script><script src=\\\"https://loopme.me/lm.bridge.js?cb=1466016421246\\\">" +
                "</script><script>if (!L || !L.bridge) {window.location = \\\"loopme://webview/fail\\\";}" +
                "</script><div id=\\\"LOOPME_widget\\\"><script id=\\\"LOOPME_script\\\" " +
                "src=\\\"https://loopme.me/lm.main.js?_=1466016421246 \\\"data-app-key\\\"=\\\"94b8115817\\\" " +
                "data-delay=\\\"2000\\\"></script></div></body></html>\",\"tracking\":{\"error\":" +
                "[\"https://loopme.me/sj/tr?et=ERROR&id=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "error_type={error_type}\"]},\"settings\":{\"format\":\"banner\"" +
                ",\"ad_expiry_time\":3600," +
                "\"preload25\":0," +
                "\"package_ids\":[com.package1]}}";
        AdParams params = parser.getAdParams(responseStr);
        assertNotNull(params);

        assertTrue(params.getAdFormat().equalsIgnoreCase(StaticParams.BANNER_TAG));
        assertNotNull(params.getHtml());
        assertNull(params.getAdOrientation());
        assertNull(params.getToken());
        assertFalse(params.isVideo360());
        assertEquals(params.getExpiredTime(), 3600 * 1000);
        assertNotNull(params.getPackageIds());
        assertEquals(params.getPackageIds().size(), 1);
    }

    @Test
    public void responsePackageAbsentTest() {

        ResponseParser.Listener listener = new ResponseParser.Listener() {
            @Override
            public void onParseError(LoopMeError message) {
            }
        };
        ResponseParser parser = new ResponseParser(listener, AdFormat.BANNER);
        assertNotNull(parser);
        String responseStr = "{\"script\":\"<html><head><meta charset=\\\"utf-8\\\"><meta " +
                "name=\\\"viewport\\\" content=\\\"initial-scale=1.0, user-scalable=0, " +
                "minimum-scale=1.0, maximum-scale=1.0\\\"><title>LoopMe</title></head><body>" +
                "<script>lmCampaigns = {\\\"eventLinkTemplate\\\":\\\"https://track.loopme.me/sj/tr?et=" +
                "__EVENT_TYPE__&rid=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "id=5761a2a5a4c0b0e43f010b55&name=__CUSTOM_EVENT_NAME__\\\",\\\"apiCreativesLink\\\"" +
                ":\\\"https://loopme.me/ad/jsonp?di=HdrUubFPSGijz6pYGSswxZ9hYDRjpkfr1yAyfqDOg8g&" +
                "id=5761a2a5a4c0b0e43f010b55&rid=5761a2a5a4c0b0e43f010b55&callback=__CALLBACK__&" +
                "tpl_path=https%3A%2F%2Fi.loopme.me%2Fhtml%2Fmpu_video_tpl%2Fvideo_tpl.html\\\",\\\"" +
                "requestId\\\":\\\"5761a2a5a4c0b0e43f010b55\\\",\\\"ads\\\":[\\\"5761a2a5a4c0b0e43f010b55\\\"]," +
                "\\\"package_ids\\\":[]}</script><script src=\\\"https://loopme.me/lm.bridge.js?cb=1466016421246\\\">" +
                "</script><script>if (!L || !L.bridge) {window.location = \\\"loopme://webview/fail\\\";}" +
                "</script><div id=\\\"LOOPME_widget\\\"><script id=\\\"LOOPME_script\\\" " +
                "src=\\\"https://loopme.me/lm.main.js?_=1466016421246 \\\"data-app-key\\\"=\\\"94b8115817\\\" " +
                "data-delay=\\\"2000\\\"></script></div></body></html>\",\"tracking\":{\"error\":" +
                "[\"https://loopme.me/sj/tr?et=ERROR&id=5761a2a5a4c0b0e43f010b55&meta=Mjk1MzM6NzI4NzozZmIxMmNjYm03NDk1ZDk4&" +
                "error_type={error_type}\"]},\"settings\":{\"format\":\"banner\"" +
                ",\"ad_expiry_time\":3600," +
                "\"preload25\":0" +
                "}}";
        AdParams params = parser.getAdParams(responseStr);
        assertNotNull(params);

        assertTrue(params.getAdFormat().equalsIgnoreCase(StaticParams.BANNER_TAG));
        assertNotNull(params.getHtml());
        assertNull(params.getAdOrientation());
        assertNull(params.getToken());
        assertFalse(params.isVideo360());
        assertEquals(params.getExpiredTime(), 3600 * 1000);
        assertNotNull(params.getPackageIds());
        assertEquals(params.getPackageIds().size(), 0);
    }
}
