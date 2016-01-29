package com.taobao.top.analysis.jobmanager;

import java.util.Map;

import com.taobao.top.analysis.data.ReportEntry;

/**
 * 分析管理接口
 * 
 * @author fangweng
 * 
 */
public interface IAnalysisManager extends IJobManager, IReportManager,
		IRuleManager, IManager
{


	/**
	 * @return
	 */
	public IJobManager getJobManager();

	/**
	 * @return
	 */
	public IReportManager getReportManager();

	/**
	 * @return
	 */
	public IRuleManager getRuleManager();
	
	/**
	 * 设置jobManager
	 * 
	 * @param jobManager
	 */
	public void setJobManager(IJobManager jobManager);

	/**
	 * 设置reportManager
	 * 
	 * @param reportManager
	 */
	public void setReportManager(IReportManager reportManager);

	/**
	 * 设置ruleManager
	 * 
	 * @param ruleManager
	 */
	public void setRuleManager(IRuleManager ruleManager);

	/**
	 * 分发任务执行分析工作
	 */
	public void dispatchJobs(String[] resources);
	

	/**
	 * 将分析数据导出到外部磁盘,用于分布式合并
	 * 
	 * @param 需要导出的数据
	 * @param 目标目录
	 */
	public void exportAnalysisData(
			Map<String, Map<String, Object>> resultPools, String destDir);

	/**
	 * 将分析数据导入到内存,用于分布式合并
	 * 
	 * @param destDir
	 */
	public Map<String, Map<String, Object>>[] loadAnalysisData(String destDir);

	/**
	 * 合并多个工作线程处理后的结果，准备生成报表
	 * 
	 * @param 多个结果池合并结果
	 * @param 规则定义
	 * @param 是否需要处理lazy的entry计算
	 * @return
	 */
	public Map<String, Map<String, Object>> mergeResultPools(
			Map<String, Map<String, Object>>[] resultPools,
			Map<String, ReportEntry> entryPool,boolean needMergeLazy);

	/**
	 * 资源重置
	 * 
	 * @param 是否需要重新编译
	 */
	public void reset(boolean needReconfig);

	public Map<String, Map<String, Object>>[] getResultPools();

	public void addResultPools(Map<String, Map<String, Object>>[] pools);

}
