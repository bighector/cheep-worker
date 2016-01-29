/**
 * 
 */
package com.taobao.top.analysis.jobmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.taobao.top.analysis.TopAnalysisConfig;
import com.taobao.top.analysis.data.JobResource;
import com.taobao.top.analysis.data.Rule;
import com.taobao.top.analysis.util.AnalyzerFilenameFilter;
import com.taobao.top.analysis.util.ReportUtil;

/**
 * 默认的规则管理实现
 * 
 * @author fangweng
 * 
 */
public class DefaultRuleManager implements IRuleManager
{

	private final Log logger = LogFactory.getLog(DefaultRuleManager.class);

	/**
	 * 规则
	 */
	protected Rule rule;

	/**
	 * 报表配置信息所在的目录
	 */
	protected List<JobResource> configResources;

	/**
	 * 全局配置
	 */
	protected TopAnalysisConfig topAnalyzerConfig;

	/**
	 * 配置文件列表
	 */
	protected List<String> configs;
	

	public DefaultRuleManager()
	{
		configResources = new ArrayList<JobResource>();
		configs = new ArrayList<String>();
		rule = new Rule();
	}
	
	

	public Rule getRule()
	{
		return rule;
	}



	public void setRule(Rule rule)
	{
		this.rule = rule;
	}



	@Override
	public void init()
	{

	}

	public void setTopAnalyzerConfig(TopAnalysisConfig topAnalyzerConfig)
	{
		this.topAnalyzerConfig = topAnalyzerConfig;
	}

	@Override
	public void buildRule(String[] configFiles)
	{

		logger.warn("start build config...");
		
		if (topAnalyzerConfig.getNodeType().equalsIgnoreCase("master")){ //master读取配置，slave不读取配置 add by fangliang 2010-05-25
			for (String config : configFiles)
			{
				if (config.startsWith("dir:"))
				{
					configResources.add(new JobResource(config.substring(config
							.indexOf("dir:")
							+ "dir:".length())));

					try
					{
						File[] files = new File(config.substring(config
								.indexOf("dir:")
								+ "dir:".length()))
								.listFiles(new AnalyzerFilenameFilter(".xml"));

						for (File file : files)
						{
							ReportUtil.buildReportModule(new StringBuilder("file:")
									.append(file.getAbsolutePath()).toString(),rule);

							configs.add("file:" + file.getAbsolutePath());
						}
						rule.setVersion(Calendar.getInstance().getTimeInMillis()); //更新版本号 add by fangliang 2010-05-25
					}
					catch (Exception ex)
					{
						logger.error(ex, ex);
					}
				}
				else
				{
					configResources.add(new JobResource(config));
					
					ReportUtil.buildReportModule(config,rule);
					rule.setVersion(Calendar.getInstance().getTimeInMillis()); //更新版本号 add by fangliang 2010-05-25
					configs.add(config);
				}

			}

		} else {
			logger.warn("Slave nodeType don't load rule.");
		}
	}

	@Override
	public void checkAndRebuildRule()
	{	
		if (configResources != null && configResources.size() > 0)
		{
			boolean needReload = false;

			for (JobResource res : configResources)
			{
				if (res.isModify())
				{
					res.reload(null);
					needReload = true;
				}
			}

			// 重新载入
			if (needReload)
			{
				Rule _rule = new Rule();
				
				for (JobResource resource : configResources)
				{
					if (resource.getResource().isDirectory())
					{
						File[] files = resource.getResource().listFiles(
								new AnalyzerFilenameFilter(".xml"));

						for (File file : files)
						{
							ReportUtil.buildReportModule(new StringBuilder("file:")
									.append(file.getAbsolutePath()).toString(),
									_rule);
						}
					}
					else
					{
						ReportUtil.buildReportModule(new StringBuilder("file:")
							.append(resource.getResource().getAbsolutePath()).toString(),
							_rule);
					}

				}
				
				rule = _rule;
				
				logger.warn("config reload ...");

			}
		}
	}

	@Override
	public void destory()
	{
		rule.clear();
	}
	
	@Override
	public Rule getRule(String domain) {
		return rule;
	}

	@Override
	public boolean isNeedRebuildRule(long version) {
		return rule.getVersion() == version ? false:true;
	}

	@Override
	public void rebuildSlaveRule(Rule ruleData) {
		rule.setAlerts(ruleData.getAlerts());
		rule.setAliasPool(ruleData.getAliasPool());
		rule.setEntryPool(ruleData.getEntryPool());
		rule.setParentEntryPool(ruleData.getParentEntryPool());
		rule.setReportPool(ruleData.getReportPool());
		rule.setVersion(ruleData.getVersion());
		rule.setDomain(ruleData.getDomain());
	}

}
