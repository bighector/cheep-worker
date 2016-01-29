package com.taobao.top.analysis.transport;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.taobao.top.analysis.transport.impl.ReadTask;
import com.taobao.top.analysis.util.NamedThreadFactory;

/**
 * 客户端数据读取工作线程
 * 
 * @author fangweng
 * 
 */
public class NioBufferReadWorker extends Thread
{
	/**
	 * 数据包处理线程池
	 */
	private ExecutorService executor;
	/**
	 * 输入数据缓冲池
	 */
	private List<ByteBuffer> bufferTree;
	/**
	 * 尚未解析的数据字节数
	 */
	int leaveCount;
	/**
	 * 读入的数据队列
	 */
	BlockingQueue<ByteBuffer> readBufferQueue;
	/**
	 * 等待回复的请求队列
	 */
	Map<Long, BasePacket> waitReplyList;
	boolean isRunnable = true;
	long lastPackageSendTime = 0;
	long totalLength = 0;

	private static final Log log = LogFactory.getLog(NioBufferReadWorker.class);

	public void stopWorker()
	{
		isRunnable = false;
		executor.shutdown();
		bufferTree.clear();
		leaveCount = 0;
		lastPackageSendTime = 0;
		totalLength = 0;
		this.interrupt();
	}

	public NioBufferReadWorker(BlockingQueue<ByteBuffer> readBufferQueue,
			Map<Long, BasePacket> waitReplyList)
	{
		executor = new ThreadPoolExecutor(20, 1000, 300, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(), new NamedThreadFactory(
						"NioClient-BufferReadWorker"),
				new ThreadPoolExecutor.AbortPolicy());

		bufferTree = new ArrayList<ByteBuffer>();
		this.readBufferQueue = readBufferQueue;
		this.waitReplyList = waitReplyList;
		leaveCount = 0;
		totalLength = 0;
	}

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

				doAnalysis(bufferTree, executor);

			}
			catch (InterruptedException ie)
			{
				if (bufferTree != null && bufferTree.size() > 0)
					bufferTree.clear();
			}
			catch (Exception ex)
			{
				// 移除可能的错误数据
				if (bufferTree != null)
				{
					totalLength = 0;
					bufferTree.clear();
				}

				log.error("InnerWorker analysis data error!", ex);
			}

		}

	}

	public void doAnalysis(List<ByteBuffer> buffers, ExecutorService executor)
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

			lastPackageSendTime = 0;

			executor.execute(new ReadTask(content, waitReplyList));
		}

	}

}
