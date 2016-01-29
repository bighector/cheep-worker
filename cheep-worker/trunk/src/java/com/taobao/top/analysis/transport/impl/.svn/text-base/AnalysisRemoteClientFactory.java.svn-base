package com.taobao.top.analysis.transport.impl;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import com.taobao.top.analysis.data.DistributeJob;
import com.taobao.top.analysis.transport.IRemoteClientFactory;
import com.taobao.top.analysis.transport.RemoteClient;

/**
 * @author fangweng
 * 
 */
public class AnalysisRemoteClientFactory implements IRemoteClientFactory
{
	private ConcurrentMap<Integer, String> jobStatusPool;
	private AtomicInteger completeJobCounter;
	ConcurrentMap<Integer, DistributeJob> jobs;

	public ConcurrentMap<Integer, String> getJobStatusPool()
	{
		return jobStatusPool;
	}

	public void setJobStatusPool(ConcurrentMap<Integer, String> jobStatusPool)
	{
		this.jobStatusPool = jobStatusPool;
	}

	public AtomicInteger getCompleteJobCounter()
	{
		return completeJobCounter;
	}

	public void setCompleteJobCounter(AtomicInteger completeJobCounter)
	{
		this.completeJobCounter = completeJobCounter;
	}

	public ConcurrentMap<Integer, DistributeJob> getJobs()
	{
		return jobs;
	}

	public void setJobs(ConcurrentMap<Integer, DistributeJob> jobs)
	{
		this.jobs = jobs;
	}

	@Override
	public RemoteClient getInstance(SocketChannel channel, Selector selector)
	{
		AnalysisRemoteClient remoteClient = new AnalysisRemoteClient();
		remoteClient.setSocketChannel(channel);
		remoteClient.setDaemon(true);
		remoteClient.setName("remoteClient-Thread");
		remoteClient.setCompleteJobCounter(completeJobCounter);
		remoteClient.setJobs(jobs);
		remoteClient.setJobstatusPool(jobStatusPool);
		remoteClient.setSelector(selector);
		remoteClient.start();

		return remoteClient;
	}

}
