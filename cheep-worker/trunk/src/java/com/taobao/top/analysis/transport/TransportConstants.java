package com.taobao.top.analysis.transport;

public class TransportConstants
{
	// buffer size
	public static final int INOUT_BUFFER_SIZE = 131072;
	public static final int DEFAULT_TIMEOUT = 3000;
	/**
	 * 消息报文验证字,用于验证有效的数据报
	 */
	public static final long MSG_FLAG = 0x11000011;

	public static final int DEFAULT_READ_THREAD = 500;
	public static final int DEFAULT_WRITE_THREAD = 100;
	public static final int DEFAULT_WRITE_PAGE = 100;
	public static final int BATCH_WRITE_PAGESIZE = 1000;

	/** 新分配内存的最大值 */
	public static final int MALLOC_MAX = 1024 * 1024 * 100; // 30MB
	// public static final int MALLOC_MAX = 2; // 30MB

	/** 队列中最大的长度 */
	public static final int WRITEQUEUE_MAX_COUNT = 300000;
	public static final int READQUEUE_MAX_COUNT = 300000;
	public static final int PACKET_HEADER_SIZE = 44;
	public static final int PACKET_HEADER_BLPOS = 40;
	public static final int PACKET_HEADER_IPPOS = 24;
	public static final int PACKET_HEADER_PORTPOS = 28;

	// 分布式分析器命令
	public static final long COMMAND_JOIN = 0x00000001;
	public static final long COMMAND_LEAVE = 0x00000002;
	public static final long COMMAND_DOJOB = 0x00000003;
	public static final long COMMAND_REPORT = 0x00000004;

	// 连接状态
	public static final int CONN_STATUS_NOT_CONNECT = 0;
	public static final int CONN_STATUS_CONNECTING = 1;
	public static final int CONN_STATUS_CONNECTED = 2;

}
