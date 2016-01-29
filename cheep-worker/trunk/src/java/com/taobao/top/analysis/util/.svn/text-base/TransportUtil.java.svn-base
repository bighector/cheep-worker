package com.taobao.top.analysis.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.taobao.top.analysis.transport.TransportConstants;

/**
 * @author fangweng
 * 
 */
public class TransportUtil
{
	/**
	 * 把long转成InetSocketAddress
	 * 
	 * @param id
	 * 
	 * @return InetSocketAddress
	 */
	public static InetSocketAddress idToAddress(long id)
	{
		StringBuffer host = new StringBuffer(20);

		host.append((id & 0xff)).append('.');
		host.append(((id >> 8) & 0xff)).append('.');
		host.append(((id >> 16) & 0xff)).append('.');
		host.append(((id >> 24) & 0xff));

		int port = (int) ((id >> 32) & 0xffff);

		return new InetSocketAddress(host.toString(), port);
	}

	/**
	 * 把host(ip:port)转成long
	 * 
	 * @param address
	 * 
	 * @return
	 */
	public static long hostToLong(String host)
	{
		return hostToLong(host, -1);
	}

	public static String longToHost(long id)
	{
		StringBuffer host = new StringBuffer(20);

		host.append((id & 0xff)).append('.');
		host.append(((id >> 8) & 0xff)).append('.');
		host.append(((id >> 16) & 0xff)).append('.');
		host.append(((id >> 24) & 0xff));

		return host.toString();
	}

	public static long hostToLong(String host, int port)
	{
		if (host == null)
		{
			return 0;
		}

		try
		{
			String[] a = host.split(":");

			if (a.length >= 2)
			{
				port = Integer.parseInt(a[1].trim());
			}

			if (port == -1)
			{
				return 0;
			}

			InetSocketAddress addr = new InetSocketAddress(a[0], port);

			if ((addr == null) || (addr.getAddress() == null)
					|| (addr.getPort() == 0))
			{
				return 0;
			}

			byte[] ip = addr.getAddress().getAddress();
			long address = (addr.getPort() & 0xffff);

			int ipa = 0;
			ipa |= ((ip[3] << 24) & 0xff000000);
			ipa |= ((ip[2] << 16) & 0xff0000);
			ipa |= ((ip[1] << 8) & 0xff00);
			ipa |= (ip[0] & 0xff);

			if (ipa < 0)
				address += 1;
			address <<= 32;
			return address + ipa;
		}
		catch (Exception e)
		{
		}

		return 0;
	}

	/**
	 * 获取包体长度
	 * 
	 * @param buffer
	 * @return
	 * @throws IOException
	 */
	public static int getPacketLength(ByteBuffer buffer) throws IOException
	{
		int length = 0;

		if (buffer == null
				|| (buffer != null && buffer.remaining() < TransportConstants.PACKET_HEADER_SIZE))
		{
			return length;
		}

		// 读入packet header
		long msgflag = buffer.getLong(buffer.position());
		length = buffer.getInt(buffer.position()
				+ TransportConstants.PACKET_HEADER_BLPOS);

		if (msgflag != TransportConstants.MSG_FLAG)
			throw new java.lang.RuntimeException(
					"Error Package,MessageFlag not be correct...");

		if (length > TransportConstants.MALLOC_MAX)
			throw new java.lang.RuntimeException(
					"Error Package,Message length not be correct...");

		return length;
	}

}
