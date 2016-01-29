/**
 * 
 */
package com.taobao.top.analysis.transport;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.taobao.top.analysis.util.TransportUtil;

/**
 * 服务端为每一个客户端连接分配的处理类, 为客户端请求作出响应处理
 * 
 * @author fangweng
 * 
 */
public abstract class RemoteClient extends Thread
{
	private static final Log log = LogFactory.getLog(RemoteClient.class);

	/**
	 * 数据通道
	 */
	public SocketChannel socketChannel;
	protected Selector selector;
	/**
	 * 输出队列
	 */
	private Queue<BasePacket> writeQueue;
	/**
	 * 读入队列
	 */
	protected BlockingQueue<ByteBuffer> readBufferQueue;// 记录读取的字节缓存
	/**
	 * 读入数据缓存列表
	 */
	private List<ByteBuffer> bufferTree;
	boolean isRunnable = true;
	/**
	 * 剩余没有读取的字节数
	 */
	int leaveCount;
	/**
	 * 最后消息发送时间,用于丢弃长时间没有处理的未完的数据包
	 */
	long lastPackageSendTime = 0;

	long totalLength = 0;

	public RemoteClient()
	{
		writeQueue = new ConcurrentLinkedQueue<BasePacket>();
		readBufferQueue = new LinkedBlockingQueue<ByteBuffer>(
				TransportConstants.READQUEUE_MAX_COUNT);
		bufferTree = new ArrayList<ByteBuffer>();
	}

	public Selector getSelector()
	{
		return selector;
	}

	public void setSelector(Selector selector)
	{
		this.selector = selector;
	}

	public SocketChannel getSocketChannel()
	{
		return socketChannel;
	}

	public Queue<BasePacket> getWriteQueue()
	{
		return writeQueue;
	}

	public void setWriteQueue(Queue<BasePacket> writeQueue)
	{
		this.writeQueue = writeQueue;
	}

	public void setSocketChannel(SocketChannel socketChannel)
	{
		this.socketChannel = socketChannel;
	}

	/**
	 * 向客户端写数据事件触发
	 * 
	 * @throws IOException
	 */
	public void onWrite() throws IOException
	{

		BasePacket packet;

		while ((packet = writeQueue.poll()) != null)
		{
			ByteBuffer c = packet.getByteBuffer();

			while (c.remaining() > 0)
				socketChannel.write(c);
		}

	}

	/**
	 * 客户端有数据发送过来事件触发
	 * 
	 * @throws IOException
	 */
	public void onRead() throws IOException
	{
		int ret = 0;
		long totalcount = 0;

		do
		{
			ByteBuffer readByteBuffer = ByteBuffer.allocate(1024);
			ret = socketChannel.read(readByteBuffer);

			totalcount += ret;

			if (ret > 0)
			{
				readByteBuffer.flip();
				readBufferQueue.add(readByteBuffer);
			}
		} while (ret > 0);

		if (log.isInfoEnabled())
			log.info(new StringBuilder().append(
					"read bytes from client,total :").append(totalcount)
					.toString());

		if (ret == -1)
			throw new RuntimeException("socket is close...");
	}

