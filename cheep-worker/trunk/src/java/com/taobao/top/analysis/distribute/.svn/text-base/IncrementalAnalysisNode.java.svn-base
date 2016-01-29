/**
 * 
 */
package com.taobao.top.analysis.distribute;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.top.analysis.AnalysisConstants;
import com.taobao.top.analysis.TopAnalysisConfig;
import com.taobao.top.analysis.data.DistributeJob;
import com.taobao.top.analysis.data.MergedJobResult;
import com.taobao.top.analysis.data.Rule;
import com.taobao.top.analysis.jobmanager.IAnalysisManager;
import com.taobao.top.analysis.transport.BasePacket;
import com.taobao.top.analysis.transport.Connection;
import com.taobao.top.analysis.transport.TransportManager;
import com.taobao.top.analysis.util.NamedThreadFactory;

/**
 * 
 * 增量分布式节点，内部有作为server和slave的两部分实现。
 * 
 * @author fangweng
 * 
 */
public class IncrementalAnalysisNode extends Thread implements IDistributedNode
{

	private static final Log logger = LogFactory.getLog(IncrementalAnalysisNode.class);
	private static final Log perf_logger = LogFactory.getLog("performance");

	IAnalysisManager analysisManager;

	/**
	 * 如果是slave，需要客户端来发送数据
	 */
	TransportManager defaultTransportManager;

	boolean runningFlag = true;
	/**
	 * 任务创建的时间
	 */
	long jobCreateTime = 0;
	/**
	 * 任务执行列表
	 */
	ConcurrentMap<Integer, DistributeJob> jobs;
	/**
	 * 任务状态池
	 */
	ConcurrentMap<Integer, String> jobStatusPool;
	/**
	 * 分析的结果缓存
	 */
	Map<String, Map<String, Object>> resultPool;

	/**
	 * 报表是否输出
	 */
	boolean isDone = false;

	/**
	 * 客户端已经执行过的任务数，不一定被合并
	 */
	AtomicInteger completeJobCounter;
	
	/**
	 * 总任务数
	 */
	AtomicInteger totalJobCounter = new AtomicInteger(0);
	
	/**
	 * 被合并的任务数
	 */
	AtomicInteger mergedJobCounter = new AtomicInteger(0);
	
	/**
	 * 记录运行期执行非主干合并的线程数
	 */
	AtomicInteger currentTmpJobMergeCounter = new AtomicInteger(0);
	
	
	/**
	 * 一轮总共消耗在merge上的时间
	 */
	AtomicLong totalMergeConsumeTime = new AtomicLong(0);

	/**
	 * 全局配置
	 */
	protected TopAnalysisConfig topAnalyzerConfig;
	
	/**
	 * 最后一次进入分布式节点处理的时间，用于输出跨天数据和其他跨天判断操作
	 */
	long lastRuntime = 0;
	
	/**
	 * 最后一次导出数据的时间，默认10分钟导出一次
	 */
	long lastExportTmpFileTime = 0;
	
	/**
	 * 用于合并结果时同步最后的结果集
	 */
	ReentrantLock mergeLock = new ReentrantLock();
	
	/**
	 * master用于合并结果集的线程池
	 */
	ThreadPoolExecutor mergeJobResultThreadPool;

	/**
	 * 未何并的中间结果
	 */
	BlockingQueue<MergedJobResult> unMergedResultQueue 
				= new LinkedBlockingQueue<MergedJobResult>();
	
	
	@Override
	public void setTopAnalyzerConfig(TopAnalysisConfig topAnalyzerConfig)
	{
		this.topAnalyzerConfig = topAnalyzerConfig;
	}

	public AtomicInteger getTotalJobCounter() {
		return totalJobCounter;
	}



	public void setTotalJobCounter(AtomicInteger totalJobCounter) {
		this.totalJobCounter = totalJobCounter;
	}



	public AtomicInteger getCompleteJobCounter()
	{
		return completeJobCounter;
	}

	public void setCompleteJobCounter(AtomicInteger completeJobCounter)
	{
		this.completeJobCounter = completeJobCounter;
	}

	public Map<String, Map<String, Object>> getResultPool()
	{
		return resultPool;
	}

	public void setResultPool(Map<String, Map<String, Object>> resultPool)
	{
		this.resultPool = resultPool;
	}

	public ConcurrentMap<Integer, DistributeJob> getJobs()
	{
		return jobs;
	}

