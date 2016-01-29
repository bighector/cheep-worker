/**
 * 
 */
package com.taobao.top.analysis.transport.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.InflaterInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.top.analysis.AnalysisConstants;
import com.taobao.top.analysis.data.DistributeJob;
import com.taobao.top.analysis.data.Rule;
import com.taobao.top.analysis.transport.BasePacket;
import com.taobao.top.analysis.transport.RemoteClient;

/**
 * @author fangweng
 * 
 */
public class AnalysisRemoteClient extends RemoteClient
{
	private static final Log log = LogFactory
			.getLog(AnalysisRemoteClient.class);

	private ConcurrentMap<Integer, String> jobstatusPool;
	private AtomicInteger completeJobCounter;
	ConcurrentMap<Integer, DistributeJob> jobs;

	public ConcurrentMap<Integer, DistributeJob> getJobs()
	{
		return jobs;
	}

	public void setJobs(ConcurrentMap<Integer, DistributeJob> jobs)
	{
		this.jobs = jobs;
	}

	public ConcurrentMap<Integer, String> getJobstatusPool()
	{
		return jobstatusPool;
	}

	public void setJobstatusPool(ConcurrentMap<Integer, String> jobstatusPool)
	{
		this.jobstatusPool = jobstatusPool;
	}

	public AtomicInteger getCompleteJobCounter()
	{
		return completeJobCounter;
	}

	public void setCompleteJobCounter(AtomicInteger completeJobCounter)
	{
		this.completeJobCounter = completeJobCounter;
	}


	public void doReplay(BasePacket receivePacket)
	{
		if (receivePacket == null)
			return;

		if (socketChannel != null)
		{
			try
			{
				long command = receivePacket.getCommand();

				if (command == AnalysisConstants.COMMAND_HEARTBEAT)
				{
					ByteBuffer content = null;
					content = ByteBuffer.allocate(receivePacket.getByteBuffer()
							.remaining());
					content.put(receivePacket.getByteBuffer());
					content.flip();

					BasePacket replyPacket = BasePacket
							.getReplayPacketInstance(content, receivePacket);

					replyPacket.getByteBuffer().flip();
					getWriteQueue().add(replyPacket);

					socketChannel.register(selector, SelectionKey.OP_WRITE
							| SelectionKey.OP_READ, this);
					selector.wakeup();

					return;
				}

				// 处理请求任务的命令
				if (command == AnalysisConstants.COMMAND_GETJOB)
				{
					doGetJob(receivePacket);

				}
				else
				// 处理分析结果提交的请求
				if (command == AnalysisConstants.COMMAND_SEND_ANALYSIS_RESULT)
				{
					doAnalysisResult(receivePacket);
					
					ByteBuffer content = ByteBuffer.allocate(2 * 4);
					content.putInt(getJobstatusPool().size());
					content.putInt(getCompleteJobCounter().get());
					content.flip();

					BasePacket replyPacket = BasePacket
							.getReplayPacketInstance(content, receivePacket);

					replyPacket.getByteBuffer().flip();
					getWriteQueue().add(replyPacket);

					socketChannel.register(selector, SelectionKey.OP_WRITE
							| SelectionKey.OP_READ, this);
					selector.wakeup();
				}
			}
			catch (Exception ex)
			{
				try
				{
					if (socketChannel != null)
					{
						socketChannel.close();
					}

					this.stopClient();
					socketChannel = null;
				}
				catch (IOException e)
				{
					log.error(e, e);
				}

				log.error(ex, ex);
			}
		}

	}
	
