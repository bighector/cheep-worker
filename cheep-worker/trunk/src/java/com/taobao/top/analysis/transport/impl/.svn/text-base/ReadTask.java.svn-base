package com.taobao.top.analysis.transport.impl;

import java.nio.ByteBuffer;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.taobao.top.analysis.transport.BasePacket;
import com.taobao.top.analysis.transport.Listener;
import com.taobao.top.analysis.transport.TransportConstants;
import com.taobao.top.analysis.util.TransportUtil;

/**
 * 数据读取工作线程
 * 
 * @author fangweng
 * 
 */
public class ReadTask implements java.lang.Runnable
{
	private ByteBuffer[] buffers;
	private Map<Long, BasePacket> waitReplyList;
	private static final Log log = LogFactory.getLog(ReadTask.class);

	public ReadTask(ByteBuffer[] bufs, Map<Long, BasePacket> waitReplyList)
	{
		buffers = bufs;
		this.waitReplyList = waitReplyList;
	}

	@Override
	public void run()
	{
		try
		{
			if (buffers != null && buffers.length > 0)
			{
				int length = TransportUtil.getPacketLength(buffers[0]);

				ByteBuffer body = null;

				if (buffers.length > 1)
				{
					body = ByteBuffer
							.allocate(TransportConstants.PACKET_HEADER_SIZE
									+ length);
					for (int i = 0; i < buffers.length; i++)
					{
						body.put(buffers[i]);
					}
					body.flip();
				}
				else
				{
					body = buffers[0];
				}

				BasePacket receivePacket = BasePacket.unmarshallPacket(body);

				if (receivePacket == null)
				{
					return;
				}

				receivePacket.setByteBuffer(body);

				if (waitReplyList != null)
				{
					// 找出对应发送的packet
					BasePacket sp = waitReplyList.remove(receivePacket
							.getSequence());
					if (sp != null)
					{
						Listener l = sp.getListener();

						if (l != null)
						{
							l.doReceive(receivePacket);
						}
						else
						{
							synchronized (sp)
							{
								sp.setReturnPacket(receivePacket);
								sp.notify();
							}
						}
					}

					receivePacket = null;
					buffers = null;
				}

			}
		}
		catch (Exception ex)
		{
			log.error("ReadTask Thread error!", ex);
		}

	}

}
