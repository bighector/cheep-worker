/**
 * 
 */
package com.taobao.top.analysis.jobmanager;

import java.util.List;

/**
 * 任务管理接口
 * 
 * @author fangweng
 * 
 */
public interface IJobManager extends IManager
{

	/**
	 * 获取分析数据列表
	 * 
	 * @return
	 */
	public List<String> getJobs();

	/**
	 * 拉取任务的分析数据,返回文件列表或者是url资源
	 */
	public String[] pullJobData(String resources);

	/**
	 * 删除已经分析过的数据
	 * 
	 * @param resource
	 * @return
	 */
	public boolean deleteJobData(String[] resource);

}
