/**
 * 
 */
package com.taobao.top.analysis;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.taobao.top.analysis.data.DistributeJob;
import com.taobao.top.analysis.distribute.IncrementalAnalysisNode;
import com.taobao.top.analysis.jobmanager.DefaultJobManager;
import com.taobao.top.analysis.jobmanager.DefaultReportManager;
import com.taobao.top.analysis.jobmanager.DefaultRuleManager;
import com.taobao.top.analysis.jobmanager.IAnalysisManager;
import com.taobao.top.analysis.jobmanager.DefaultAnalysisManager;
import com.taobao.top.analysis.jobmanager.IJobManager;
import com.taobao.top.analysis.jobmanager.IReportManager;
import com.taobao.top.analysis.jobmanager.IRuleManager;
import com.taobao.top.analysis.transport.TransportManager;
import com.taobao.top.analysis.transport.impl.AnalysisRemoteClientFactory;
import com.taobao.top.analysis.transport.impl.DefaultTransportManager;
import com.taobao.top.analysis.transport.impl.Server;

/**
 * 分布式分析器,支持Server和Slave的两种模式
 * 
 * @author fangweng
 * 
 */
public class TopAnalysisNode implements java.lang.Runnable
{
	private static final Log logger = LogFactory.getLog(TopAnalysisNode.class);

	/**
	 * 具体报表分析执行类，用于上层业务分析
	 * ，Master和Slave都内置的分析引擎
	 */
	IAnalysisManager analysisManager = null;
	/**
	 * Master的数据处理层，负责监听客户端请求
	 */
	Server transportServer;
	Thread tServer;
	/**
	 * slave的数据处理层，负责连接服务端发送请求
	 */
	TransportManager defaultTransportManager;
	/**
	 * 分析器的全局配置
	 */
	TopAnalysisConfig topAnalyzerConfig;
	
	/**
	 * 分布式节点的实现
	 */
	IncrementalAnalysisNode distributeNode;

	/**
	 * 分析的结果缓存
	 */
	Map<String, Map<String, Object>> resultPool;
	/**
	 * 任务执行列表
	 */
	ConcurrentMap<Integer, DistributeJob> jobs;
	/**
	 * 任务状态池
	 */
	ConcurrentMap<Integer, String> jobStatusPool;

	private boolean flag = true;
	/**
	 * 任务计数器
	 */
	private AtomicInteger completeJobCounter = new AtomicInteger(0);
	
	

	public IAnalysisManager getAnalysisManager()
	{
		return analysisManager;
	}

	public void setAnalysisManager(IAnalysisManager analysisManager)
	{
		this.analysisManager = analysisManager;
	}

	public TopAnalysisConfig getTopAnalyzerConfig()
	{
		return topAnalyzerConfig;
	}

