/**
 * 
 */
package com.taobao.top.analysis;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 分析器配置类，用于读取配置信息
 * 
 * @author fangweng
 * 
 */
public class TopAnalysisConfig
{
	private static final Log logger = LogFactory
			.getLog(TopAnalysisConfig.class);

	//--Common Config---------------------------
	/**
	 * 当前服务节点的类型，slave,master,alone
	 */
	private String nodeType = "master";// master,slave,lone
	/**
	 * 分布式环境下，主服务器端口
	 */
	private int masterPort = 7650;

	
	//--Master Config---------------------------
	/**
	 * 报表配置文件，模型配置,Slave不需要，因为是通过Master传递过来的，Job内包含了定义
	 */
	private String[] reportConfigs;	
	/**
	 * 分析结果输出目录
	 */
	private String output;	
	/**
	 * 增量输出报表，单位分钟
	 */
	private int exportInterval = 5;
	/**
	 * 任务重置时间，单位（分钟），多久时间没有被执行完毕任务可以被回收再分配
	 */
	private int jobRecycleTime = 5;

	/**
	 * 增量式分析过程中，任务重新获取的时间间隔，单位分钟。
	 */
	private int jobResetTime = -1;

	/**
	 * 指定当天执行的小时点
	 */
	private int doJobHourOfDay = -1;
	/**
	 * Master合并结果的工作线程最大数量
	 */
	private int maxMergeJobWorker = 10;
	
	/**
	 * master是否是单线程阻塞方式合并结果
	 */
	private boolean needBlockToMergeResult = false;
	
	/**
	 * master合并最小的结果数
	 */
	private int minMergeJobCount = 2;
	
	/**
	 * 设置了minMergeJobCount后最大等待组成一个bundle批量处理的时间，默认为1秒
	 */
	private int maxJobResultBundleWaitTime = 1000;
	
	/**
	 * 任何结果只允许并行合并一次，后续就必须合并到主干
	 */
	private boolean resultProcessOnlyOnce = false;
	/**
	 * 分布式任务类型，file支持直接从ftp拖取文件，machine支持增量的获取数据
	 */
	private String jobFileFrom = AnalysisConstants.JOBFILEFROM_FTP;

	/**
	 * 任务的具体描述，可能是文件，也可能是服务器ip
	 */
	private String jobs;

	/**
	 * 方便在jobs的描述，用$job$占位
	 */
	private String resourcePattern;
	/**
	 * 需要将报表发送给谁
	 */
	private String mailto;

	/**
	 * smpt服务器
	 */
	private String smtpServer = "email.alibaba-inc.com";

	private String mailUserName;

	private String mailPassWord;
	/**
	 * 图形输出目录
	 */
	private String chartFilePath = "D:\\testlog\\output";
	
	
	
	//--Slave Config----------------------------
	/**
	 * 分布式版本下，主服务器地址
	 */
	private String masterAddress;
	/**
	 * 对于输入分析的日志采用什么符号作为分隔符
	 */
	private String splitRegex = ",";
	/**
	 * 单机工作线程池大小，Slave用于并行分析数据的线程个数
	 */
	private int analysisWorkNum = 20;
	/**
	 * 分布式环境下，Slave请求任务间隔时间，单位s
	 */
	private int getJobInterval = 1;
	/**
	 * 切割文件工作线程池大小
	 */
	private int splitWorkerNum = 10;
	/**
	 * Slave每次获取任务最大数量。
	 */
	private int maxTransJobCount = 1;
	/**
	 * 最大文件块定义，单位M
	 */
	private int maxFileBlockSize = 1500;
	/**
	 * 日志文件的编码方式
	 */
	private String logFileEncoding = "UTF-8";
	/**
	 * 分析内容输入目录，如果是增量流方式，将不使用这个配置
	 */
	private String input = "/tmp";
	/**
	 * 是否需要删除分析后的日志
	 */
	private boolean deleteLogFile = false;
	/**
	 * 获取分析文件，文件名的匹配字符串，用于FTP的模式
	 */
	private String[] matchFileName = { ".top-access.log." };
	/**
	 * ftp服务器地址，用于当前采用ftp存储日志的情况
	 */
	private String ftpServer = "121.0.25.136";

