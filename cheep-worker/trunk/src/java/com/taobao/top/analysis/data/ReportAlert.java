/**
 * 
 */
package com.taobao.top.analysis.data;

/**
 * 报表告警定义
 * 
 * @author fangweng
 * 
 */
public class ReportAlert implements java.io.Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4494677880176118602L;

	private String reportId;
	private String keyentry;
	private String entryname;
	private String alerttype;
	private String valve;

	public String getKeyentry()
	{
		return keyentry;
	}

	public void setKeyentry(String keyentry)
	{
		this.keyentry = keyentry;
	}

	public String getReportId()
	{
		return reportId;
	}

	public void setReportId(String reportId)
	{
		this.reportId = reportId;
	}

	public String getEntryname()
	{
		return entryname;
	}

	public void setEntryname(String entryname)
	{
		this.entryname = entryname;
	}

	public String getAlerttype()
	{
		return alerttype;
	}

	public void setAlerttype(String alerttype)
	{
		this.alerttype = alerttype;
	}

	public String getValve()
	{
		return valve;
	}

	public void setValve(String valve)
	{
		this.valve = valve;
	}

}