	public void setJobs(ConcurrentMap<Integer, DistributeJob> jobs)
	{
		this.jobs = jobs;
	}

	public ConcurrentMap<Integer, String> getJobStatusPool()
	{
		return jobStatusPool;
	}

	public void setJobStatusPool(ConcurrentMap<Integer, String> jobStatusPool)
	{
		this.jobStatusPool = jobStatusPool;
	}

	public IAnalysisManager getReportJobManager()
	{
		return analysisManager;
	}

	public void setReportJobManager(IAnalysisManager reportJobManager)
	{
		this.analysisManager = reportJobManager;
	}

	public TransportManager getDefaultTransportManager()
	{
		return defaultTransportManager;
	}

	public void setDefaultTransportManager(
			TransportManager defaultTransportManager)
	{
		this.defaultTransportManager = defaultTransportManager;
	}

	public void stopThread()
	{
		runningFlag = false;
		this.interrupt();
	}

	/*
	 * 判断是否需要导出结果
	 * 
	 * @param 任务执行状态池
	 */
	public boolean needToExportReport(Map<Integer, String> jobstatusPool)
	{
		
		if (mergedJobCounter.get() == 0)
			return false;
		
		return mergedJobCounter.get() >= totalJobCounter.get();//由于可能任务会被反复放到中间结果队列，因此数值为大于等于
		
	}

	@Override
	public void doAsMaster()
	{

		// 增量分析结束的工作，当前以天为单位,默认写死中国时区
		long post = (System.currentTimeMillis() + 8*60*60*1000)/86400000;		
		
		if (lastRuntime == 0)
			lastRuntime = post;

		if (post != lastRuntime && isDone)
		{
			// 如果是增量分析的情况，隔天情况发生需要发送报表
			if (topAnalyzerConfig.getJobFileFrom().equals(
					AnalysisConstants.JOBFILEFROM_MACHINE))
			{
				deliverReport(false);
				resultPool.clear();
				
				resetJobList(jobs, jobStatusPool);
			}
			
			lastRuntime = post;
		}
		
		
		
		// 判断是否重置了任务
		boolean checkResult = checkAndResetJobList(jobs, jobStatusPool);

		// 合并结果
		if (!isDone)
		{			
			mergeJobResult(jobs, jobStatusPool,topAnalyzerConfig.isNeedBlockToMergeResult());
		}
			

		// 判断是否需要导出数据到报表,和保存中间结果
		if (!isDone && needToExportReport(jobStatusPool))
		{
			logger.error("merge result consume :" + totalMergeConsumeTime.get());
			perf_logger.error("master merge,0,0," + totalMergeConsumeTime.get());
			
			logger.error("start generage report.");
			
			analysisManager.generateReports(resultPool, analysisManager.getRule(null)
					.getReportPool(), topAnalyzerConfig.getOutput(), false);
			
			logger.error("end generage report.");

			isDone = true;

			try
			{
				if (lastExportTmpFileTime == 0 || 
						System.currentTimeMillis() - lastExportTmpFileTime > 10 * 60 *1000)
				{
					// 导出中间结果,没有做任何并发保护
					analysisManager.exportAnalysisData(resultPool,
							topAnalyzerConfig.getOutput() + File.separator + "tmp"
									+ File.separator);
					
					lastExportTmpFileTime = System.currentTimeMillis();
				}
			}
			catch (Exception ex)
			{
				logger.error(ex, ex);
			}

			// 如果是ftp方式，完成以后即发送报表
			if (topAnalyzerConfig.getJobFileFrom().equals(
					AnalysisConstants.JOBFILEFROM_FTP))
			{
				deliverReport(false);

				resultPool.clear();

				// 删除ftp上的数据.
				if (topAnalyzerConfig.isDeleteLogFile())
				{
					String[] deleteJobs = new String[jobs.size()];
					Iterator<DistributeJob> jobIter = jobs.values().iterator();

					int i = 0;

					while (jobIter.hasNext())
					{
						deleteJobs[i] = jobIter.next().getJobs();
						i += 1;
					}

					analysisManager.deleteJobData(deleteJobs);
				}
			}

		}

		// 没有任务重新分配同时任务也都已经完成，则休息一会儿
		if (!checkResult && isDone)
		{
			try
			{
				Thread.sleep(1000 * 5);
			}
			catch (InterruptedException e)
			{
				logger.error(e, e);
			}
		}

	}

