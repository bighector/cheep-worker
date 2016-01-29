package com.taobao.top.analysis.jobmanager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.top.analysis.TopAnalysisConfig;
import com.taobao.top.analysis.data.Report;
import com.taobao.top.analysis.data.ReportAlert;
import com.taobao.top.analysis.data.ReportEntry;
import com.taobao.top.analysis.data.Rule;
import com.taobao.top.analysis.util.AnalyzerFilenameFilter;
import com.taobao.top.analysis.util.ReportUtil;
import com.taobao.top.analysis.worker.IWorker;
import com.taobao.top.analysis.worker.LogJobWorker;

/**
 * 报表任务处理管理，增量式任务，可以支持ftp文件增量，机器文件增量
 * 
 * @author fangweng
 * 
 */
public class DefaultAnalysisManager implements IAnalysisManager
{
	private final Log logger = LogFactory.getLog(DefaultAnalysisManager.class);

	/**
	 * 任务执行线程池
	 */
	protected ExecutorService jobExecuter;

	/**
	 * 分析结果集
	 */
	protected Map<String, Map<String, Object>>[] resultPools;
	/**
	 * 任务结束汇总阀门
	 */
	protected CountDownLatch countDownLatch;

	/**
	 * 分析中的错误记录
	 */
	protected AtomicLong errorCounter;
	/**
	 * 配置文件
	 */
	protected String propFile;
	/**
	 * 全局配置
	 */
	protected TopAnalysisConfig topAnalyzerConfig;

	/**
	 * 任务管理实现
	 */
	IJobManager jobManager;

	/**
	 * 规则管理实现
	 */
	IRuleManager ruleManager;

	/**
	 * 报表管理实现
	 */
	IReportManager reportManager;
	
	

	public IJobManager getJobManager()
	{
		return jobManager;
	}

	public IRuleManager getRuleManager()
	{
		return ruleManager;
	}

	public IReportManager getReportManager()
	{
		return reportManager;
	}

	public void setTopAnalyzerConfig(TopAnalysisConfig topAnalyzerConfig)
	{
		this.topAnalyzerConfig = topAnalyzerConfig;
	}

	public void setJobManager(IJobManager jobManager)
	{
		this.jobManager = jobManager;
	}

	public void setRuleManager(IRuleManager ruleManager)
	{
		this.ruleManager = ruleManager;
	}

	public void setReportManager(IReportManager reportManager)
	{
		this.reportManager = reportManager;
	}

	public Map<String, Map<String, Object>>[] getResultPools()
	{
		return resultPools;
	}

	@Override
	public void init()
	{

		if (jobExecuter != null)
		{
			jobExecuter.shutdown();
			jobExecuter = null;
		}

		jobExecuter = Executors.newFixedThreadPool(topAnalyzerConfig
				.getAnalysisWorkNum());
		errorCounter = new AtomicLong(0);

		logger.info("JobManager init end...");
	}

	/**
	 * 支持拖拉数据，ftp代表从ftp上获取，machine代表从服务器上增量获取日志信息
	 * 
	 * @return
	 */
	public List<String> getJobs()
	{
		return jobManager.getJobs();
	}

	public String[] pullJobData(String resources)
	{
		return jobManager.pullJobData(resources);
	}

	public boolean deleteJobData(String[] resource)
	{

		return jobManager.deleteJobData(resource);
	}

	public void buildRule(String[] configFiles)
	{
		ruleManager.buildRule(configFiles);
	}

	public void checkAndRebuildRule()
	{
		ruleManager.checkAndRebuildRule();
	}

	public void dispatchReports(List<String> reports, String info)
	{
		reportManager.dispatchReports(reports, info);
	}

	public void dispatchAlerts(List<String> alerts)
	{
		reportManager.dispatchAlerts(alerts);

	}

	public List<String> generateAlerts(Map<String, Report> reportPool,
			List<ReportAlert> alerts, String dir)
	{
		return reportManager.generateAlerts(reportPool, alerts, dir);
	}