	protected void doGetJob(BasePacket receivePacket) throws UnsupportedEncodingException, ClosedChannelException
	{
		//可能给多个job
		int jobCount = receivePacket.getByteBuffer().getInt();
		
		List<byte[]> jobs = new ArrayList<byte[]>();
		int jobsLength = 0;
		List<Integer> jobIds = new ArrayList<Integer>();
		Rule ruleDTO = null;
		
		
		if (getJobstatusPool().size() > 0)
		{
			Iterator<Integer> keys = getJobstatusPool().keySet()
					.iterator();

			while (keys.hasNext())
			{
				int key = keys.next();

				String status = getJobstatusPool().get(key);

				if (status.equals(DistributeJob.JOB_STATUS_UNDO))
				{
					if (getJobstatusPool().replace(key,
							DistributeJob.JOB_STATUS_UNDO,
							DistributeJob.JOB_STATUS_DOING))
					{
						DistributeJob job = getJobs().get(key);
						byte[] j = job.getJobs().getBytes("UTF-8");
						jobs.add(j);
						jobsLength += j.length;
						jobIds.add(job.getJobId());
						ruleDTO = job.getRuleData();
						job.setWorkerIp(receivePacket.getRemoteIP());
						job.setStartTime(System.currentTimeMillis());

						jobCount = jobCount -1;
						
						if (jobCount == 0)
							break;
					}
				}
				else
					continue;

			}
		}

		ByteBuffer content = null;

		if (jobIds.size() > 0)
		{
			byte[] ruleDataArray = new byte[0]; //读取配置对象到字节数组  add by fangliang 2010-05-24
			ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
			ObjectOutputStream  objOutputStream = null;
			try {
				objOutputStream = new  ObjectOutputStream(byteArrayStream);
				objOutputStream.writeObject(ruleDTO);
				objOutputStream.flush();
				ruleDataArray = byteArrayStream.toByteArray();
			} catch (Exception e) {
				log.error("output RuleDataDTO stream error!", e);
			} finally {
				try 
				{
					if(objOutputStream != null)
					{
						objOutputStream.close();
					}
				
				} catch (IOException ex) {
					log.error("output RuleDataDTO stream error!", ex);
				}
			}

			content = ByteBuffer.allocate(4*4 + 8 + ruleDataArray.length + 4 * 2 * jobIds.size() + jobsLength);
			content.putInt(getJobstatusPool().size());
			content.putInt(getCompleteJobCounter().get());
			content.putInt(jobIds.size());
			content.putInt(ruleDataArray.length);
			content.putLong(ruleDTO.getVersion());
			content.put(ruleDataArray);
			
			for(int i = 0 ; i < jobIds.size() ; i++)
			{
				content.putInt(jobIds.get(i));
				content.putInt(jobs.get(i).length);
				content.put(jobs.get(i));
			}

			content.flip();
		}
		else
		{
			content = ByteBuffer.allocate(3 * 4);

			content.putInt(getJobstatusPool().size());
			content.putInt(getCompleteJobCounter().get());
			content.putInt(0);
			content.flip();
		}

		BasePacket replyPacket = BasePacket
				.getReplayPacketInstance(content, receivePacket);

		replyPacket.getByteBuffer().flip();
		getWriteQueue().add(replyPacket);

		if (log.isWarnEnabled() && jobIds.size() > 0)
		{
			StringBuilder info = new StringBuilder("Send Jobs: ");
			
			
			for(int k = 0 ; k < jobIds.size(); k++)
			{
				info.append("jobId:").append(jobIds.get(k))
					.append(":").append(jobs.get(k)).append(";");
			}
			
			
			log.warn(info.append(" to ").append(
									receivePacket.getRemoteIP())
							.toString());
		}

		socketChannel.register(selector, SelectionKey.OP_WRITE
				| SelectionKey.OP_READ, this);
		
		selector.wakeup();
	}
	