	/**
	 * 发送结果
	 * 
	 * @param 是否需要删除发送后的邮件
	 */
	private void deliverReport(boolean needDeleteReportFile)
	{
		try
		{
			List<String> reports = analysisManager.generateReports(resultPool,
					analysisManager.getRule(null).getReportPool(), topAnalyzerConfig
							.getOutput(), true);

			List<String> alerts = analysisManager.generateAlerts(
					analysisManager.getRule(null).getReportPool(), analysisManager
					.getRule(null).getAlerts(), topAnalyzerConfig.getOutput());

			if (alerts.size() > 0)
				analysisManager.dispatchAlerts(alerts);

			// 日志记录一下处理情况
			Iterator<DistributeJob> jobIter = jobs.values().iterator();
			Map<String, String> workerIps = new HashMap<String, String>();

			long consume = 0;

			// 统计执行时间
			while (jobIter.hasNext())
			{
				DistributeJob myjob = jobIter.next();
				workerIps.put(myjob.getWorkerIp(), myjob.getWorkerIp());
				consume += myjob.getEndTime() - myjob.getStartTime();
				logger.info(myjob);
			}

			logger
					.error(new StringBuilder()
							.append(
									"TopAnalysis(Map-Reduce Version) analysis end, total consume:")
							.append(
									(System.currentTimeMillis() - jobCreateTime)
											/ (1000 * 60)).append(" minutes,")
							.append(" workers count : ").append(
									workerIps.size()).toString());

			analysisManager
					.dispatchReports(
							reports,
							new StringBuilder()
									.append(
											"TopAnalysis(Map-Reduce Version) analysis end, total consume:")
									.append(consume / (1000 * 60)).append(
											" minutes,").append(
											" workers count : ").append(
											workerIps.size()).toString());
			
			
			if (needDeleteReportFile)
			{
				if (reports != null && reports.size() > 0)
				{
					for(String report : reports)
					{
						new File(report).delete();
					}
				}
				
				if (alerts != null && alerts.size() > 0)
				{
					for(String alert : alerts)
					{
						new File(alert).delete();
					}
				}
			}
		}
		catch (Exception ex)
		{
			logger.error(ex, ex);
		}
		finally
		{
			logger.error("end send alert and report...");
		}
	}
	/**
	 * 判断是否需要创建任务
	 * 
	 * @param jobs
	 * @param jobstatusPool
	 */
	protected boolean checkAndResetJobList(Map<Integer, DistributeJob> jobs,
			Map<Integer, String> jobStatusPool)
	{
		boolean result = false;

		// 表是创建任务需要和时间相关
		if (topAnalyzerConfig.getDoJobHourOfDay() >= 0)
		{
			//int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
			int hour = (int)(System.currentTimeMillis()/3600000 % 24 + 8);

			if (topAnalyzerConfig.getDoJobHourOfDay() > hour)
			{
				// 这里线程休息10分钟，减少频繁循环
				try
				{
					Thread.sleep(600000);
				}
				catch (InterruptedException e)
				{
					logger.error(e, e);
				}

				return result;
			}
		}

		// 从未创建过任务
		if (jobCreateTime == 0)
		{
			resetJobList(jobs, jobStatusPool);
			result = true;
		}
		else
		{
			// 没有设置周期重置,跨天重置一次
			if (topAnalyzerConfig.getJobResetTime() <= 0)
			{
				
				Calendar calendar = Calendar.getInstance();
				
				int day = calendar.get(Calendar.DAY_OF_MONTH);
				
				calendar.setTimeInMillis(jobCreateTime);
				
				if (day != calendar.get(Calendar.DAY_OF_MONTH))
				{
					resetJobList(jobs, jobStatusPool);
					result = true;
				}
				
				return result;
			}
				

			if (System.currentTimeMillis() - jobCreateTime >= topAnalyzerConfig
					.getJobResetTime() * 1000 * 60)
			{
				// 判断如果没有输出成功，表示任务在指定的间隔时间内无法完成,2倍间隔时间未完成，重置任务
				if (!isDone
						&& System.currentTimeMillis() - jobCreateTime < topAnalyzerConfig
								.getJobResetTime() * 1000 * 60 * 2)
				{
					logger
							.error("It's time to reset Job, but jobs not complete...");
					try
					{
						Thread.sleep(3000);
					}
					catch (InterruptedException e)
					{
						logger.error(e, e);
					}
				}
				else
				{
					logger.warn("reset job ...");
					
					if (!isDone)
					{
						boolean gotIt = false;
						
						try
						{
							gotIt = mergeLock.tryLock(3, TimeUnit.MINUTES);
							
							// 导出中间结果
							if(gotIt)
								analysisManager.exportAnalysisData(resultPool,
									topAnalyzerConfig.getOutput() + File.separator + "tmp"
											+ File.separator);
							else
							{
								logger.error("can't got merge lock,so adata not export");
							}
						}
						catch (Exception ex)
						{
							logger.error(ex, ex);
						}
						finally
						{
							if (gotIt)
								mergeLock.unlock();
						}
					}
					
					resetJobList(jobs, jobStatusPool);
					result = true;
				}
			}
		}

		return result;

	}

