/**
 * 
 */
package com.taobao.top.analysis.distribute;

import java.util.Map;

import com.taobao.top.analysis.TopAnalysisConfig;

/**
 * 分布式节点接口
 * 
 * @author fangweng
 * 
 */
public interface IDistributedNode
{

	/**
	 * 判断当前是否是作为Master节点在运行
	 * 
	 * @return
	 */
	public boolean isMaster();

	/**
	 * 以Master模式运行
	 */
	public void doAsMaster();

	/**
	 * 以Slave模式运行
	 */
	public void doAsSlave();

	/**
	 * 判断是否需要输出报表
	 * 
	 * @param 任务执行状态池
	 * @return
	 */
	public boolean needToExportReport(Map<Integer, String> jobstatusPool);

	/**
	 * 设置全局配置
	 * 
	 * @param topAnalyzerConfig
	 */
	public void setTopAnalyzerConfig(TopAnalysisConfig topAnalyzerConfig);

}
