package com.taobao.top.analysis.jobmanager;


import com.taobao.top.analysis.data.Rule;

/**
 * 配置管理接口
 * 
 * @author fangweng
 * 
 */
public interface IRuleManager extends IManager
{

	/**
	 * 读取配置，建立分析规则模型
	 */
	public void buildRule(String[] configFiles);

	/**
	 * 根据分析器报表配置目录中文件修改的情况， 判断是否需要重新载入配置
	 */
	public void checkAndRebuildRule();
	
	/**
	 * 根据master推送过来的版本号检查是否需要更新配置 add by fangliang 2010-05-21
	 * @param version
	 * @return
	 */
	public boolean isNeedRebuildRule(long version);
	
	/**
	 * 根据master推送过来的配置信息更新slave的配置信息 add by fangliang 2010-05-21
	 * @param ruleData
	 */
	public void rebuildSlaveRule(Rule ruleData);
	

	/**
	 * 获取规则
	 * @return
	 */
	public Rule getRule(String domain);

}
