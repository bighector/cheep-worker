/**
 * 
 */
package com.taobao.top.analysis.data;

import java.util.Map;

/**
 * 分布式任务定义
 * 
 * @author fangweng
 * 
 */
public class DistributeJob implements java.io.Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String JOB_STATUS_UNDO = "undo";
	public static final String JOB_STATUS_DOING = "doing";
	public static final String JOB_STATUS_DONE = "done";
	public static final String JOB_STATUS_RESULT_MERGED = "merged";

	/**
	 * 任务Id
	 */
	private int jobId;
	/**
	 * 任务文件名称或者是服务器ip
	 */
	private String jobs;
	/**
	 * 任务开始时间
	 */
	private long startTime;
	/**
	 * 任务结束时间
	 */
	private long endTime;
	/**
	 * 工作者IP
	 */
	private String workerIp;
	
	/**
	 * 最新的配置信息 add by fangliang 2010-05-20
	 */
	private Rule ruleData;
	/**
	 * 结果集
	 */
	private Map<String, Map<String, Object>> results;

	@Override
	public String toString()
	{
		return new StringBuilder("DistributeJob: ").append("jobId:").append(
				jobId).append(",jobs:").append(jobs).append(",startTime:")
				.append(startTime).append(",endTime:").append(endTime).append(
						",workerIp:").append(workerIp).toString();
	}

	public int getJobId()
	{
		return jobId;
	}

	public void setJobId(int jobId)
	{
		this.jobId = jobId;
	}

	public Map<String, Map<String, Object>> getResults()
	{
		return results;
	}

	public void setResults(Map<String, Map<String, Object>> results)
	{
		this.results = results;
	}

	public String getJobs()
	{
		return jobs;
	}

	public void setJobs(String jobs)
	{
		this.jobs = jobs;
	}

	public long getStartTime()
	{
		return startTime;
	}

	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}

	public long getEndTime()
	{
		return endTime;
	}

	public void setEndTime(long endTime)
	{
		this.endTime = endTime;
	}

	public String getWorkerIp()
	{
		return workerIp;
	}

	public void setWorkerIp(String workerIp)
	{
		this.workerIp = workerIp;
	}

	public Rule getRuleData() {
		return ruleData;
	}

	public void setRuleData(Rule ruleData) {
		this.ruleData = ruleData;
	}

}
