package com.taobao.top.analysis.data;

import java.util.List;

/**
 * 报表定义
 * 
 * @author wenchu
 * 
 */
public class Report implements java.io.Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 278466226057587334L;

	private String id;
	private String file;// 保存的文件名称
	private List<ReportEntry> reportEntrys;// entry的列表
	private String mailto;// 暂时未使用
	private String orderby;// 暂时未使用
	private String chartTitle;// 图形输出的Title，当前采用google chart api。
	private String chartType;// 当前支持柱状和曲线,line,bar
	private int rowCount = 0;// 最多获取多少行
	private String export;// 是否需要导出报表内容：支持chart,html
	private int exportCount = 0;// 输出html多少行
	
	private String conditions;//该report中key的条件设置 add by fangliang 2010-05-26
	private boolean period = false;//是否周期性输出结果，用于片段维度统计

	
	public boolean isPeriod() {
		return period;
	}

	public void setPeriod(boolean period) {
		this.period = period;
	}

	public int getExportCount()
	{
		return exportCount;
	}

	public void setExportCount(int exportCount)
	{
		this.exportCount = exportCount;
	}

	public String getExport()
	{
		return export;
	}

	public void setExport(String export)
	{
		this.export = export;
	}

	public int getRowCount()
	{
		return rowCount;
	}

	public void setRowCount(int rowCount)
	{
		this.rowCount = rowCount;
	}

	public String getChartType()
	{
		return chartType;
	}

	public void setChartType(String chartType)
	{
		this.chartType = chartType;
	}

	public String getChartTitle()
	{
		return chartTitle;
	}

	public void setChartTitle(String chartTitle)
	{
		this.chartTitle = chartTitle;
	}

	public String getOrderby()
	{
		return orderby;
	}

	public void setOrderby(String orderby)
	{
		this.orderby = orderby;
	}

	public String getMailto()
	{
		return mailto;
	}

	public void setMailto(String mailto)
	{
		this.mailto = mailto;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getFile()
	{
		return file;
	}

	public void setFile(String file)
	{
		this.file = file;
	}

	public List<ReportEntry> getReportEntrys()
	{
		return reportEntrys;
	}

	public void setReportEntrys(List<ReportEntry> reportEntrys)
	{
		this.reportEntrys = reportEntrys;
	}

	public String getConditions() {
		return conditions;
	}

	public void setConditions(String conditions)
	{
		this.conditions = conditions;
	}

}