	/**
	 * 重置任务
	 * 
	 * @param jobs
	 * @param jobStatusPool
	 */
	private void resetJobList(Map<Integer, DistributeJob> jobs,
			Map<Integer, String> jobStatusPool)
	{
		List<String> _jobs = analysisManager.getJobs();

		jobs.clear();
		jobStatusPool.clear();
		unMergedResultQueue.clear();

		completeJobCounter.set(0);
		totalJobCounter.set(0);
		mergedJobCounter.set(0);
		currentTmpJobMergeCounter.set(0);
		totalMergeConsumeTime.set(0);
		isDone = false;
		jobCreateTime = System.currentTimeMillis();

		// 创建随机数，为了避免两次相近分配的任务会有重叠
		Random randomGenerator = new Random();
		int randomInt = randomGenerator.nextInt(1000);

		if (_jobs != null && _jobs.size() > 0)
		{
			int i = 0;

			for (String j : _jobs)
			{
				DistributeJob job = new DistributeJob();
				job.setJobId(i + randomInt);
				job.setJobs(j);
				job.setRuleData(analysisManager.getRule(null));
				jobs.put(job.getJobId(), job);
				jobStatusPool
						.put(job.getJobId(), DistributeJob.JOB_STATUS_UNDO);
				i++;
			}

			totalJobCounter.set(i);
			
			logger.error("total job " + i);
		}
		else
			logger.error("today jobs is null,please check config!");
	}

