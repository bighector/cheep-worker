package com.taobao.top.analysis.map;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.top.analysis.data.Alias;
import com.taobao.top.analysis.data.ReportEntry;

public class TimeMapTest
{

	static TimeMap timeMap;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		timeMap = new TimeMap();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	@Test
	public void testGenerateKey()
	{
		ReportEntry entry = new ReportEntry();
		entry.setKeys(new String[] { "1", "2" });

		long now = System.currentTimeMillis();

		Map<String, Alias> aliasPool = new HashMap<String, Alias>();
		Alias a1 = new Alias();
		a1.setKey("3");
		a1.setName("errorCode");

		Alias a2 = new Alias();
		a2.setKey("4");
		a2.setName("subErrorCode");

		aliasPool.put(a1.getName(), a1);
		aliasPool.put(a2.getName(), a2);

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, 2010);
		calendar.set(Calendar.MONTH, 2);
		calendar.set(Calendar.DAY_OF_MONTH, 12);
		calendar.set(Calendar.HOUR_OF_DAY, 8);

		String[] contents = new String[] {
				String.valueOf(calendar.getTimeInMillis()), "taobao.user.get",
				"43", "564" };

		String result = "2010-3-12 08--taobao.user.get";

		Assert.assertEquals(result, timeMap.generateKey(entry, contents,
				aliasPool,null));

	}

}