	private String ftpUserName = "pubftp";

	private String ftpPassWord = "look";
	/**
	 * ftp的连接配置
	 */
	private boolean localPassiveMode = false;
	/**
	 * window环境下的解压缩命令,linux下不需要配置
	 */
	private String unzipCommand = "C:\\\\Program Files\\\\WinRAR\\\\winrar ";
	
	
	
	
	public boolean isResultProcessOnlyOnce() {
		return resultProcessOnlyOnce;
	}

	public void setResultProcessOnlyOnce(boolean resultProcessOnlyOnce) {
		this.resultProcessOnlyOnce = resultProcessOnlyOnce;
	}

	public int getMaxJobResultBundleWaitTime() {
		return maxJobResultBundleWaitTime;
	}

	public void setMaxJobResultBundleWaitTime(int maxJobResultBundleWaitTime) {
		this.maxJobResultBundleWaitTime = maxJobResultBundleWaitTime;
	}

	public int getMinMergeJobCount() {
		return minMergeJobCount;
	}

	public void setMinMergeJobCount(int minMergeJobCount) {
		this.minMergeJobCount = minMergeJobCount;
	}

	public boolean isNeedBlockToMergeResult() {
		return needBlockToMergeResult;
	}

	public void setNeedBlockToMergeResult(boolean needBlockToMergeResult) {
		this.needBlockToMergeResult = needBlockToMergeResult;
	}

	public int getMaxMergeJobWorker() {
		return maxMergeJobWorker;
	}

	public void setMaxMergeJobWorker(int maxMergeJobWorker) {
		this.maxMergeJobWorker = maxMergeJobWorker;
	}

	public int getMaxTransJobCount() {
		return maxTransJobCount;
	}

	public void setMaxTransJobCount(int maxTransJobCount) {
		this.maxTransJobCount = maxTransJobCount;
	}

	public String getLogFileEncoding() {
		return logFileEncoding;
	}

	public void setLogFileEncoding(String logFileEncoding) {
		this.logFileEncoding = logFileEncoding;
	}

	public String getFtpUserName()
	{
		return ftpUserName;
	}

	public void setFtpUserName(String ftpUserName)
	{
		this.ftpUserName = ftpUserName;
	}

	public String getFtpPassWord()
	{
		return ftpPassWord;
	}

	public void setFtpPassWord(String ftpPassWord)
	{
		this.ftpPassWord = ftpPassWord;
	}

	public int getDoJobHourOfDay()
	{
		return doJobHourOfDay;
	}

	public void setDoJobHourOfDay(int doJobHourOfDay)
	{
		this.doJobHourOfDay = doJobHourOfDay;
	}

	public String getResourcePattern()
	{
		return resourcePattern;
	}

	public void setResourcePattern(String resourcePattern)
	{
		this.resourcePattern = resourcePattern;
	}

	public int getJobResetTime()
	{
		return jobResetTime;
	}

	public void setJobResetTime(int jobResetTime)
	{
		this.jobResetTime = jobResetTime;
	}

	public int getJobRecycleTime()
	{
		return jobRecycleTime;
	}

	public void setJobRecycleTime(int jobRecycleTime)
	{
		this.jobRecycleTime = jobRecycleTime;
	}

	public String getJobs()
	{
		return jobs;
	}

	public void setJobs(String jobs)
	{
		this.jobs = jobs;
	}

	public String getJobFileFrom()
	{
		return jobFileFrom;
	}

	public void setJobFileFrom(String jobFileFrom)
	{
		this.jobFileFrom = jobFileFrom;
	}

	public String getChartFilePath()
	{
		return chartFilePath;
	}

	public void setChartFilePath(String chartFilePath)
	{
		this.chartFilePath = chartFilePath;
	}

	public int getExportInterval()
	{
		return exportInterval;
	}

	public void setExportInterval(int exportInterval)
	{
		this.exportInterval = exportInterval;
	}

