/**
 * 
 */
package com.taobao.top.analysis.transport.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.taobao.top.analysis.transport.IRemoteClientFactory;
import com.taobao.top.analysis.transport.RemoteClient;

/**
 * NIO服务端抽象父类
 * 
 * @author fangweng
 * 
 */
public class Server implements java.lang.Runnable
{

	private static final Log log = LogFactory.getLog(Server.class);

	ServerSocketChannel ssc;
	Selector selector;
	IRemoteClientFactory remoteClientFactory;
	int port;
	int counter;
	int checkServerStatusInterval;//检查服务端状态间隔时间，如果小于0表是不需要检查

	public Server(int port,int checkServerStatusInterval) throws IOException
	{
		this.port = port;
		this.checkServerStatusInterval = checkServerStatusInterval;
		init();

		if (log.isInfoEnabled())
			log.info("server listen on port :" 
					+ port + ",checkServerStatusInterval:" + checkServerStatusInterval);
	}
	
	private void init() throws IOException
	{
		selector = Selector.open();
		ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.socket().bind(new InetSocketAddress(port));	
		
		ssc.register(selector, SelectionKey.OP_ACCEPT);
		counter = 0;
	}

	public void run()
	{
		while (true)
		{
			try
			{
				if (!dispatch())
					counter += 1;
				else
					counter = 0;
				
				//10 minute no event happen to reset server
				if (checkServerStatusInterval > 0)
					if (counter > 60 * checkServerStatusInterval)
					{
						log.error("server reset now...");
						
						try
						{
							reset();
						}
						catch(Exception e)
						{
							log.error(e,e);
							break;
						}
					}
			}
			catch (Exception ex)
			{
				log.error(ex, ex);
				
				try
				{
					reset();
				}
				catch(Exception e)
				{
					log.error(e,e);
					break;
				}
				
			}
		}
		
		throw new java.lang.RuntimeException("Server stop work...");
	}
	
	public void reset() throws IOException
	{
		if (ssc != null)
			ssc.close();
		
		if (selector != null)
			selector.close();
		
		
		init();
	}

	public boolean dispatch() throws Exception
	{
		if (this.selector.select(1000) <= 0)
		{
			return false;
		}

		Iterator<SelectionKey> iter = selector.selectedKeys().iterator();

		while (iter.hasNext())
		{
			SelectionKey key = (SelectionKey) iter.next();

			iter.remove();

			if (!key.isValid())
			{
				key.cancel();
				continue;
			}

			if (key.isAcceptable())
			{
				onAccept(key);
			}
			else
			{
				try
				{
					if (key.isReadable())
					{
						if (log.isInfoEnabled())
							log.info("read bytes from client : "
									+ ((SocketChannel) key.channel()).socket()
											.getRemoteSocketAddress());

						onRead(key);
					}
					else if (key.isWritable())
					{
						if (log.isInfoEnabled())
							log.info("write to client : "
									+ ((SocketChannel) key.channel()).socket()
											.getRemoteSocketAddress());

						onWrite(key);
					}
				}
				catch (Exception ex)
				{
					try
					{
						RemoteClient remoteClient = (RemoteClient) key
								.attachment();

						if (remoteClient.getSocketChannel() != null)
						{
							remoteClient.getSocketChannel().close();
						}

						remoteClient.stopClient();
						remoteClient = null;

						log.error(ex);
					}
					catch (IOException e)
					{
						log.error(e, e);
					}
				}
			}

		}
		
		return true;
	}

	protected void registerChannel(Selector selector,
			SelectableChannel channel, int ops, Object attachement)
			throws Exception
	{
		if (channel == null)
			return;

		channel.configureBlocking(false);
		channel.register(selector, ops);
	}

	public void onAccept(SelectionKey key) throws IOException
	{
		SocketChannel channel = ((ServerSocketChannel) key.channel()).accept();

		RemoteClient remoteClient = remoteClientFactory.getInstance(channel,
				selector);

		channel.configureBlocking(false);
		channel.register(selector, SelectionKey.OP_READ, remoteClient);

		if (log.isWarnEnabled())
			log.warn("accept client from : "
					+ channel.socket().getRemoteSocketAddress());
	}

	public void onRead(SelectionKey key) throws IOException
	{
		RemoteClient remoteClient = (RemoteClient) key.attachment();
		remoteClient.onRead();
	}

	public IRemoteClientFactory getRemoteClientFactory()
	{
		return remoteClientFactory;
	}

	public void setRemoteClientFactory(IRemoteClientFactory remoteClientFactory)
	{
		this.remoteClientFactory = remoteClientFactory;
	}

	public void onWrite(SelectionKey key) throws IOException
	{
		key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
		RemoteClient remoteClient = (RemoteClient) key.attachment();
		remoteClient.onWrite();
	}

}