	/**
	 * 合并处理结果
	 */
	protected void mergeJobResult(
			ConcurrentMap<Integer, DistributeJob> jobs,
			ConcurrentMap<Integer, String>jobstatusPool,boolean isblockMode)
	{

		if (jobs == null || totalJobCounter.get() == 0 
				|| jobstatusPool == null || mergedJobCounter.get() >= totalJobCounter.get())
			return;

		// 检查job列表
		List<Map<String, Map<String, Object>>> mergeResults = new ArrayList<Map<String, Map<String, Object>>>();
		int mergeResultCount = 0;
		
		long collectJobTime = System.currentTimeMillis();	
		
		//小于批量操作的数目,实际数目
		while(mergeResults.size() < topAnalyzerConfig.getMinMergeJobCount())
		{			
			Iterator<Integer> jobIds = jobs.keySet().iterator();
			
			boolean isAllJobDone = true;
			
			while (jobIds.hasNext())
			{
				int key = jobIds.next();
				DistributeJob job = jobs.get(key);
				
				//检查是否已经执行完毕
				if (jobstatusPool.get(key).equals(DistributeJob.JOB_STATUS_DOING)
						||jobstatusPool.get(key).equals(DistributeJob.JOB_STATUS_UNDO))
					isAllJobDone = false;

				// 失效的任务,超过指定分钟的未完成任务可以被再次回收分配
				if (jobstatusPool.get(key).equals(DistributeJob.JOB_STATUS_DOING)
						&& job.getStartTime() > 0)
					if (System.currentTimeMillis() - job.getStartTime() > 1000 * 60 * topAnalyzerConfig
							.getJobRecycleTime())
					{

						job.setStartTime(System.currentTimeMillis());
						jobstatusPool.put(key, DistributeJob.JOB_STATUS_UNDO);

						logger.warn(new StringBuilder().append("jobId:").append(
								job.getJobId()).append(",jobs:").append(
								job.getJobs()).append(
								" not finish too long time,reset it status..."));

						continue;
					}

				// 需要merge的
				if (jobstatusPool.replace(key, 
						DistributeJob.JOB_STATUS_DONE,DistributeJob.JOB_STATUS_RESULT_MERGED))
				{
					if (job.getResults() != null)
					{
						mergeResults.add(job.getResults());
						job.setResults(null);
					}
					else
						logger.warn("job : " + key + " has no result...");
					
					mergeResultCount += 1;

					continue;
				}
				
			}
			
			//单线程分析模式，直接继续
			if (isblockMode)
				break;
			
			//如果只允许执行一次中间结果，则不将中间结果加入到再次合并中，另一种情况就是所有任务已经完成，则允许被二次合并
			if (!topAnalyzerConfig.isResultProcessOnlyOnce() || isAllJobDone)
			{
				MergedJobResult jr = unMergedResultQueue.poll();
				
				//将未何并到主干的结果也继续交给线程去做合并
				while(jr !=  null)
				{	
					mergeResults.add(jr.getMergedResult());
					mergeResultCount += jr.getMergeCount();
					jr = unMergedResultQueue.poll();
				}
			}
			
			
			//最后一拨需要合并的数据,不需要再等待批量去做
			if (mergeResultCount + mergedJobCounter.get() >= totalJobCounter.get())
				break;
			
			if (System.currentTimeMillis() - collectJobTime 
						> topAnalyzerConfig.getMaxJobResultBundleWaitTime())
				break;
			
			//放缓一些节奏
			if (mergeResultCount == 0)
			{
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
				collectJobTime = System.currentTimeMillis();
			}	
		}

		if (mergeResultCount > 0)
		{
			//单线程分析模式
			if (isblockMode)
			{
				logger.error("start merge jobResult.");
				long beg = System.currentTimeMillis();
				
				int size = mergeResults.size();
				
				Map<String, Map<String, Object>>[] results;

				if (resultPool != null && resultPool.size() > 0)
					size += 1;

				results = new HashMap[size];
				results[0] = resultPool;

				for (Map<String, Map<String, Object>> r : mergeResults)
				{
					size -= 1;
					results[size] = r;
				}

				resultPool = analysisManager.mergeResultPools(results,
						analysisManager.getRule(null).getEntryPool(),true);
				
				mergedJobCounter.addAndGet(mergeResultCount);
				
				results = null;
				mergeResults.clear();
				long end = System.currentTimeMillis() - beg;
				totalMergeConsumeTime.addAndGet(end);
				
				logger.error(new StringBuilder("end merge jobResult. merge count : ")
						.append(mergeResultCount).append(", total merge count: ")
						.append(mergedJobCounter.get()).append(",merge consume : ").append(end).toString());
			}
			else
			{
				if (mergeResults.size() > 0)
				{
					mergeJobResultThreadPool.execute(new MergeJobTask(mergeResultCount,mergeResults));
				}
				else
					mergedJobCounter.addAndGet(mergeResultCount);
			}
			
		}

		//放缓一点节奏
		try
		{
			if (mergeResultCount == 0)
				Thread.sleep(1000);				
		}
		catch(Exception ex)
		{
			//do nothing
		}
			

	}

