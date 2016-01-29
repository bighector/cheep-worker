package com.taobao.top.analysis.distribute;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.top.analysis.TopAnalysisConfig;
import com.taobao.top.analysis.data.DistributeJob;
import com.taobao.top.analysis.jobmanager.DefaultAnalysisManager;
import com.taobao.top.analysis.jobmanager.DefaultJobManager;
import com.taobao.top.analysis.jobmanager.DefaultReportManager;
import com.taobao.top.analysis.jobmanager.DefaultRuleManager;
import com.taobao.top.analysis.jobmanager.IAnalysisManager;
import com.taobao.top.analysis.jobmanager.IJobManager;
import com.taobao.top.analysis.jobmanager.IReportManager;
import com.taobao.top.analysis.jobmanager.IRuleManager;
import com.taobao.top.analysis.transport.TransportManager;
import com.taobao.top.analysis.transport.impl.DefaultTransportManager;

public class IncrementalAnalysisNodeTest
{

	static IncrementalAnalysisNode distributeNode;
	static IAnalysisManager analysisManager;
	static ConcurrentMap<Integer, DistributeJob> jobs;
	static ConcurrentMap<Integer, String> jobStatusPool;
	static Map<String, Map<String, Object>> resultPool;
	static AtomicInteger completeJobCounter;
	static TransportManager defaultTransportManager;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		distributeNode = new IncrementalAnalysisNode();
		jobStatusPool = new ConcurrentHashMap<Integer, String>();
		jobs = new ConcurrentHashMap<Integer, DistributeJob>();
		resultPool = new HashMap<String, Map<String, Object>>();
		completeJobCounter = new AtomicInteger(2);
		defaultTransportManager = new DefaultTransportManager();
		defaultTransportManager.start();

		String propFile = "top-analysis-utest.properties";

		TopAnalysisConfig topAnalyzerConfig = new TopAnalysisConfig();
		topAnalyzerConfig.loadConfigFromFile(propFile);

		analysisManager = new DefaultAnalysisManager();
		analysisManager.setTopAnalyzerConfig(topAnalyzerConfig);

		IRuleManager ruleManager = new DefaultRuleManager();
		IReportManager reportManager = new DefaultReportManager();
		IJobManager jobManager = new DefaultJobManager();
		ruleManager.setTopAnalyzerConfig(topAnalyzerConfig);
		reportManager.setTopAnalyzerConfig(topAnalyzerConfig);
		jobManager.setTopAnalyzerConfig(topAnalyzerConfig);

		analysisManager.setReportManager(reportManager);
		analysisManager.setRuleManager(ruleManager);
		analysisManager.setJobManager(jobManager);

		analysisManager.init();
		analysisManager.buildRule(topAnalyzerConfig.getReportConfigs());

		distributeNode.setName("TopAnalyzerNode-InnerJobWorker");
		distributeNode.setReportJobManager(analysisManager);
		distributeNode.setJobs(jobs);
		distributeNode.setJobStatusPool(jobStatusPool);
		distributeNode.setResultPool(resultPool);
		distributeNode.setCompleteJobCounter(completeJobCounter);
		distributeNode.setDefaultTransportManager(defaultTransportManager);
		distributeNode.setTopAnalyzerConfig(topAnalyzerConfig);
	}

	@Before
	public void setUp() throws Exception
	{
		jobs.clear();
		jobStatusPool.clear();
		resultPool.clear();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		defaultTransportManager.stop();
	}

	@Test
	public void testCreateJobs()
	{

		try
		{
			distributeNode.checkAndResetJobList(jobs, jobStatusPool);

			Assert.assertEquals(jobs.size(), 1);
			Assert.assertEquals(jobStatusPool.size(), 1);

			Integer key = jobs.keySet().iterator().next();
			Assert
					.assertEquals(jobs.get(key).getJobs(),
							"http://192.168.214.211/top/services/logPull?command=pulllog&size=3000");

		}
		catch (Exception ex)
		{
			Assert.assertTrue(false);
			ex.printStackTrace();
		}

	}

	@Test
	public void testMergeJobResult()
	{
		try
		{
			resultPool.put("1", new HashMap<String, Object>());
			resultPool.get("1").put("taobao.item.get", (Double) 10.0);
			resultPool.get("1").put("taobao.user.get", (Double) 11.0);
			resultPool.get("1").put("taobao.shop.get", (Double) 2.0);

			Map<String, Map<String, Object>> res = new HashMap<String, Map<String, Object>>();
			res.put("1", new HashMap<String, Object>());
			res.get("1").put("taobao.item.get", (Double) 10.0);
			res.get("1").put("taobao.user.get", (Double) 10.0);
			res.get("1").put("taobao.shop.get", (Double) 10.0);

			DistributeJob job = new DistributeJob();
			job.setJobId(1);
			job.setJobs("test");
			job.setResults(res);

			jobs.put(job.getJobId(), job);
			jobStatusPool.put(job.getJobId(), DistributeJob.JOB_STATUS_DONE);

			distributeNode.mergeJobResult(jobs, jobStatusPool,true);
			
			resultPool = distributeNode.getResultPool();

			Assert.assertEquals(resultPool.get("1").get("taobao.item.get"),
					(Double) 20.0);
			Assert.assertEquals(resultPool.get("1").get("taobao.user.get"),
					(Double) 21.0);
			Assert.assertEquals(resultPool.get("1").get("taobao.shop.get"),
					(Double) 12.0);

			Assert.assertEquals(jobStatusPool.values().iterator().next(),
					DistributeJob.JOB_STATUS_RESULT_MERGED);
		}
		catch (Exception ex)
		{
			Assert.assertTrue(false);
			ex.printStackTrace();
		}

	}

	@Test
	public void testNeedToExportReport()
	{
		try
		{

			DistributeJob job = new DistributeJob();
			job.setJobId(1);
			job.setJobs("test");

			jobs.put(job.getJobId(), job);
			jobStatusPool.put(job.getJobId(), DistributeJob.JOB_STATUS_DONE);

			Assert
					.assertFalse(distributeNode
							.needToExportReport(jobStatusPool));

			jobStatusPool.put(job.getJobId(),
					DistributeJob.JOB_STATUS_RESULT_MERGED);

			Assert.assertTrue(distributeNode.needToExportReport(jobStatusPool));

		}
		catch (Exception ex)
		{
			Assert.assertTrue(false);
			ex.printStackTrace();
		}
	}

}