	@SuppressWarnings("unchecked")
	public void addResultPools(Map<String, Map<String, Object>>[] pools)
	{
		if (pools != null && pools.length > 0)
		{
			if (this.resultPools == null
					|| (this.resultPools != null && this.resultPools.length == 0))
				this.resultPools = pools;
			else
			{
				Map<String, Map<String, Object>>[] newResultPools = new Map[pools.length
						+ this.resultPools.length];

				System.arraycopy(this.resultPools, 0, newResultPools, 0,
						this.resultPools.length);
				System.arraycopy(pools, 0, newResultPools,
						this.resultPools.length, pools.length);

				this.resultPools = newResultPools;
			}

		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void dispatchJobs(String[] resources)
	{

		logger.info("JobManager start dispatchJobs...");
		long start = System.currentTimeMillis();

		try
		{
			int workerSize = resources.length;

			resultPools = new HashMap[workerSize];
			countDownLatch = new CountDownLatch(workerSize);

			for (int i = 0; i < workerSize; i++)
			{
				resultPools[i] = new HashMap<String, Map<String, Object>>();

				IWorker worker;

				worker = new LogJobWorker(new StringBuilder("worker").append(i)
						.toString(), resources[i], topAnalyzerConfig
						.getSplitRegex(), getRule(null).getEntryPool(), getRule(null).getParentEntryPool(),
						resultPools[i], getRule(null).getAliasPool(), countDownLatch,
						errorCounter,topAnalyzerConfig);

				jobExecuter.execute(worker);
			}

			countDownLatch.await(60, TimeUnit.MINUTES);

			if (logger.isWarnEnabled() && resources != null)
				logger.warn(new StringBuilder(
						"All Worker end process, wating for merge result")
						.append(", time consume: ").append(
								(System.currentTimeMillis() - start) / 1000)
						.append(", errorCounter: ").append(errorCounter.get())
						.toString());

		}
		catch (Exception ex)
		{
			logger.error(ex, ex);
		}
	}

	@Override
	public void exportAnalysisData(
			Map<String, Map<String, Object>> resultPools, String destDir)
	{
		if (resultPools != null && resultPools.size() > 0)
		{
			ObjectOutputStream objOutStream = null;
			
			logger.warn("start to export data...");

			try
			{
				File dest = new File(destDir);

				if (!dest.exists() || (dest.exists() && !dest.isDirectory()))
					dest.mkdirs();

				Calendar calendar = Calendar.getInstance();

				String dir = new File(destDir).getAbsolutePath();

				if (!dir.endsWith(File.separator))
				{
					dir = new StringBuilder(dir).append(File.separatorChar)
							.toString();
				}

				String destfile = new StringBuilder(dir).append(
						calendar.get(Calendar.YEAR)).append("-").append(
						String.valueOf(calendar.get(Calendar.MONTH) + 1))
						.append("-")
						.append(calendar.get(Calendar.DAY_OF_MONTH))
						.append("-").append(
								InetAddress.getLocalHost().getHostAddress())
						.append(".adata").toString();

				new File(destfile).createNewFile();

				Deflater def = new Deflater(Deflater.BEST_SPEED,false);
				DeflaterOutputStream deflaterOutputStream = 
					new DeflaterOutputStream(new FileOutputStream(destfile),def);
				objOutStream = new ObjectOutputStream(new BufferedOutputStream(deflaterOutputStream));

				objOutStream.writeObject(resultPools);
				
				logger.warn("export data end .");

			}
			catch (Exception ex)
			{
				logger.error(ex, ex);
			}
			finally
			{
				if (objOutStream != null)
				{
					try
					{
						objOutStream.close();
					}
					catch (IOException e)
					{
						logger.error(e, e);
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Map<String, Object>>[] loadAnalysisData(String destDir)
	{
		ObjectInputStream objInputStream = null;
		Queue<Map<String, Map<String, Object>>> results = new LinkedList<Map<String, Map<String, Object>>>();

		try
		{
			File dest = new File(destDir);

			if (!dest.exists() || (dest.exists() && !dest.isDirectory()))
				return null;

			File[] files = dest.listFiles(new AnalyzerFilenameFilter(".adata"));

			// 当天的备份数据
			Calendar calendar = Calendar.getInstance();
			String prefix = new StringBuilder().append(
					calendar.get(Calendar.YEAR)).append("-").append(
					String.valueOf(calendar.get(Calendar.MONTH) + 1)).append(
					"-").append(calendar.get(Calendar.DAY_OF_MONTH))
					.append("-").toString();

			for (File f : files)
			{
				if (!f.getName().startsWith(prefix))
					continue;

				InflaterInputStream inflaterInputStream = new InflaterInputStream(new FileInputStream(f));
				objInputStream = new ObjectInputStream(new BufferedInputStream(inflaterInputStream));
				Map<String, Map<String, Object>> resultPools = (Map<String, Map<String, Object>>) objInputStream
						.readObject();

				if (resultPools != null && resultPools.size() > 0)
				{
					results.add(resultPools);
				}
			}

		}
		catch (Exception ex)
		{
			logger.error(ex, ex);
		}
		finally
		{
			if (objInputStream != null)
			{
				try
				{
					objInputStream.close();
				}
				catch (IOException e)
				{
					logger.error(e, e);
				}
			}
		}

		if (results.size() > 0)
			return (Map<String, Map<String, Object>>[]) results
					.toArray(new Map[results.size()]);
		else
			return null;
	}

	@Override
	public Map<String, Map<String, Object>> mergeResultPools(
			Map<String, Map<String, Object>>[] resultPools,
			Map<String, ReportEntry> entryPool,boolean needMergeLazy)
	{

		long start = System.currentTimeMillis();

		Map<String, Map<String, Object>> result = ReportUtil.mergeResultPools(
				resultPools, entryPool,needMergeLazy);

		if (logger.isInfoEnabled())
			logger.info(new StringBuilder(
					"All result merge end, wating for generate report").append(
					", time consume: ").append(
					(System.currentTimeMillis() - start) / 1000).toString());

		return result;
	}

	public List<String> generateReports(
			Map<String, Map<String, Object>> resultPool,
			Map<String, Report> reportPool, String dir, boolean needTimeSuffix)
	{
		return reportManager.generateReports(resultPool, reportPool, dir,
				needTimeSuffix);
	}

	public void reset(boolean needReconfig)
	{
		if (needReconfig)
		{
			getRule(null).clear();
			errorCounter = new AtomicLong(0);
		}

		if (resultPools != null && resultPools.length > 0)
		{
			for (Map<String, Map<String, Object>> result : resultPools)
			{
				if (result != null)
				{
					Iterator<String> iter = result.keySet().iterator();

					while (iter.hasNext())
					{
						result.get(iter.next()).clear();
					}
				}

				result.clear();
			}

			resultPools = null;
		}
	}

	@Override
	public void destory()
	{
		if (jobExecuter != null)
		{
			if (!jobExecuter.isShutdown())
				jobExecuter.shutdown();
		}

		if (jobManager != null)
		{
			jobManager.destory();
		}

		if (ruleManager != null)
		{
			ruleManager.destory();
		}

		if (reportManager != null)
		{
			reportManager.destory();
		}

		reset(true);
	}
	
	/**
	 * 取得最新的配置数据 add by fangliang 2010-05-20
	 * @return
	 */
	public Rule getRule(String domain){
		return ruleManager.getRule(domain);
	}

	@Override
	public boolean isNeedRebuildRule(long version) {
		return ruleManager.isNeedRebuildRule(version);
	}

	@Override
	public void rebuildSlaveRule(Rule ruleData) {
		ruleManager.rebuildSlaveRule(ruleData);
	}

}
