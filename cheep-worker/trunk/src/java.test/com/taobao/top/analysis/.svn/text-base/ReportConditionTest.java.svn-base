package com.taobao.top.analysis;

import java.io.File;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.taobao.top.analysis.jobmanager.DefaultAnalysisManager;
import com.taobao.top.analysis.jobmanager.DefaultJobManager;
import com.taobao.top.analysis.jobmanager.DefaultReportManager;
import com.taobao.top.analysis.jobmanager.DefaultRuleManager;
import com.taobao.top.analysis.jobmanager.IAnalysisManager;
import com.taobao.top.analysis.jobmanager.IJobManager;
import com.taobao.top.analysis.jobmanager.IReportManager;
import com.taobao.top.analysis.jobmanager.IRuleManager;

public class ReportConditionTest {
	
	@Before
	public void setUp() throws Exception
	{

		File output = new File("output");

		if (output.exists() && output.isDirectory())
			deleteDir(new File("output"));

		new File("output").mkdir();

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{

	}

	@Test
	public void testReportCondition()
	{
		
		IAnalysisManager analysisManager = null;

		try
		{
			analysisManager = new DefaultAnalysisManager();
			TopAnalysisConfig config = new TopAnalysisConfig();
			analysisManager.setTopAnalyzerConfig(config);

			IRuleManager ruleManager = new DefaultRuleManager();
			IReportManager reportManager = new DefaultReportManager();
			IJobManager jobManager = new DefaultJobManager();

			analysisManager.setReportManager(reportManager);
			analysisManager.setRuleManager(ruleManager);
			analysisManager.setJobManager(jobManager);
			ruleManager.setTopAnalyzerConfig(config);
			reportManager.setTopAnalyzerConfig(config);
			jobManager.setTopAnalyzerConfig(config);

			config.setReportConfigs(new String[] { "unitTest4.xml" });
			config.setInput("input");
			config.setOutput("output");
			config.setAnalysisWorkNum(30);
			config.setSplitWorkerNum(5);
			config.setMaxFileBlockSize(100);

			config.toString();

			long beg = System.currentTimeMillis();

			analysisManager.init();
			analysisManager.buildRule(config.getReportConfigs());
			String[] resources = new String[1];
			resources[0] = "file:test-report-condition.log";
			analysisManager.dispatchJobs(resources);
			Map<String, Map<String, Object>> resultPool = analysisManager
					.mergeResultPools(analysisManager.getResultPools(),
							analysisManager.getRule(null).getEntryPool(),true);

			analysisManager.exportAnalysisData(resultPool, config.getOutput()
					+ File.separator + "tmpdata" + File.separator);

			Assert.assertTrue(resultPool.get("apiReport_3") != null);
			Assert.assertTrue(((Double)resultPool.get("apiReport_3").get("GLOBAL_KEY")).equals(Double.valueOf(7777)));
			Assert.assertTrue(resultPool.get("apiReport_4") != null);
			Assert.assertTrue(((Double)resultPool.get("apiReport_4").get("GLOBAL_KEY")).equals(Double.valueOf(15554)));
			
			analysisManager.exportAnalysisData(resultPool, config.getOutput()
					+ File.separator + "tmpdata" + File.separator);

			analysisManager
					.loadAnalysisData(config.getOutput() + File.separator
							+ "tmpdata" + File.separator);

			List<String> reports = analysisManager.generateReports(resultPool,
					analysisManager.getRule(null).getReportPool(), config.getOutput(), true);

			analysisManager.dispatchReports(reports, "report");

			analysisManager.checkAndRebuildRule();
			
			System.out.println("Report Analysis consume : "
					+ ((System.currentTimeMillis() - beg) / 1000) + "s");
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Assert.assertTrue(false);
		}
		finally
		{
			if (analysisManager != null)
				analysisManager.destory();
		}
	}

	public static boolean deleteDir(File dir)
	{
		if (dir.isDirectory())
		{
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++)
			{
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success)
				{
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}

}
