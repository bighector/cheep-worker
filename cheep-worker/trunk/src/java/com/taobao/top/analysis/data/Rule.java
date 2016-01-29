package com.taobao.top.analysis.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 配置信息的传输对象，用于master向slave传输配置信息
 * @author fangliang
 *
 */
public class Rule implements Serializable{

	private static final long serialVersionUID = -4977691745701083747L;
	
	/**
	 * entry定义池
	 */
	private Map<String,ReportEntry> entryPool;
	/**
	 * 报表定义池
	 */
	private Map<String,Report> reportPool;
	/**
	 * 别名定义池
	 */
	private Map<String,Alias> aliasPool;
	/**
	 * 告警定义
	 */
	private List<ReportAlert> alerts;
	
	/**
	 * 父亲entry定义池
	 */
	Map<String, ReportEntry> parentEntryPool;
	
	/**
	 * 记录所有被引用的Entry的定义，用于过滤没有被引用的entry定义
	 */
	private Map<String, ReportEntry> referEntrys;
	
	
	/**
	 * 配置信息的版本号 格式为TimeInMillis值
	 */
	private long version;
	
	/**
	 * 所属域
	 */
	private String domain;
	
	public Rule()
	{
		entryPool = new HashMap<String, ReportEntry>();
		parentEntryPool = new HashMap<String, ReportEntry>();
		reportPool = new TreeMap<String, Report>();
		aliasPool = new HashMap<String, Alias>();
		alerts = new ArrayList<ReportAlert>();
		referEntrys = new HashMap<String,ReportEntry>();
		version = System.currentTimeMillis();
	}
	
	public void clear()
	{
		entryPool.clear();
		parentEntryPool.clear();
		reportPool.clear();
		aliasPool.clear();
		alerts.clear();
		referEntrys.clear();
		
		domain = null;
		version = 0;
	}
	

	public String getDomain()
	{
		return domain;
	}


	public Map<String, ReportEntry> getReferEntrys() {
		return referEntrys;
	}

	public void setReferEntrys(Map<String, ReportEntry> referEntrys) {
		this.referEntrys = referEntrys;
	}

	public void setDomain(String domain)
	{
		this.domain = domain;
	}


	public Map<String, ReportEntry> getParentEntryPool()
	{
		return parentEntryPool;
	}


	public void setParentEntryPool(Map<String, ReportEntry> parentEntryPool)
	{
		this.parentEntryPool = parentEntryPool;
	}


	public Map<String, ReportEntry> getEntryPool() {
		return entryPool;
	}

	public void setEntryPool(Map<String, ReportEntry> entryPool) {
		this.entryPool = entryPool;
	}

	public Map<String, Report> getReportPool() {
		return reportPool;
	}

	public void setReportPool(Map<String, Report> reportPool) {
		this.reportPool = reportPool;
	}

	public Map<String, Alias> getAliasPool() {
		return aliasPool;
	}

	public void setAliasPool(Map<String, Alias> aliasPool) {
		this.aliasPool = aliasPool;
	}

	public List<ReportAlert> getAlerts() {
		return alerts;
	}

	public void setAlerts(List<ReportAlert> alerts) {
		this.alerts = alerts;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

}
