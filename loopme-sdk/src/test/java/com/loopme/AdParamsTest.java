package com.loopme;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.api.mockito.PowerMockito;

import com.loopme.AdParams;
import com.loopme.Logging;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AdParams.AdParamsBuilder.class, Logging.class})
public class AdParamsTest {
	
	@Test
	public void testAdParamsBuilderCreation() {
		PowerMockito.mockStatic(Logging.class);
		
		String format1 = "interstitial";
		String format2 = "banner";
		String format3 = "sdf";
		String format4 = "";
		String format5 = null;
		
		AdParams params1 = new AdParams.AdParamsBuilder(format1).build();
		assertTrue(params1 != null);
		
		AdParams params2 = new AdParams.AdParamsBuilder(format2).build();
		assertTrue(params2 != null);
		
		AdParams params3 = new AdParams.AdParamsBuilder(format3).build();
		assertTrue(params3 == null);
		
		AdParams params4 = new AdParams.AdParamsBuilder(format4).build();
		assertTrue(params4 == null);
		
		AdParams params5 = new AdParams.AdParamsBuilder(format5).build();
		assertTrue(params5 == null);
	}

	@Test
	public void testGetHtml() {
		PowerMockito.mockStatic(Logging.class);
		
		String html1 = "some html";
		String html2 = "";
		String html3 = null;
		
		AdParams params1 = new AdParams.AdParamsBuilder("interstitial")
			.html(html1)
			.build();
		assertEquals(html1, params1.getHtml());
		
		AdParams params2 = new AdParams.AdParamsBuilder("interstitial")
			.html(html2)
			.build();
		assertEquals(html2, params2.getHtml());
		
		AdParams params3 = new AdParams.AdParamsBuilder("interstitial")
			.html(html3)
			.build();
		assertNull(params3.getHtml());
	}

	@Test
	public void testSetGetAdFormat() {
		PowerMockito.mockStatic(Logging.class);//TODO can be moved to setup()
		
		String format1 = "interstitial";
		String format2 = "banner";
		
		AdParams params1 = new AdParams.AdParamsBuilder(format1).build();
		assertEquals(format1, params1.getAdFormat());
		
		AdParams params2 = new AdParams.AdParamsBuilder(format2).build();
		assertEquals(format2, params2.getAdFormat());
	}

	@Test
	public void testSetGetAdOrientation() {
		PowerMockito.mockStatic(Logging.class);
		
		String or1 = "landscape";
		String or2 = "portrait";
		String or3 = "p";
		String or4 = "l";
		String or5 = "asasd";
		String or6 = "";
		String or7 = null;
		
		AdParams params1 = new AdParams.AdParamsBuilder("interstitial")
			.orientation(or1).build();
		assertEquals(or1, params1.getAdOrientation());
		
		AdParams params2 = new AdParams.AdParamsBuilder("interstitial")
			.orientation(or2).build();
		assertEquals(or2, params2.getAdOrientation());
		
		AdParams params3 = new AdParams.AdParamsBuilder("interstitial")
			.orientation(or3).build();
		assertNull(params3.getAdOrientation());
		
		AdParams params4 = new AdParams.AdParamsBuilder("interstitial")
			.orientation(or4).build();
		assertNull(params4.getAdOrientation());
		
		AdParams params5 = new AdParams.AdParamsBuilder("interstitial")
			.orientation(or5).build();
		assertNull(params5.getAdOrientation());
		
		AdParams params6 = new AdParams.AdParamsBuilder("interstitial")
			.orientation(or6).build();
		assertNull(params6.getAdOrientation());
		
		AdParams params7 = new AdParams.AdParamsBuilder("interstitial")
			.orientation(or7).build();
		assertNull(params7.getAdOrientation());
	}

	@Test
	public void testGetExpiredTime() {
		PowerMockito.mockStatic(Logging.class);
		
		final int DEFAULT_EXPIRED_TIME = 1000 * 60 * 10; //10 min
		
		int time1 = -5;
		int time2 = 0;
		int time3 = 60 * 9; //9 min
		int time4 = 60 * 11; //11 min
		
		AdParams params1 = new AdParams.AdParamsBuilder("interstitial")
			.expiredTime(time1).build();
		assertEquals(DEFAULT_EXPIRED_TIME, params1.getExpiredTime());
		
		AdParams params2 = new AdParams.AdParamsBuilder("interstitial")
			.expiredTime(time2).build();
		assertEquals(DEFAULT_EXPIRED_TIME, params2.getExpiredTime());
		
		AdParams params3 = new AdParams.AdParamsBuilder("interstitial")
			.expiredTime(time3).build();
		assertEquals(DEFAULT_EXPIRED_TIME, params3.getExpiredTime());
		
		AdParams params4 = new AdParams.AdParamsBuilder("interstitial")
			.expiredTime(time4).build();
		assertEquals(time4 * 1000, params4.getExpiredTime());
	}
}
