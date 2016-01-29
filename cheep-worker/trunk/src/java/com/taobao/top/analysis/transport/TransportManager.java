package com.taobao.top.analysis.transport;

/**
 * 客户端连接管理类接口
 * 
 * @author fangweng
 * 
 */
public interface TransportManager
{

	/**
	 * 从连接池获取客户端连接
	 * 
	 * @param host
	 * @param port
	 * @return
	 */
	public Connection connect(String host, int port);

	/**
	 * 重连服务器
	 * 
	 * @param host
	 * @param port
	 * @return
	 */
	public Connection reconnect(String host, int port);

	/**
	 * 从连接池获取客户端连接
	 * 
	 * @param address
	 * @return
	 */
	public Connection connect(long address);

	public void stop();

	public void start();
}