	/*
	 * 循环处理接收到的数据
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run()
	{
		while (isRunnable)
		{

			try
			{
				ByteBuffer byteBuffer = readBufferQueue.poll(1000,
						TimeUnit.MILLISECONDS);

				if (byteBuffer == null)
					continue;

				do
				{
					totalLength += byteBuffer.remaining();

					// 判断是否需要merge
					if (bufferTree != null
							&& bufferTree.size() > 0
							&& bufferTree.get(0).remaining() < TransportConstants.PACKET_HEADER_SIZE)
					{
						if (log.isInfoEnabled())
							log.info("merge leave bytes to next package...");

						ByteBuffer nByteBuffer = ByteBuffer.allocate(byteBuffer
								.remaining()
								+ bufferTree.get(0).remaining());

						nByteBuffer.put(bufferTree.get(0));
						nByteBuffer.put(byteBuffer);
						nByteBuffer.flip();
						bufferTree.remove(0);
						bufferTree.add(0, nByteBuffer);
					}
					else
						bufferTree.add(byteBuffer);

					byteBuffer = readBufferQueue.poll();

				} while (byteBuffer != null);

				doAnalysis(bufferTree);

			}
			catch (Exception ex)
			{
				// 移除可能的错误数据
				if (bufferTree != null)
				{
					totalLength = 0;
					bufferTree.clear();
				}

				if (!(ex instanceof java.lang.InterruptedException))
					log.error("InnerWorker analysis data error!", ex);
			}

		}

	}

	/**
	 * 分析接收到的数据
	 * 
	 * @param buffers
	 * @throws IOException
	 */
	public void doAnalysis(List<ByteBuffer> buffers) throws IOException
	{

		if (buffers == null || (buffers != null && buffers.size() == 0))
			return;

		if (totalLength > 8)
		{
			// 作消息合法校验,控制大于报头长度的垃圾数据
			int bufStart = 0;
			for (ByteBuffer buf : buffers)
			{
				if (buf.getLong(buf.position()) != TransportConstants.MSG_FLAG)
					bufStart += 1;
				else
					break;
			}

			if (bufStart > 0)
			{
				long removeLength = 0;

				for (int i = 0; i < bufStart; i++)
				{
					totalLength -= buffers.get(0).remaining();
					removeLength += buffers.get(0).remaining();
					buffers.remove(0);
				}

				log.error("remove " + bufStart
						+ " packages,leave package count: " + buffers.size()
						+ " , totalLength: " + totalLength + ", removeLength: "
						+ removeLength);
			}
		}

		if (totalLength < TransportConstants.PACKET_HEADER_SIZE)
			return;

		int bufLength = 0;

		while (totalLength >= TransportConstants.PACKET_HEADER_SIZE)
		{
			int listIndex = 0;

			bufLength = buffers.get(0).getInt(
					buffers.get(0).position()
							+ TransportConstants.PACKET_HEADER_BLPOS)
					+ TransportConstants.PACKET_HEADER_SIZE;

			if (totalLength < bufLength)
			{
				// 防止长久堆积的无效数据,10分钟还不能够消耗的数据
				if (lastPackageSendTime > 0
						&& (System.currentTimeMillis() - lastPackageSendTime > 1000 * 60 * 10))
				{
					if (buffers.size() == 1)
					{
						buffers.clear();
					}
					else
					{
						int bufStart = 1;
						for (int i = 1; i < buffers.size(); i++)
						{
							ByteBuffer buf = buffers.get(i);
							if (buf.getLong(buf.position()) == TransportConstants.MSG_FLAG)
								break;
							else
								bufStart += 1;
						}

						if (bufStart > 0)
						{
							for (int i = 0; i < bufStart; i++)
							{
								totalLength -= buffers.get(0).remaining();
								buffers.remove(0);
							}

							log
									.error("remove "
											+ bufStart
											+ " packages,because long time not get content...");
						}
					}

					lastPackageSendTime = 0;
				}

				if (lastPackageSendTime == 0)
					lastPackageSendTime = System.currentTimeMillis();

				break;
			}

			for (int i = 0; i < buffers.size(); i++)
			{
				ByteBuffer content = buffers.get(i);
				int remain = content.remaining();

				if (bufLength <= remain)
				{
					if (bufLength < remain)
					{
						// 合并消息，防止第一个消息包都无法读出body length，将当前剩余数据包合并到后一个数据包中
						if (remain - bufLength < TransportConstants.PACKET_HEADER_SIZE
								&& i + 1 < buffers.size())
						{
							ByteBuffer nByteBuffer = ByteBuffer.allocate(remain
									- bufLength
									+ buffers.get(i + 1).remaining());
							ByteBuffer _buf = content.duplicate();
							_buf.position(_buf.position() + bufLength);
							nByteBuffer.put(_buf);
							nByteBuffer.put(buffers.get(i + 1));
							nByteBuffer.flip();
							buffers.remove(i + 1);
							buffers.add(i + 1, nByteBuffer);

							content.limit(content.position() + bufLength);
						}
						else
						{
							ByteBuffer _buf = content.duplicate();
							_buf.position(_buf.position() + bufLength);
							content.limit(content.position() + bufLength);
							buffers.add(i + 1, _buf);
						}
					}

					break;
				}
				else
				{
					bufLength -= remain;
					listIndex += 1;
				}

			}

			ByteBuffer[] content = new ByteBuffer[listIndex + 1];

			for (int i = 0; i < listIndex + 1; i++)
			{
				content[i] = buffers.get(0);
				totalLength -= buffers.get(0).remaining();
				buffers.remove(0);
			}

			if (log.isInfoEnabled())
				log.info("totalLength: " + totalLength);

			lastPackageSendTime = 0;

			// 开始分析接收到的数据报，转化为有效的数据报文，并且作相应的回复
			if (content != null && content.length > 0)
			{
				int length = TransportUtil.getPacketLength(content[0]);

				ByteBuffer body = null;

				if (content.length > 1)
				{
					body = ByteBuffer
							.allocate(TransportConstants.PACKET_HEADER_SIZE
									+ length);
					for (int i = 0; i < content.length; i++)
					{
						body.put(content[i]);
					}
					body.flip();
				}
				else
				{
					body = content[0];
				}

				BasePacket receivePacket = BasePacket.unmarshallPacket(body);

				if (receivePacket == null)
				{
					return;
				}

				receivePacket.setByteBuffer(body);
				doReplay(receivePacket);
			}
		}

	}

	/**
	 * 回复客户端请求
	 * 
	 * @param receivePacket
	 */
	public abstract void doReplay(BasePacket receivePacket);

	public void stopClient()
	{
		isRunnable = false;
		bufferTree.clear();
		leaveCount = 0;
		lastPackageSendTime = 0;
		totalLength = 0;

		log.error("stop RemoteClient "
				+ socketChannel.socket().getRemoteSocketAddress().toString());

		this.interrupt();
	}

}
