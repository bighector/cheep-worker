package com.taobao.top.analysis.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.taobao.top.analysis.AnalysisConstants;
import com.taobao.top.analysis.TopAnalysisNode;
import com.taobao.top.analysis.data.DistributeJob;
import com.taobao.top.analysis.data.Rule;
import com.taobao.top.analysis.transport.impl.AnalysisRemoteClientFactory;
import com.taobao.top.analysis.transport.impl.DefaultTransportManager;
import com.taobao.top.analysis.transport.impl.Server;

public class TransportTestCase
{
	TopAnalysisNode topAnalyzerNode;
	Server myServer;

	TransportManager defaultTransportManager;

	@Before
	public void setUpBeforeClass() throws Exception
	{
		topAnalyzerNode = new TopAnalysisNode();

		Rule ruleData = new Rule ();
		ruleData.setVersion(System.currentTimeMillis());
		
		DistributeJob job = new DistributeJob();
		job.setJobId(11);
		job.setJobs("testjob");
		job.setRuleData(ruleData);

		topAnalyzerNode.getJobs().put(11, job);

		topAnalyzerNode.getJobStatusPool().put(11,
				DistributeJob.JOB_STATUS_UNDO);

		defaultTransportManager = new DefaultTransportManager();
		defaultTransportManager.start();
		
	}

	@After
	public void tearDownAfterClass() throws Exception
	{
		defaultTransportManager.stop();
	}

	@Test
	public void testTransport() throws IOException, InterruptedException
	{
		try
		{
			myServer = new Server(7650,-1);
			AnalysisRemoteClientFactory remoteClientFactory = new AnalysisRemoteClientFactory();
			remoteClientFactory.setCompleteJobCounter(topAnalyzerNode
					.getCompleteJobCounter());
			remoteClientFactory.setJobs(topAnalyzerNode.getJobs());
			remoteClientFactory.setJobStatusPool(topAnalyzerNode
					.getJobStatusPool());
			myServer.setRemoteClientFactory(remoteClientFactory);
			new Thread(myServer).start();

			Thread.sleep(1000);

			Connection conn = defaultTransportManager.connect(InetAddress
					.getLocalHost().getHostAddress(), 7650);
			ByteBuffer buf = ByteBuffer.allocate(8);
			buf.putDouble(12.42);
			buf.flip();
			BasePacket basePacket = BasePacket.getNewPacketInstance(buf,
					AnalysisConstants.COMMAND_HEARTBEAT);

			BasePacket result = conn.sendPacket(basePacket, 50);

			long bodylength = result.getBodyLength();

			double d = result.getByteBuffer().getDouble();

			Assert.assertEquals(12.42, d);
			Assert.assertEquals(8, bodylength);

			basePacket = BasePacket.getNewPacketInstance(null,
					AnalysisConstants.COMMAND_GETJOB);

			result = conn.sendPacket(basePacket, 100000);

			result.getByteBuffer().getInt();
			result.getByteBuffer().getInt();

			int jobid = result.getByteBuffer().getInt();

			Assert.assertEquals(11, jobid);

		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Assert.assertFalse(true);
		}

	}

}
