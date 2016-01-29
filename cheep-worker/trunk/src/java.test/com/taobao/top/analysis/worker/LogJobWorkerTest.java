/**
 * 
 */
package com.taobao.top.analysis.worker;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.top.analysis.TopAnalysisConfig;
import com.taobao.top.analysis.data.Rule;
import com.taobao.top.analysis.util.ReportUtil;

/**
 * @author fangweng
 * 
 */
public class LogJobWorkerTest
{

	static LogJobWorker logJobWorker;
	static Map<String, Map<String, Object>> resultPool;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUp() throws Exception
	{
		// TopAnalyzerConfig.getInstance().loadConfigFromFile("top-analysis.properties");

		Rule rule = new Rule();
		resultPool = new HashMap<String,Map<String,Object>>();
		TopAnalysisConfig config = new TopAnalysisConfig();

		String resource = "file:test.log";

		ReportUtil.buildReportModule("unitTest.xml", rule);

		logJobWorker = new LogJobWorker("testWorker", resource, ",", rule.getEntryPool(),
				rule.getParentEntryPool(), resultPool,rule.getAliasPool(), new CountDownLatch(1),
				new AtomicLong(0),config);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDown() throws Exception
	{
	}

	/**
	 * Test method for
	 * {@link com.taobao.top.analysis.worker.AbstractLogJobWorker#process(com.taobao.top.analysis.data.ReportEntry, java.lang.String[], java.util.Map)}
	 * .
	 */
	@Test
	public void testDoJob()
	{

		try
		{
			logJobWorker.doJob();

			Assert.assertEquals(resultPool.get("report:apiReport服务名称").values()
					.size(), 4);

			Assert.assertTrue(resultPool.get("report:apiReport服务名称").values()
					.contains("taobao.itemcats.get.v2"));

			Assert.assertTrue(resultPool.get("report:apiReport服务名称").values()
					.contains("taobao.taobaoke.items.convert"));

			Assert.assertTrue(resultPool.get("report:apiReport服务名称").values()
					.contains("taobao.taobaoke.items.get"));

			Assert.assertTrue(resultPool.get("report:apiReport服务名称").values()
					.contains("taobao.user.get"));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Assert.assertTrue(false);
		}

	}

}