	public String getSmtpServer()
	{
		return smtpServer;
	}

	public void setSmtpServer(String smtpServer)
	{
		this.smtpServer = smtpServer;
	}

	public boolean isLocalPassiveMode()
	{
		return localPassiveMode;
	}

	public void setLocalPassiveMode(boolean localPassiveMode)
	{
		this.localPassiveMode = localPassiveMode;
	}

	public String getFtpServer()
	{
		return ftpServer;
	}

	public void setFtpServer(String ftpServer)
	{
		this.ftpServer = ftpServer;
	}

	public String[] getMatchFileName()
	{
		return matchFileName;
	}

	public boolean isInMatchFiles(String fileName)
	{
		boolean result = false;

		if (fileName != null && !"".equals(fileName))
		{
			for (String f : matchFileName)
			{
				if (fileName.indexOf(f) >= 0)
				{
					result = true;
					break;
				}
			}
		}

		return result;
	}

	public void setMatchFileName(String matchFileName)
	{
		this.matchFileName = matchFileName.split(",");
	}

	public boolean isDeleteLogFile()
	{
		return deleteLogFile;
	}

	public void setDeleteLogFile(boolean deleteLogFile)
	{
		this.deleteLogFile = deleteLogFile;
	}

	public String getUnzipCommand()
	{
		return unzipCommand;
	}

	public void setUnzipCommand(String unzipCommand)
	{
		this.unzipCommand = unzipCommand;
	}

	public int getGetJobInterval()
	{
		return getJobInterval;
	}

	public void setGetJobInterval(int getJobInterval)
	{
		this.getJobInterval = getJobInterval;
	}

	public String getMasterAddress()
	{
		return masterAddress;
	}

	public void setMasterAddress(String masterAddress)
	{
		this.masterAddress = masterAddress;
	}

	public int getMasterPort()
	{
		return masterPort;
	}

	public void setMasterPort(int masterPort)
	{
		this.masterPort = masterPort;
	}

	public String getSplitRegex()
	{
		return splitRegex;
	}

	public void setSplitRegex(String splitRegex)
	{
		this.splitRegex = splitRegex;
	}

	public String getMailUserName()
	{
		return mailUserName;
	}

	public void setMailUserName(String mailUserName)
	{
		this.mailUserName = mailUserName;
	}

	public String getMailPassWord()
	{
		return mailPassWord;
	}

	public void setMailPassWord(String mailPassWord)
	{
		this.mailPassWord = mailPassWord;
	}

	public void loadConfigFromFile(String conf)
	{
		File prop = new File(conf);
		URL resource = null;

		if (prop != null && !prop.exists())
		{
			prop = new File(new StringBuilder().append(
					System.getProperty("user.dir")).append(File.separatorChar)
					.append(conf).toString());

			if (prop != null && !prop.exists())
			{
				logger.error(new StringBuilder(
						"top config properties file not exist!").append(
						" file location:").append(
						System.getProperty("user.dir")).append(
						File.separatorChar).append(conf).toString());

				logger.error("try to load from classpath...");

				resource = ClassLoader.getSystemResource(conf);

				if (resource == null)
					return;
			}
		}

		Properties properties = new Properties();
		FileReader reader = null;

		try
		{
			if (resource != null)
			{
				properties.load(resource.openStream());
			}
			else
			{
				reader = new FileReader(conf);
				properties.load(reader);
			}

		}
		catch (Exception ex)
		{
			logger.error(ex.getMessage(), ex);
		}
		finally
		{
			if (reader != null)
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
					logger.error(e.getMessage(), e);
				}
		}
		
		//第一步要检查是Master还是Slave
		if (properties.getProperty("nodeType") != null)
			nodeType = properties.getProperty("nodeType");
		else
		{
			throw new java.lang.RuntimeException("nodeType must set!");
		}	
		
		if (properties.getProperty("masterPort") != null)
			masterPort = Integer.valueOf(properties.getProperty("masterPort"));
		else
			throw new java.lang.RuntimeException("node must define masterPort.");
		
