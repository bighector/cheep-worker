package com.taobao.top.analysis.map;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.top.analysis.data.Alias;
import com.taobao.top.analysis.data.ReportEntry;

public class MultiConditionMapTest {
	private static MultiConditionMap multiCondMap;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		multiCondMap = new MultiConditionMap();
	}
	@Test
	public void testGenerateKey() {
		ReportEntry entry = new ReportEntry();
		entry.setKeys(new String[] { "1", "2" });
		
		entry.setMapParams("com.taobao.top.analysis.map.TimeMap;" + "" +"," + TestMap.class.getName());

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

		Assert.assertEquals(result, multiCondMap.generateKey(entry, contents,
				aliasPool,null));
	}
}
class TestMap implements IReportMap{
	public TestMap(){}
	@Override
	public String generateKey(ReportEntry entry, String[] contents,
			Map<String, Alias> aliasPool,String tempMapParams) {
		return "ok";
	}
	
}