	@Override
	public void doAsSlave()
	{

		try
		{
			if (!isDone)
				Thread.sleep(1000 * topAnalyzerConfig.getGetJobInterval());

			isDone = false;

			// 获取任务
			Connection conn = defaultTransportManager.connect(topAnalyzerConfig
					.getMasterAddress(), topAnalyzerConfig.getMasterPort());

			//定义需要获取的任务数
			ByteBuffer content = ByteBuffer.allocate(4);
			content.putInt(topAnalyzerConfig.getMaxTransJobCount());
			content.flip();
			
			// 获取任务来分析处理，处理后将结果反馈给Master
			BasePacket basePacket = BasePacket.getNewPacketInstance(content,
					AnalysisConstants.COMMAND_GETJOB);
			BasePacket result = conn.sendPacket(basePacket, 60000);

			if (result != null && result.getByteBuffer().remaining() > 0)
			{
				int totalJobCounter = result.getByteBuffer().getInt();
				int completeJobCounter = result.getByteBuffer().getInt();
				int transJobCounter = result.getByteBuffer().getInt();
				long begtime = System.currentTimeMillis();
				List<Integer> jobIds = new ArrayList<Integer>();
				
				if(transJobCounter > 0){ //是否是空任务 add by fangliang 2010-05-24
					int ruleDataLength = result.getByteBuffer().getInt();
					long version = result.getByteBuffer().getLong();
					
					if(analysisManager.isNeedRebuildRule(version)){ //判断是否是最新版本 add by fangliang 2010-05-24
						byte [] ruleDataArray = new byte[ruleDataLength];
						result.getByteBuffer().get(ruleDataArray);
						ByteArrayInputStream byteArrayStream = null; //更新配置 add by fangliang 2010-05-24
						ObjectInputStream objInputStream = null;
						try {
							byteArrayStream = new ByteArrayInputStream(ruleDataArray);
							objInputStream = new ObjectInputStream(byteArrayStream);
							Rule dto = (Rule)objInputStream.readObject();
							if(dto != null){
								analysisManager.rebuildSlaveRule(dto);
							}
						} catch (Exception e) {
							logger.error("input RuleDataDTO stream error!",e);
						} finally {
							if(byteArrayStream != null){
								byteArrayStream.close();
							}
							if(objInputStream != null){
								objInputStream.close();
							}
						}
					} else {
						result.getByteBuffer().position(result.getByteBuffer().position()+ruleDataLength);//重新设置下标值
					} 
				}

				// 返回结果内是否包含了描述的任务
				if (result.getByteBuffer().remaining() > 0)
				{
//					byte[] c = new byte[result.getByteBuffer().remaining()];
//					result.getByteBuffer().get(c);
//					String jobs = new String(c, "UTF-8");
					StringBuilder jobs = new StringBuilder();
					
					for(int t = 0 ; t < transJobCounter; t++)
					{
						jobIds.add(result.getByteBuffer().getInt());
						int jobLength = result.getByteBuffer().getInt();
						byte[] _job = new byte[jobLength];
						result.getByteBuffer().get(_job);
						jobs.append(new String(_job,"UTF-8")).append(",");
					}

					if (logger.isWarnEnabled())
						logger.warn(new StringBuilder().append(
								"getJobFile from ").append(
								topAnalyzerConfig.getMasterAddress()).append(
								",files : ").append(jobs).append(",totaljob :")
								.append(totalJobCounter).append(
										",completejob :").append(
										completeJobCounter).toString());

					String[] resources = analysisManager.pullJobData(jobs.toString());

					Map<String, Map<String, Object>> mergedResultPool = null;

					if (resources != null)
					{
						analysisManager.dispatchJobs(resources);
						
						long mergeBeg = System.currentTimeMillis();

						// 考虑后续可以在slave先合并结果在发送到master
						if (analysisManager.getResultPools() == null
								|| (analysisManager.getResultPools() != null && analysisManager
										.getResultPools().length == 0))
						{
							mergedResultPool = new HashMap<String, Map<String, Object>>();
							
							for(int jobId : jobIds)
								logger.error(new StringBuilder(
										"------------------jobId:").append(jobId)
										.append(" get no result...----------"));
						}
						else
						{
							//不做lazy的计算，节省传输和计算
							mergedResultPool = analysisManager
									.mergeResultPools(analysisManager
											.getResultPools(), analysisManager.getRule(null)
											.getEntryPool(),false);
						}
						
						perf_logger.error(new StringBuilder().append("slave merge,")
								.append("0,0,").append(System.currentTimeMillis() - mergeBeg).toString());

						// 删除文件
						if (topAnalyzerConfig.getJobFileFrom().equals(
								AnalysisConstants.JOBFILEFROM_FTP))
						{
							try
							{
								Calendar calendar = Calendar.getInstance();
								// 删除临时文件
								File dir = new File(
										new StringBuilder(topAnalyzerConfig
												.getInput())
												.append(File.separator)
												.append(
														calendar
																.get(Calendar.YEAR))
												.append("-")
												.append(
														calendar
																.get(Calendar.MONTH) + 1)
												.append("-")
												.append(
														calendar
																.get(Calendar.DAY_OF_MONTH))
												.toString());

								File[] files = dir.listFiles();
								for (File f : files)
									f.delete();

								logger
										.info("end analysis, delete log files...");

							}
							catch (Exception ee)
							{
								logger.error("delete log file error...", ee);
							}

						}

					}
					else
					{
						logger.error("pull data from source error...");
					}

					// 输出结果
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					Deflater def = new Deflater(Deflater.BEST_COMPRESSION,false);
					DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(bout,def);
					ObjectOutputStream objOutputStream = new ObjectOutputStream(
							deflaterOutputStream);
					

					try
					{
						objOutputStream.writeInt(jobIds.size());
						
						for(int jb : jobIds)
							objOutputStream.writeInt(jb);
						
						objOutputStream.writeLong(begtime);

						if (mergedResultPool == null)
						{
							objOutputStream.writeBoolean(false);
						}
						else
						{
							objOutputStream.writeBoolean(true);
							objOutputStream.writeObject(mergedResultPool);
						}
						
						deflaterOutputStream.finish();

						ByteBuffer buf = ByteBuffer.wrap(bout.toByteArray());

						basePacket = BasePacket.getNewPacketInstance(buf,
								AnalysisConstants.COMMAND_SEND_ANALYSIS_RESULT);

						conn = defaultTransportManager.connect(
								topAnalyzerConfig.getMasterAddress(),
								topAnalyzerConfig.getMasterPort());

						result = conn.sendPacket(basePacket, 1000 * 60 * 1);

						// 重新发送未发送的数据
						if (result == null)
						{
							logger.error("first send result error,resend ...");
							conn = defaultTransportManager.connect(
									topAnalyzerConfig.getMasterAddress(),
									topAnalyzerConfig.getMasterPort());

							conn.sendPacket(basePacket, 1000 * 60 * 3);
						}
						else
						{
							totalJobCounter = result.getByteBuffer().getInt();
							completeJobCounter = result.getByteBuffer()
									.getInt();

							logger
									.info(new StringBuilder("Total Job :")
											.append(totalJobCounter)
											.append(", complete Job : ")
											.append(completeJobCounter)
											.append(
													", send result to server success..."));

							isDone = true;
						}

					}
					catch (Exception ex)
					{
						logger.error(ex, ex);
					}
					finally
					{
						if (objOutputStream != null)
						{
							objOutputStream.close();
							objOutputStream = null;
						}
						
						if (deflaterOutputStream != null)
						{
							def.end();
							deflaterOutputStream.close();
							deflaterOutputStream = null;
						}

						if (bout != null)
						{
							bout.close();
							bout = null;
						}
					}

				}
				else
				{
					if (logger.isInfoEnabled())
						logger.info(new StringBuilder().append(
								"getJobFile from ").append(
								topAnalyzerConfig.getMasterAddress()).append(
								",but no job release...").append(",totaljob :")
								.append(totalJobCounter).append(
										",completejob :").append(
										completeJobCounter).toString());
				}

			}
			else
			{
				// 可能出现问题了，需要重新启动
				conn = defaultTransportManager.reconnect(topAnalyzerConfig
						.getMasterAddress(), topAnalyzerConfig.getMasterPort());
			}
		}
		catch (Exception ex)
		{
			logger.error(ex, ex);
		}

	}