		if (nodeType.equalsIgnoreCase("master"))
		{
			if (properties.getProperty("configs") == null)
			{
				throw new java.lang.RuntimeException("master node must define configs.");
			}
			else
				reportConfigs = properties.getProperty("configs").split(",");


			if (properties.getProperty("output") == null)
			{
				throw new java.lang.RuntimeException("master node must define output.");
			}
			else
				output = properties.getProperty("output");
			
			//用于合并结果的参数
			if (properties.getProperty("maxMergeJobWorker") != null)
				maxMergeJobWorker = Integer.valueOf(properties
						.getProperty("maxMergeJobWorker"));
			
			if (properties.getProperty("needBlockToMergeResult") != null)
				needBlockToMergeResult = Boolean.valueOf(properties
						.getProperty("needBlockToMergeResult"));
			
			if (properties.getProperty("resultProcessOnlyOnce") != null)
				resultProcessOnlyOnce = Boolean.valueOf(properties
						.getProperty("resultProcessOnlyOnce"));
			
			if (properties.getProperty("minMergeJobCount") != null)
			{
				minMergeJobCount = Integer.valueOf(properties
						.getProperty("minMergeJobCount"));
				
				if (minMergeJobCount <= 0)
					minMergeJobCount = 1;
			}
			
			if (properties.getProperty("maxJobResultBundleWaitTime") != null)
				maxJobResultBundleWaitTime = Integer.valueOf(properties
						.getProperty("maxJobResultBundleWaitTime"));
		}
		
		if (nodeType.equalsIgnoreCase("slave"))
		{
			if (properties.getProperty("masterAddress") != null)
				masterAddress = properties.getProperty("masterAddress");
			else
				throw new java.lang.RuntimeException("slave node must define masterAddress.");

			
			if (properties.getProperty("getJobInterval") != null)
				getJobInterval = Integer.valueOf(properties
						.getProperty("getJobInterval"));
			
			if (properties.getProperty("maxTransJobCount") != null)
				maxTransJobCount = Integer.valueOf(properties
						.getProperty("maxTransJobCount"));
		}
			
		if (properties.getProperty("splitRegex") != null)
			splitRegex = properties.getProperty("splitRegex");	
		
		if (properties.getProperty("input") != null)
			input = properties.getProperty("input");

		if (properties.getProperty("workerNum") != null)
			analysisWorkNum = Integer.parseInt(properties
					.getProperty("workerNum"));

		if (properties.getProperty("splitWorkerNum") != null)
			splitWorkerNum = Integer.parseInt(properties
					.getProperty("splitWorkerNum"));

		if (properties.getProperty("maxFileSize") != null)
			maxFileBlockSize = Integer.parseInt(properties
					.getProperty("maxFileSize"));

		if (properties.getProperty("mailto") != null)
			mailto = properties.getProperty("mailto");
	

		if (properties.getProperty("unzipCommand") != null)
			unzipCommand = properties.getProperty("unzipCommand");

		if (properties.getProperty("deleteLogFile") != null)
			deleteLogFile = Boolean.valueOf(properties
					.getProperty("deleteLogFile"));

		if (properties.getProperty("matchFileName") != null)
			setMatchFileName(properties.getProperty("matchFileName"));

		if (properties.getProperty("ftpServer") != null)
			ftpServer = properties.getProperty("ftpServer");

		if (properties.getProperty("localPassiveMode") != null)
			localPassiveMode = Boolean.valueOf(properties
					.getProperty("localPassiveMode"));		

		if (properties.getProperty("smtpServer") != null)
			smtpServer = properties.getProperty("smtpServer");

		if (properties.getProperty("mailUserName") != null)
			mailUserName = properties.getProperty("mailUserName");

		if (properties.getProperty("mailPassWord") != null)
			mailPassWord = properties.getProperty("mailPassWord");

		if (properties.getProperty("exportInterval") != null)
			exportInterval = Integer.valueOf(properties
					.getProperty("exportInterval"));

		if (properties.getProperty("chartFilePath") != null)
			chartFilePath = properties.getProperty("chartFilePath");

