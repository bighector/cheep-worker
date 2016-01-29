package com.taobao.top.analysis.transport.impl;

import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.taobao.top.analysis.transport.Connection;
import com.taobao.top.analysis.transport.TransportManager;
import com.taobao.top.analysis.util.TransportUtil;

/**
 * 客户端管理类默认实现
 * 
 * @author fangweng
 * 
 */
public class DefaultTransportManager implements Runnable, TransportManager
{
	private static final Log log = LogFactory
			.getLog(DefaultTransportManager.class);
	private Selector selector = null; // 选择器
	private Thread currThread = null; // 线程
	private boolean stop = false;
	private ConcurrentMap<Long, Connection> connMap = new ConcurrentHashMap<Long, Connection>();// 连接池
	private Queue<Connection> newConn = new ConcurrentLinkedQueue<Connection>();// 需要建立的新连接
	private ReentrantLock lock = new ReentrantLock();

	/**
	 * 开始线程
	 * 
	 * @throws IOException
	 */
	public synchronized void start()
	{
		try
		{
			if (this.currThread == null)
			{
				this.selector = Selector.open();
				this.currThread = new Thread(this, "TCP Transport Thread");
				this.stop = false;
				this.currThread.start();
			}
		}
		catch (IOException e)
		{
			log.error(e, e);
		}
	}

	/**
	 * 停止线程
	 * 
	 * @throws IOException
	 */
	public void stop()
	{
		try
		{
			if (this.currThread != null)
			{
				this.stop = true;
				this.selector.close();
				
				for(Connection conn : connMap.values())
				{
					try
					{
						conn.close();
					}
					catch(Exception ex)
					{
						log.error("conn close error!",ex);
					}
				}
				
				this.currThread.interrupt();
				this.currThread = null;
			}
		}
		catch (IOException e)
		{
			log.error(e, e);
		}
	}

	/**
	 * 创建一连接
	 * 
	 * @return
	 */
	public Connection connect(String host, int port)
	{
		return connect(TransportUtil.hostToLong(host, port));
	}

	public Connection reconnect(String host, int port)
	{
		lock.lock();
		try
		{
			Connection conn = connMap.remove(TransportUtil.hostToLong(host, port));
			conn.close();
		}
		catch(Exception ex)
		{
			log.error("reconnect error!",ex);
		}
		finally
		{
			lock.unlock();
		}

		return connect(TransportUtil.hostToLong(host, port));
	}

	/**
	 * 创建一连接
	 * 
	 * @param address
	 * 
	 * @return
	 */
	public Connection connect(long address)
	{
		Connection connection = null;

		lock.lock();

		try
		{
			connection = connMap.get(address);

			if (connection == null)
			{
				connection = new ClientConnection(selector, connMap,
						TransportUtil.idToAddress(address));

				connMap.put(address, connection);
				newConn.offer(connection);
				this.selector.wakeup();

				try
				{
					Thread.sleep(500);
				}
				catch (InterruptedException e)
				{
				}
				
				log.warn("create new connection...");
				
			}
			else if (connection.needReConnect())
			{
				newConn.offer(connection);
				this.selector.wakeup();

				try
				{
					Thread.sleep(500);
				}
				catch (InterruptedException e)
				{
				}
				
				log.warn("reconnect connection...");
			}
		}
		finally
		{
			lock.unlock();
		}

		return connection;
	}

	/**
	 * 把连接加入到select中
	 */
	private void openNewConn()
	{
		Connection connection;

		while ((connection = newConn.poll()) != null)
		{
			connection.open();
		}

	}

	/**
	 * 线程run函数
	 */
	public void run()
	{
		Connection connection = null;

		while (!this.stop)
		{
			// 把newConn加进来
			openNewConn();

			try
			{
				if (this.selector.select(1000) <= 0)
				{
					continue;
				}

				Iterator<SelectionKey> iter = selector.selectedKeys()
						.iterator();

				while (iter.hasNext())
				{
					SelectionKey key = iter.next();

					iter.remove();

					if (!key.isValid())
					{
						key.cancel();
						continue;
					}

					connection = (Connection) key.attachment();

					if (key.isConnectable())
					{
						connection.onConnection();
					}
					else if (key.isReadable())
					{
						connection.onRead();
					}
					else if (key.isWritable())
					{
						key.interestOps(key.interestOps()
								& ~SelectionKey.OP_WRITE);
						connection.onWrite();
					}

				}

			}
			catch (ClosedSelectorException cse)
			{
				break;
			}
			catch (Throwable e)
			{
				if (connection != null)
				{
					connection.onError(e);
				}
			}
			finally
			{
				connection = null;
			}
		}
	}

}