	@Override
	public boolean isMaster()
	{

		return topAnalyzerConfig.getNodeType().equalsIgnoreCase("master");
	}

	@Override
	public void run()
	{

		if (!isMaster())
		{
			try
			{
				if (logger.isWarnEnabled())
					logger.warn("Thank you "
						+ InetAddress.getLocalHost().getHostName()
						+ " for help Top Analyzer do Analysis...");
			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			// 建立任务
			checkAndResetJobList(jobs, jobStatusPool);

			// 载入属于当前分析的中间结果。
			Map<String, Map<String, Object>>[] tmp = analysisManager
					.loadAnalysisData(topAnalyzerConfig.getOutput()
							+ File.separator + "tmp" + File.separator);

			if (tmp != null && tmp.length > 0)
			{
				resultPool = analysisManager.mergeResultPools(tmp,
						analysisManager.getRule(null).getEntryPool(),true);
				logger.warn("load temp data from File...");
			}
			
			mergeJobResultThreadPool = new ThreadPoolExecutor
				(topAnalyzerConfig.getMaxMergeJobWorker(),
					topAnalyzerConfig.getMaxMergeJobWorker(),0,TimeUnit.SECONDS
					,new LinkedBlockingQueue<Runnable>(), 
					new NamedThreadFactory("mergeJobResult_worker"));

		}
		
		logger.error("start Distribute node now!");

		while (runningFlag)
		{
			try
			{
				if (isMaster())
					doAsMaster();
				else
					doAsSlave();
			}
			catch (Exception ex)
			{
				logger.error(ex, ex);
			}
		}

	}
	
	/**
	 * 用于合并结果的线程实现
	 * @author fangweng
	 * @email fangweng@taobao.com
	 * @date 2010-12-18
	 *
	 */
	class MergeJobTask implements java.lang.Runnable
	{

		int mergeCount = 0;
		List<Map<String, Map<String, Object>>> mergeResults;
		
		public MergeJobTask(int mergeCount,List<Map<String, Map<String, Object>>> mergeResults)
		{
			this.mergeCount = mergeCount;
			this.mergeResults = mergeResults;
		}
		
		@Override
		public void run() 
		{
			
			//尝试获取锁，如果失败先合并其他结果最后通过锁来合并主干
			boolean gotIt = mergeLock.tryLock();
			long beg = System.currentTimeMillis();
			long end = 0;
			
			try
			{
				if (gotIt)//和主干内容一起合并
				{
					//在只允许处理一次的模式下，中间结果交由获得主干合并权的线程再次合并
					if (topAnalyzerConfig.isResultProcessOnlyOnce())
					{
						MergedJobResult jr = unMergedResultQueue.poll();
						
						//将未何并到主干的结果也继续交给线程去做合并
						while(jr !=  null)
						{	
							mergeResults.add(jr.getMergedResult());
							mergeCount += jr.getMergeCount();
							jr = unMergedResultQueue.poll();
						}
					}
								
					int size = mergeResults.size();
					
					logger.error("start merge jobResult, count :" + size);
					
					Map<String, Map<String, Object>>[] results;

					if (resultPool != null && resultPool.size() > 0)
						size += 1;

					results = new HashMap[size];
					results[0] = resultPool;

					for (Map<String, Map<String, Object>> r : mergeResults)
					{
						size -= 1;
						results[size] = r;
					}
					
					
					
					//证明已经合并完毕，可以处理lazy
					if (mergedJobCounter.get() + mergeCount >= totalJobCounter.get())
						resultPool = analysisManager.mergeResultPools(results,
							analysisManager.getRule(null).getEntryPool(),true);
					else
						resultPool = analysisManager.mergeResultPools(results,
								analysisManager.getRule(null).getEntryPool(),false);
									
					mergedJobCounter.addAndGet(mergeCount);
					
					end = System.currentTimeMillis() - beg;
					
					logger.error(new StringBuilder("end merge jobResult. merge count : ")
							.append(mergeResults.size()).append(", total merge count: ").append(mergedJobCounter.get())
							.append(", mergeConsume : ").append(end).toString());
					 
					results = null;
					mergeResults.clear();

				}
				else
				{
					
					int size = mergeResults.size();				
					
					logger.error(new StringBuilder("start merge nonTrunk jobResult. current tmp merge job count: ") 
							.append(currentTmpJobMergeCounter.incrementAndGet()).append(",current transaction result count: " + size) );
					
					Map<String, Map<String, Object>>[] results = new HashMap[size];
					
					for (Map<String, Map<String, Object>> r : mergeResults)
					{
						size -= 1;
						results[size] = r;
					}

					Map<String, Map<String, Object>> otherResult;
					
					if (mergeResults.size() == 1)
						otherResult = results[0];
					else
						otherResult = analysisManager.mergeResultPools(results,
							analysisManager.getRule(null).getEntryPool(),false);
					
					

					//将结果放入到队列中等待获得锁的线程去执行
					MergedJobResult jr = new MergedJobResult();
					jr.setMergeCount(mergeCount);
					jr.setMergedResult(otherResult);
					unMergedResultQueue.offer(jr);
					
					end = System.currentTimeMillis() - beg;
					
					logger.error(new StringBuilder("end merge jobResult. merge count : ")
						.append(mergeResults.size()).append(" nonTrunk jobResult")
						.append(", mergeConsume : ").append(end).toString());
					
					results = null;
					mergeResults.clear();
					
				}
			}
			catch(Exception ex)
			{
				logger.error("MergeJobTask execute error",ex);
			}
			finally
			{
				if (gotIt)
					mergeLock.unlock();
				else
					currentTmpJobMergeCounter.decrementAndGet();
				
				totalMergeConsumeTime.addAndGet(end);
			}


		}
		
	}

}