	public void setTopAnalyzerConfig(TopAnalysisConfig topAnalyzerConfig)
	{
		this.topAnalyzerConfig = topAnalyzerConfig;
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

	public IncrementalAnalysisNode getDistributeNode()
	{
		return distributeNode;
	}

	public void setDistributeNode(IncrementalAnalysisNode distributeNode)
	{
		this.distributeNode = distributeNode;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		if (args == null
				|| (args != null && args.length == 1 && args[0].equals("")))
		{
			System.out.println("usage : java -jar TopAnalyzer.jar propFile");
			logger.error("usage : java -jar TopAnalyzer.jar propFile");
			return;
		}

		TopAnalysisNode topAnalyzerNode = new TopAnalysisNode();

		TopAnalysisConfig config = new TopAnalysisConfig();
		config.loadConfigFromFile(args[0]);
		topAnalyzerNode.setTopAnalyzerConfig(config);

		new Thread(topAnalyzerNode, "topAnalyzerNode-Thread").start();

	}

	public TopAnalysisNode()
	{
		// 初始化资源和分析器执行器
		jobs = new ConcurrentHashMap<Integer, DistributeJob>();
		jobStatusPool = new ConcurrentHashMap<Integer, String>();
	}

	@Override
	public void run()
	{
		try
		{
			long lastCheckTime = 0;

			logger.warn("Map-Reduce edition Analyer...");

			init();

			while (flag)
			{

				// 判断是否需要载入新的配置,当前是3分钟检查一次
				if (System.currentTimeMillis() - lastCheckTime > 3 * 60 * 1000)
				{
					//master需要5分钟载入一次配置文件,而slave不需要  modify by fangliang 2010-05-20
					if (topAnalyzerConfig.getNodeType().equalsIgnoreCase("master"))
					{
						analysisManager.checkAndRebuildRule();
					}

					lastCheckTime = System.currentTimeMillis();
				}

				Thread.sleep(1000);
			}
		}
		catch (Exception ex)
		{
			logger.error(ex, ex);
		}
		finally
		{
			try
			{
				if (distributeNode != null)
					distributeNode.stopThread();

				if (analysisManager != null)
					analysisManager.destory();

				if (defaultTransportManager != null)
				{
					defaultTransportManager.stop();
					defaultTransportManager = null;
				}

			}
			catch (Exception ex)
			{
				logger.error(ex, ex);
			}

		}
	}

	protected void init() throws IOException
	{
		//上层的解析模块初始化
		if (analysisManager == null)
		{
			analysisManager = new DefaultAnalysisManager();

			IRuleManager ruleManager = new DefaultRuleManager();
			IReportManager reportManager = new DefaultReportManager();
			IJobManager jobManager = new DefaultJobManager();

			ruleManager.setTopAnalyzerConfig(topAnalyzerConfig);
			reportManager.setTopAnalyzerConfig(topAnalyzerConfig);
			jobManager.setTopAnalyzerConfig(topAnalyzerConfig);
			analysisManager.setTopAnalyzerConfig(topAnalyzerConfig);

			analysisManager.setReportManager(reportManager);
			analysisManager.setRuleManager(ruleManager);
			analysisManager.setJobManager(jobManager);

			ruleManager.init();
			reportManager.init();
			jobManager.init();

			analysisManager.init();
			analysisManager.buildRule(topAnalyzerConfig.getReportConfigs());
		}

		// 判断是否自己就是master
		if (topAnalyzerConfig.getNodeType().equalsIgnoreCase("master"))
		{
			transportServer = new Server(topAnalyzerConfig.getMasterPort(),topAnalyzerConfig.getJobResetTime());
			
			AnalysisRemoteClientFactory remoteClientFactory = new AnalysisRemoteClientFactory();
			remoteClientFactory.setCompleteJobCounter(completeJobCounter);
			remoteClientFactory.setJobs(jobs);
			remoteClientFactory.setJobStatusPool(jobStatusPool);

			transportServer.setRemoteClientFactory(remoteClientFactory);

			tServer = new Thread(transportServer, "TopAnalyzer-ServerThread");
			tServer.setDaemon(true);
			tServer.start();

			logger.warn("TopAnalyzerNode start at Master Mode...");
		}
		else
		{
			defaultTransportManager = new DefaultTransportManager();
			defaultTransportManager.start();

			logger.warn("TopAnalyzerNode start at Slave Mode...");
		}

		// 后台任务启动
		distributeNode = new IncrementalAnalysisNode();
		distributeNode.setName("TopAnalyzerNode-InnerJobWorker");
		distributeNode.setDaemon(true);
		distributeNode.setTopAnalyzerConfig(topAnalyzerConfig);
		distributeNode.setReportJobManager(analysisManager);
		distributeNode.setJobs(jobs);
		distributeNode.setJobStatusPool(jobStatusPool);
		distributeNode.setResultPool(resultPool);
		distributeNode.setCompleteJobCounter(completeJobCounter);
		distributeNode.setDefaultTransportManager(defaultTransportManager);

		distributeNode.start();
	}

	public void stopThread()
	{
		this.flag = false;
	}

	public ConcurrentMap<Integer, DistributeJob> getJobs()
	{
		return jobs;
	}

	public ConcurrentMap<Integer, String> getJobStatusPool()
	{
		return jobStatusPool;
	}

	public AtomicInteger getCompleteJobCounter()
	{
		return completeJobCounter;
	}

	public void setCompleteJobCounter(AtomicInteger completeJobCounter)
	{
		this.completeJobCounter = completeJobCounter;
	}

}
