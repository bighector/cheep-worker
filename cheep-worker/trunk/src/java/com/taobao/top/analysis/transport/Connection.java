/**
 * 
 */
package com.taobao.top.analysis.transport;

import java.nio.channels.SocketChannel;

/**
 * 连接接口定义
 * 
 * @author fangweng
 * 
 */
public interface Connection
{

	/**
	 * 打开连接
	 */
	public void open();

	/**
	 * 连接事件触发以后需要调用的方法
	 */
	public void onConnection();

	/**
	 * 是否需要重连
	 */
	public boolean needReConnect();

	/**
	 * 读事件触发以后需要调用的方法
	 */
	public void onRead();

	/**
	 * 写事件触发以后需要调用的方法
	 */
	public void onWrite();

	/**
	 * 出错时候需要调用的方法
	 */
	public void onError(Throwable e);

	/**
	 * 异步发送具体的消息报
	 */
	public boolean postPacket(BasePacket packet, Listener listener);

	/**
	 * 同步发送消息报
	 */
	public BasePacket sendPacket(BasePacket packet, int timeout);

	/**
	 * 获取传输通道
	 */
	public SocketChannel getChannel();
	
	/**
	 * 关闭通道
	 */
	public void close();

}
