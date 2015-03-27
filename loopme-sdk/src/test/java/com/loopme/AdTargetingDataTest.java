package com.loopme;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.api.mockito.PowerMockito;

import com.loopme.AdTargetingData;
import com.loopme.Utils;
import com.loopme.Logging;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Logging.class)
public class AdTargetingDataTest {

	@Test
	public void creationTest() {
		AdTargetingData data = new AdTargetingData();
		assertNotNull(data);
		assertNull(data.getKeywords());
		assertNull(data.getGender());
		assertEquals(data.getYob(), 0);
	}
	
	@Test
	public void testClear() {
		AdTargetingData data = new AdTargetingData();
		
		int yob = 1986;
		
		data.setGender("f");
		data.setKeywords("test");
		data.setYob(yob);
		
		assertNotNull(data.getKeywords());
		assertNotNull(data.getGender());
		assertEquals(data.getYob(), yob);
		
		data.clear();
		
		assertNull(data.getKeywords());
		assertNull(data.getGender());
		assertEquals(data.getYob(), 0);
	}

	@Test
	public void testKeywords() {
		AdTargetingData data = new AdTargetingData();
		
		String keyword1 = "keyword1";
		String keyword2 = "keyword2";
		
		data.setKeywords(keyword1);
		assertEquals(data.getKeywords(), keyword1);
		
		data.setKeywords(keyword2);
		assertEquals(data.getKeywords(), keyword2);
		
		data.setKeywords(null);
		assertEquals(data.getKeywords(), null);
	}

	@Test
	public void testYob() {
		AdTargetingData data = new AdTargetingData();
		
		int yob1 = 1986;
		int yob2 = 0;
		int yob3 = -3;
		int yob4 = 10000;
		
		data.setYob(yob1);
		assertEquals(data.getYob(), yob1);
		data.clear();
		
		data.setYob(yob2);
		assertEquals(data.getYob(), yob2);
		data.clear();
		
		data.setYob(yob3);
		assertEquals(data.getYob(), 0);
		data.clear();
		
		data.setYob(yob4);
		assertEquals(data.getYob(), 0);
		data.clear();
	}

	@Test
	public void testGender() {
		PowerMockito.mockStatic(Logging.class);
		PowerMockito.doNothing().when(Logging.class);

		AdTargetingData data = new AdTargetingData();
		
		String gender1 = "f";
		String gender2 = "m";
		String gender3 = "sdsdf";
		String gender4 = "";
		String gender5 = "34";
		String gender6 = "b";
		
		data.setGender(gender1);
		assertEquals(data.getGender(), gender1);
		data.clear();
		
		data.setGender(gender2);
		assertEquals(data.getGender(), gender2);
		data.clear();
		
		data.setGender(gender3);
		assertEquals(data.getGender(), null);
		data.clear();
	}

}