		if (properties.getProperty("jobFileFrom") != null)
			jobFileFrom = properties.getProperty("jobFileFrom");

		if (properties.getProperty("jobs") != null)
			jobs = properties.getProperty("jobs");

		if (properties.getProperty("jobRecycleTime") != null)
			jobRecycleTime = Integer.valueOf(properties
					.getProperty("jobRecycleTime"));

		if (properties.getProperty("jobResetTime") != null)
			jobResetTime = Integer.valueOf(properties
					.getProperty("jobResetTime"));

		if (properties.getProperty("resourcePattern") != null)
			resourcePattern = properties.getProperty("resourcePattern");

		if (properties.getProperty("doJobHourOfDay") != null)
			doJobHourOfDay = Integer.valueOf(properties
					.getProperty("doJobHourOfDay"));

		if (properties.getProperty("ftpUserName") != null)
			ftpUserName = properties.getProperty("ftpUserName");

		if (properties.getProperty("ftpPassWord") != null)
			ftpPassWord = properties.getProperty("ftpPassWord");
		
		if (properties.getProperty("logFileEncoding") != null)
			logFileEncoding = properties.getProperty("logFileEncoding");
		
	}

	@Override
	public String toString()
	{
		return new StringBuilder().append("configs:").append(
				Arrays.toString(reportConfigs)).append(",input:").append(input)
				.append(",output:").append(output).append(",workerNum:")
				.append(analysisWorkNum).append(",splitWorkerNum:").append(
						splitWorkerNum).append(",maxFileSize:").append(
						maxFileBlockSize).append("M").append(",mailto:")
				.append(mailto).append(",splitRegex:").append(splitRegex)
				.append(",masterAddress:").append(masterAddress).append(
						",masterPort:").append(masterPort).append(",nodeType:")
				.append(nodeType).append(",getJobInterval:").append(
						getJobInterval).append(",unzipCommand:").append(
						unzipCommand).append(",deleteLogFile:").append(
						deleteLogFile).append(",ftpServer:").append(ftpServer)
				.append(",jobFileFrom:").append(jobFileFrom).append(
						",jobRecycleTime:").append(jobRecycleTime).append(
						",jobResetTime:").append(jobResetTime).append(
						",resourcePattern:").append(resourcePattern).append(
						",ftpUserName:").append(ftpUserName).append(
						",ftpPassWord:").append(ftpPassWord)
						.append(",logFileEncoding:").append(logFileEncoding)
						.append(",maxTransJobCount:").append(maxTransJobCount)
						.append(",maxMergeJobWorker:").append(maxMergeJobWorker).toString();
	}

	public String[] getReportConfigs()
	{
		return reportConfigs;
	}

	public void setReportConfigs(String[] reportConfigs)
	{
		this.reportConfigs = reportConfigs;
	}

	public String getNodeType()
	{
		return nodeType;
	}

	public void setNodeType(String nodeType)
	{
		this.nodeType = nodeType;
	}

	public String getInput()
	{
		return input;
	}

	public void setInput(String input)
	{
		this.input = input;
	}

	public String getOutput()
	{
		return output;
	}

	public void setOutput(String output)
	{
		this.output = output;
	}

	public int getAnalysisWorkNum()
	{
		return analysisWorkNum;
	}

	public void setAnalysisWorkNum(int analysisWorkNum)
	{
		this.analysisWorkNum = analysisWorkNum;
	}

	public int getSplitWorkerNum()
	{
		return splitWorkerNum;
	}

	public void setSplitWorkerNum(int splitWorkerNum)
	{
		this.splitWorkerNum = splitWorkerNum;
	}

	public String getMailto()
	{
		return mailto;
	}

	public void setMailto(String mailto)
	{
		this.mailto = mailto;
	}

	public int getMaxFileBlockSize()
	{
		return maxFileBlockSize;
	}

	public void setMaxFileBlockSize(int maxFileBlockSize)
	{
		this.maxFileBlockSize = maxFileBlockSize;
	}

}
