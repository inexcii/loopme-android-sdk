package com.loopme.common;

import com.loopme.constants.AdFormat;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Logging.class})
public class AdParamsTest {

    @Test
    public void video360Test() {
        //case 1
        AdParams.AdParamsBuilder builder =
                new AdParams.AdParamsBuilder(StaticParams.BANNER_TAG);
        builder.video360(true);
        AdParams params = builder.build();
        Assert.assertTrue(params.isVideo360());

        //case 2
        AdParams.AdParamsBuilder builder2 =
                new AdParams.AdParamsBuilder(StaticParams.INTERSTITIAL_TAG);
        builder2.video360(false);
        AdParams params2 = builder2.build();
        Assert.assertFalse(params2.isVideo360());
    }

    @Test
    public void partPreloadTest() {
        //case 1
        AdParams.AdParamsBuilder builder =
                new AdParams.AdParamsBuilder(StaticParams.BANNER_TAG);
        builder.partPreload(true);
        AdParams params = builder.build();
        Assert.assertTrue(params.getPartPreload());

        //case 2
        AdParams.AdParamsBuilder builder2 =
                new AdParams.AdParamsBuilder(StaticParams.INTERSTITIAL_TAG);
        builder2.partPreload(false);
        AdParams params2 = builder2.build();
        Assert.assertFalse(params2.getPartPreload());
    }

    @Test
    public void html1Test() {
        String html = "some html";
        AdParams.AdParamsBuilder builder =
                new AdParams.AdParamsBuilder(StaticParams.BANNER_TAG);

        builder.html(html);
        AdParams params = builder.build();
        Assert.assertTrue(html.equalsIgnoreCase(params.getHtml()));
    }

    @Test
    public void htmlEmptyTest() {
        String html = "";
        AdParams.AdParamsBuilder builder =
                new AdParams.AdParamsBuilder(StaticParams.INTERSTITIAL_TAG);
        builder.html(html);
        AdParams params = builder.build();
        Assert.assertTrue(html.equalsIgnoreCase(params.getHtml()));
    }

    @Test
    public void htmlNullTest() {
        String html = null;
        AdParams.AdParamsBuilder builder =
                new AdParams.AdParamsBuilder(StaticParams.INTERSTITIAL_TAG);
        builder.html(html);
        AdParams params = builder.build();
        Assert.assertNull(params.getHtml());
    }

    @Test
    public void token1Test() {
        String token = "some token";
        AdParams.AdParamsBuilder builder =
                new AdParams.AdParamsBuilder(StaticParams.BANNER_TAG);

        builder.token(token);
        AdParams params = builder.build();
        Assert.assertTrue(token.equalsIgnoreCase(params.getToken()));
    }

    @Test
    public void tokenEmptyTest() {
        String token = "";
        AdParams.AdParamsBuilder builder =
                new AdParams.AdParamsBuilder(StaticParams.INTERSTITIAL_TAG);
        builder.token(token);
        AdParams params = builder.build();
        Assert.assertTrue(token.equalsIgnoreCase(params.getToken()));
    }

    @Test
    public void tokenNullTest() {
        String token = null;
        AdParams.AdParamsBuilder builder =
                new AdParams.AdParamsBuilder(StaticParams.INTERSTITIAL_TAG);
        builder.token(token);
        AdParams params = builder.build();
        Assert.assertNull(params.getToken());
    }

    @Test
    public void packageId1Test() {
        String package1 = "com.package1";
        String package2 = "com.package2";
        List<String> packages = new ArrayList<String>();
        packages.add(package1);
        packages.add(package2);

        AdParams.AdParamsBuilder builder =
                new AdParams.AdParamsBuilder(StaticParams.BANNER_TAG);

        builder.packageIds(packages);
        AdParams params = builder.build();
        List<String> result = params.getPackageIds();
        Assert.assertTrue(result.get(0).equalsIgnoreCase(package1));
        Assert.assertTrue(result.get(1).equalsIgnoreCase(package2));
    }

    @Test
    public void packageId2Test() {
        List<String> packages = new ArrayList<String>();

        AdParams.AdParamsBuilder builder =
                new AdParams.AdParamsBuilder(StaticParams.BANNER_TAG);

        builder.packageIds(packages);
        AdParams params = builder.build();
        List<String> result = params.getPackageIds();
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void packageId3Test() {
        AdParams.AdParamsBuilder builder =
                new AdParams.AdParamsBuilder(StaticParams.BANNER_TAG);

        builder.packageIds(null);
        AdParams params = builder.build();
        Assert.assertNull(params.getPackageIds());
    }

    @Test
    public void expiredTime1Test() {
        AdParams.AdParamsBuilder builder =
                new AdParams.AdParamsBuilder(StaticParams.BANNER_TAG);
        builder.expiredTime(3600);
        AdParams params = builder.build();
        Assert.assertEquals(params.getExpiredTime(), 3600 * 1000);
    }

    @Test
    public void expiredTime2Test() {
        AdParams.AdParamsBuilder builder =
                new AdParams.AdParamsBuilder(StaticParams.BANNER_TAG);
        builder.expiredTime(36);
        AdParams params = builder.build();
        Assert.assertEquals(params.getExpiredTime(), StaticParams.DEFAULT_EXPIRED_TIME);
    }

    @Test
    public void expiredTime3Test() {
        AdParams.AdParamsBuilder builder =
                new AdParams.AdParamsBuilder(StaticParams.BANNER_TAG);
        AdParams params = builder.build();
        Assert.assertEquals(params.getExpiredTime(), StaticParams.DEFAULT_EXPIRED_TIME);
    }

    @Test
    public void orientationPortTest() {
        AdParams.AdParamsBuilder builder =
                new AdParams.AdParamsBuilder(StaticParams.BANNER_TAG);
        builder.orientation("portrait");
        AdParams params = builder.build();
        Assert.assertEquals(params.getAdOrientation(), "portrait");
    }

    @Test
    public void orientationLandTest() {
        AdParams.AdParamsBuilder builder =
                new AdParams.AdParamsBuilder(StaticParams.BANNER_TAG);
        builder.orientation("landscape");
        AdParams params = builder.build();
        Assert.assertEquals(params.getAdOrientation(), "landscape");
    }

    @Test
    public void orientationInvalidTest() {
        AdParams.AdParamsBuilder builder =
                new AdParams.AdParamsBuilder(StaticParams.BANNER_TAG);
        builder.orientation("land");
        AdParams params = builder.build();
        Assert.assertNull(params.getAdOrientation());
    }

    @Test
    public void orientationEmptyTest() {
        AdParams.AdParamsBuilder builder =
                new AdParams.AdParamsBuilder(StaticParams.BANNER_TAG);
        AdParams params = builder.build();
        Assert.assertNull(params.getAdOrientation());
    }
}
