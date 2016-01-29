package com.taobao.top.analysis.transport;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 底层数据交互基础消息包
 * 
 * @author fangweng
 * 
 */
public class BasePacket
{

	private static final Log log = LogFactory.getLog(BasePacket.class);
	private static byte[] ip;

	static
	{
		try
		{
			ip = InetAddress.getLocalHost().getAddress();
		}
		catch (Exception ex)
		{
			log.error("can not get local ip", ex);
		}

	}

	// header
	/**
	 * 消息标志，用于去除无效数据报
	 */
	private long msgflag;
	/**
	 * 会话号，用于返回数据定位响应处理者
	 */
	private long sequence;
	/**
	 * 消息命令
	 */
	private long command;
	/**
	 * 时间戳
	 */
	private long timestamp;
	/**
	 * 消息体长度
	 */
	private int bodyLength;
	/**
	 * 远程客户端ＩＰ
	 */
	private String remoteIP;
	/**
	 * 远程客户端临时端口
	 */
	private int remotePort;

	/**
	 * 会话码生成器
	 */
	private static AtomicLong seqGenerator = new AtomicLong(0);
	/**
	 * 转换后的发送字节缓存
	 */
	protected ByteBuffer byteBuffer;
	/**
	 * 返回的消息报文
	 */
	private BasePacket returnPacket;
	private long startTime = 0;
	/**
	 * 异常
	 */
	protected Exception exception;
	/**
	 * 指定的处理监听类，暂时没有用
	 */
	private Listener listener = null;

	private BasePacket()
	{
	};

	public int getRemotePort()
	{
		return remotePort;
	}

	public void setRemotePort(int remotePort)
	{
		this.remotePort = remotePort;
	}

	public long getMsgflag()
	{
		return msgflag;
	}

	public void setMsgflag(long msgflag)
	{
		this.msgflag = msgflag;
	}

	public Exception getException()
	{
		return exception;
	}

	public void setException(Exception exception)
	{
		this.exception = exception;
	}

	public long getStartTime()
	{
		return startTime;
	}

	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}

	public long getSequence()
	{
		return sequence;
	}

	public void setSequence(long sequence)
	{
		this.sequence = sequence;
	}

	public long getCommand()
	{
		return command;
	}

	public void setCommand(long command)
	{
		this.command = command;
	}

	public long getBodyLength()
	{
		return bodyLength;
	}

	public void setBodyLength(int bodyLength)
	{
		this.bodyLength = bodyLength;
	}

	public long getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(long timestamp)
	{
		this.timestamp = timestamp;
	}

	public Listener getListener()
	{
		return listener;
	}

	public void setListener(Listener listener)
	{
		this.listener = listener;
	}

	public void setByteBuffer(ByteBuffer byteBuffer)
	{
		this.byteBuffer = byteBuffer;
	}

	public BasePacket getReturnPacket()
	{
		return returnPacket;
	}

	public void setReturnPacket(BasePacket returnPacket)
	{
		this.returnPacket = returnPacket;
	}

	public String getRemoteIP()
	{
		return remoteIP;
	}

	public void setRemoteIP(String remoteIP)
	{
		this.remoteIP = remoteIP;
	}

	public ByteBuffer getByteBuffer()
	{
		if (byteBuffer == null)
		{
			encode();
		}

		return byteBuffer;
	}

	public int encode()
	{
		return 0;
	}

	public void decode()
	{
	}

	protected void writePacketBegin(int capacity)
	{

		// packet header
		byteBuffer = ByteBuffer.allocate(capacity
				+ TransportConstants.PACKET_HEADER_SIZE);
		byteBuffer.putLong(TransportConstants.MSG_FLAG);
		byteBuffer.putLong(sequence);
		byteBuffer.putLong(command);
		byteBuffer.put(ip);
		byteBuffer.putInt(remotePort);
		byteBuffer.putLong(System.currentTimeMillis());
		byteBuffer.putInt(0); // body len
	}

	protected void writePacketEnd()
	{
		int len = byteBuffer.position() - TransportConstants.PACKET_HEADER_SIZE;

		byteBuffer.putInt(TransportConstants.PACKET_HEADER_BLPOS, len);
	}

	/**
	 * 根据byteBuffer创建传输包
	 * 
	 * @param 消息体
	 * @param 消息命令
	 * @return
	 */
	public static BasePacket getNewPacketInstance(ByteBuffer buffer,
			long command)
	{
		BasePacket result = new BasePacket();
		int size = 0;

		if (buffer != null)
			size = buffer.remaining();

		result.sequence = seqGenerator.incrementAndGet();
		result.command = command;

		result.writePacketBegin(size);

		if (buffer != null)
			result.byteBuffer.put(buffer);

		result.writePacketEnd();

		return result;
	}

	/**
	 * 根据给定的消息体和返回数据创建返回的数据报
	 * 
	 * @param 需要返回的数据字节包
	 * @param 原数据包
	 * @return
	 */
	public static BasePacket getReplayPacketInstance(ByteBuffer buffer,
			BasePacket srcPacket)
	{
		BasePacket result = new BasePacket();

		int size = 0;

		if (buffer != null)
			size = buffer.remaining();

		result.sequence = srcPacket.sequence;
		result.command = srcPacket.command;
		result.remotePort = srcPacket.remotePort;

		result.writePacketBegin(size);

		if (buffer != null)
			result.byteBuffer.put(buffer);

		result.writePacketEnd();

		return result;
	}

	/**
	 * 从buffer中反解析创建Packet
	 * 
	 * @param buffer
	 * @return
	 * @throws UnknownHostException
	 */
	public static BasePacket unmarshallPacket(ByteBuffer buffer)
			throws UnknownHostException
	{
		if (buffer.remaining() < TransportConstants.PACKET_HEADER_SIZE)
		{
			return null;
		}

		BasePacket packet = new BasePacket();

		long msgflag = buffer.getLong();
		packet.setMsgflag(msgflag);
		packet.setSequence(buffer.getLong());
		packet.setCommand(buffer.getLong());

		byte[] bip = new byte[4];
		buffer.get(bip);
		packet.setRemotePort(buffer.getInt());

		packet.setRemoteIP(new StringBuilder().append(
				InetAddress.getByAddress(bip).getHostAddress()).append(":")
				.append(packet.getRemotePort()).toString());

		packet.setTimestamp(buffer.getLong());
		packet.setBodyLength(buffer.getInt());

		if (msgflag != TransportConstants.MSG_FLAG)
		{
			log.error("error msgflag !");
			return null;
		}

		return packet;

	}

	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();

		result.append("msgflag:").append(msgflag).append(",sequence:").append(
				sequence).append(",command:").append(command).append(
				",timestamp:").append(timestamp).append(",bodyLength:").append(
				bodyLength).append(",remoteIP:").append(remoteIP).append(
				",remotePort:").append(remotePort).append(",byteBuffer:")
				.append(byteBuffer);

		return result.toString();
	}

}
