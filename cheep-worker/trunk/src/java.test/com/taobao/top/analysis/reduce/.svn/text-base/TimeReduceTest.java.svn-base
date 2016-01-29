package com.taobao.top.analysis.reduce;

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

public class TimeReduceTest
{

	static TimeReduce timeReduce;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		timeReduce = new TimeReduce();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	@Test
	public void testGenerateValue()
	{
		ReportEntry entry = new ReportEntry();
		entry.setKeys(new String[] { "1", "2" });

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, 2010);
		calendar.set(Calendar.MONTH, 10);
		calendar.set(Calendar.DAY_OF_MONTH, 20);
		calendar.set(Calendar.HOUR_OF_DAY, 10);
		calendar.set(Calendar.MINUTE, 10);

		Map<String, Alias> aliasPool = new HashMap<String, Alias>();
		Alias a1 = new Alias();
		a1.setKey("3");
		a1.setName("errorCode");

		Alias a2 = new Alias();
		a2.setKey("4");
		a2.setName("subErrorCode");

		aliasPool.put(a1.getName(), a1);
		aliasPool.put(a2.getName(), a2);

		String[] contents = new String[] {
				String.valueOf(calendar.getTimeInMillis()), "taobao.user.get",
				"43", "564" };

		String result;

		result = new StringBuilder().append(calendar.get(Calendar.YEAR))
				.append("-").append(calendar.get(Calendar.MONTH) + 1).append(
						"-").append(calendar.get(Calendar.DAY_OF_MONTH))
				.append(" ").append("10").toString();

		Assert.assertEquals(result, timeReduce.generateValue(entry, contents,
				aliasPool));

		entry.setReduceParams("minute:10");

		result = new StringBuilder().append(calendar.get(Calendar.YEAR))
				.append("-").append(calendar.get(Calendar.MONTH) + 1).append(
						"-").append(calendar.get(Calendar.DAY_OF_MONTH))
				.append(" ").append(calendar.get(Calendar.HOUR_OF_DAY)).append(
						":10").toString();

		Assert.assertEquals(result, timeReduce.generateValue(entry, contents,
				aliasPool));

		entry.setReduceParams("day");

		result = new StringBuilder().append(calendar.get(Calendar.YEAR))
				.append("-").append(calendar.get(Calendar.MONTH) + 1).append(
						"-").append(calendar.get(Calendar.DAY_OF_MONTH))
				.toString();

		Assert.assertEquals(result, timeReduce.generateValue(entry, contents,
				aliasPool));

	}

}