	@SuppressWarnings("unchecked")
	protected void doAnalysisResult(BasePacket receivePacket) throws IOException
	{
		if (receivePacket.getByteBuffer().remaining() > 0)
		{
			
			log.error("package content size :" + receivePacket.getByteBuffer().remaining());
			
			ByteArrayInputStream bin = new ByteArrayInputStream(
					receivePacket.getByteBuffer().array(),receivePacket.getByteBuffer().position()
					,receivePacket.getByteBuffer().remaining());
			
			InflaterInputStream inflaterInputStream = new InflaterInputStream(bin);
			ObjectInputStream objInputStream = new ObjectInputStream(
					inflaterInputStream);

			try
			{
				int transJobCount = objInputStream.readInt();
				ArrayList<Integer> tJobIds = new ArrayList<Integer>();
				
				for(int k = 0; k < transJobCount; k++)
				{
					tJobIds.add(objInputStream.readInt());
				}
				
				long jobStartTime = objInputStream.readLong();
				boolean jobflag = objInputStream.readBoolean();

				Calendar calendar = Calendar.getInstance();
				int today = calendar.get(Calendar.DAY_OF_MONTH);
				calendar.setTimeInMillis(jobStartTime);

				if (today == calendar.get(Calendar.DAY_OF_MONTH)
						|| (System.currentTimeMillis() - jobStartTime <=  10*60*1000))
				{
					int _index = 0;
					
					for(int jobid : tJobIds)
					{
						if (getJobstatusPool().get(jobid) == null)
						{
							if (log.isWarnEnabled())
								log.warn("Get JobResult, jobid: "
										+ jobid + " not exist...");
						}

						if (getJobstatusPool().get(jobid) != null
								&& !getJobstatusPool().get(jobid)
										.equals(DistributeJob.JOB_STATUS_RESULT_MERGED)
								&& !getJobstatusPool().get(jobid)
										.equals(DistributeJob.JOB_STATUS_DONE))
						{
							//fixme 现在批量操作要么全都成功，要么全都失败
							if (jobflag)
							{
								Map<String, Map<String, Object>> result = null;
								
								//只合并一次结果，虽然是多个jobs
								if (_index == 0)
									result = (Map<String, Map<String, Object>>) objInputStream.readObject();
								
								//尝试释放掉最大的body资源
								receivePacket.setByteBuffer(null);

								DistributeJob job = getJobs()
										.get(jobid);

								// 并发问题，不能提前，先要放数据，然后置内容
								job.setEndTime(System
										.currentTimeMillis());
								job.setResults(result);
								
								boolean duplicateFlag = false;
								
								if (!getJobstatusPool().replace(jobid, 
										DistributeJob.JOB_STATUS_DOING, DistributeJob.JOB_STATUS_DONE))
									if(!getJobstatusPool().replace(jobid, 
											DistributeJob.JOB_STATUS_UNDO, DistributeJob.JOB_STATUS_DONE))
									{
										job.setResults(null);
										result = null;
										duplicateFlag = true;
									}
								
								if (!duplicateFlag)
									getCompleteJobCounter().incrementAndGet();

								if (log.isWarnEnabled())
								{
									log.warn(new StringBuilder("Get JobResult, jobId:").append(jobid).append(",jobs:")
													.append(job.getJobs()).append(" from ").append(receivePacket.getRemoteIP())
													.toString());

									log.warn(new StringBuilder("Total job: ").append(getJobstatusPool().size())
													.append(", complete job : ").append(getCompleteJobCounter().get())
													.toString());
								}
							}
							else
							{
								DistributeJob job = getJobs()
										.get(jobid);

								// 任务重置
								getJobstatusPool().replace(jobid,
										DistributeJob.JOB_STATUS_DOING,
										DistributeJob.JOB_STATUS_UNDO);

								log.error(new StringBuilder("Get JobResult, jobId:").append(jobid).append(",jobs:")
										.append(job.getJobs()).append(" from ").append(receivePacket.getRemoteIP())
										.append(",execute fail...").toString());
							}

						}
						else
						{
							if (getJobstatusPool().get(jobid) != null
									&& (getJobstatusPool().get(jobid).equals(DistributeJob.JOB_STATUS_DONE)
											||(getJobstatusPool().get(jobid).equals(DistributeJob.JOB_STATUS_RESULT_MERGED))))
								log.warn(new StringBuilder("Get JobResult, jobId:")
												.append(jobid).append(" be done by others..."));
						}
						
						_index += 1;
					}
				}
				else if (log.isWarnEnabled())
					log.warn("Get JobResult date not match...");

			}
			catch (Exception ex)
			{
				log.error(ex, ex);
			}
			finally
			{
				//虽然inputsteam都会保证传递关闭，这里还是在做一下。
				if (objInputStream != null)
					objInputStream.close();

				if (inflaterInputStream != null)
					inflaterInputStream.close();
				
				if (bin != null)
					bin.close();

				objInputStream = null;
				inflaterInputStream = null;
				bin = null;
			}
		}
	}
	

}
