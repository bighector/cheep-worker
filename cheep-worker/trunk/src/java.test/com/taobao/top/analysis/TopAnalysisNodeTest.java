/**
 * 
 */
package com.taobao.top.analysis;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author fangweng
 * 
 */
public class TopAnalysisNodeTest
{

	TopAnalysisNode serverNode;
	TopAnalysisNode slaveNode;
	Thread server;
	Thread slave;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void beforeClass() throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void afterClass() throws Exception
	{
	}

	
	@Test
	public void test()
	{
		try
		{
			serverNode = new TopAnalysisNode();
			TopAnalysisConfig config = new TopAnalysisConfig();
			config.loadConfigFromFile("now-top-analysis.properties");
			serverNode.setTopAnalyzerConfig(config);

			server = new Thread(serverNode, "topAnalyzerNode-server-Thread");
			server.setDaemon(true);
			server.start();

			Thread.sleep(1000);

			slaveNode = new TopAnalysisNode();
			TopAnalysisConfig slave_config = new TopAnalysisConfig();
			slave_config
					.loadConfigFromFile("now-top-analysis-slave.properties");
			slaveNode.setTopAnalyzerConfig(slave_config);
			slave = new Thread(slaveNode, "topAnalyzerNode-slave-Thread");
			slave.setDaemon(true);
			slave.start();

			Thread.sleep(1000000000);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			Assert.assertFalse(true);
		}

	}

}
