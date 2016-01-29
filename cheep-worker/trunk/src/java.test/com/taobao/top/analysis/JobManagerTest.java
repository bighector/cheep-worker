/**
 * 
 */
package com.taobao.top.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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
import com.taobao.top.analysis.util.FileUtil;

/**
 * 用于做本地文件和配置分析
 * 
 * @author fangweng
 * 
 */
public class JobManagerTest
{
	
	
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

	
	public static void main(String[] args)
	{
		
		Map<String,String[]> t = new HashMap<String,String[]>();
		t.put("1", new String[]{"ss","bb"});
		t.put("2", new String[]{"ss","bb1"});
		
		ArrayList<String[]> t1 = new ArrayList<String[]>();
		t1.addAll(t.values());
		
		t.get("1")[0] = "s1";
		
		
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR, 11);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		System.out.println(calendar.getTimeInMillis());
	}
	
	
	@Test
	public void testJobManagerTest()
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

			config.setReportConfigs(new String[] { "top-report-period.xml","topidReport.xml","pageApi.xml","top-report-api2.xml" });

			config.setInput("input");
			config.setOutput("output");
			
			//内存有多大配置多大线程数目
			config.setAnalysisWorkNum(3);
			config.setSplitWorkerNum(5);
			config.setMaxFileBlockSize(100);
			//config.setSplitRegex("\\|");

			config.toString();

			long beg = System.currentTimeMillis();

			analysisManager.init();
			analysisManager.buildRule(config.getReportConfigs());
			
			String[] resources = FileUtil.splitDataFile("d:\\\\tradeDownloadLogFinal.log", 5);
			
			//String[] resources = new String[1];
			//String[] resources = new String[2];
//			resources[0] = "file:c:\\alimama6.cm2.perf-log.log.2010-08-11.log";
//			resources[1] = "file:c:\\alimama7.cm2.perf-log.log.2010-08-11.log";

			//resources[0] = "file:tmp1.log";

			//resources[1] = "file:C:\\log\\top014138.cm4.log";
			//resources[2] = "file:C:\\log\\top015082.cm3.log";
			//resources[1] = "file:C:\\log\\top015090.cm3.log";
			analysisManager.dispatchJobs(resources);
			Map<String, Map<String, Object>> resultPool = analysisManager
					.mergeResultPools(analysisManager.getResultPools(),
							analysisManager.getRule(null).getEntryPool(),true);

			analysisManager.exportAnalysisData(resultPool, config.getOutput()
					+ File.separator + "tmpdata" + File.separator);

			analysisManager
					.loadAnalysisData(config.getOutput() + File.separator
							+ "tmpdata" + File.separator);

			List<String> reports = analysisManager.generateReports(resultPool,
					analysisManager.getRule(null).getReportPool(), config.getOutput(), true);

			analysisManager.dispatchReports(reports, "report");

			analysisManager.checkAndRebuildRule();

			Assert.assertTrue(reports.size() > 0);

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
