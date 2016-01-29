/**
 * 
 */
package com.taobao.top.analysis.jobmanager;

import java.util.List;
import java.util.Map;
import com.taobao.top.analysis.data.Report;
import com.taobao.top.analysis.data.ReportAlert;

/**
 * 报表管理接口
 * 
 * @author fangweng
 * 
 */
public interface IReportManager extends IManager
{
	/**
	 * 创建最终报表文件
	 * 
	 * @param 分析后的结果池
	 * @param 报表定义库
	 * @param 需要存放报表的目录
	 * @param 是否需要加时间后缀
	 */
	public List<String> generateReports(
			Map<String, Map<String, Object>> resultPool,
			Map<String, Report> reportPool, String dir, boolean needTimeSuffix);

	/**
	 * 根据配置创建比对告警通知
	 * 
	 * @param 报表定义
	 * @param 告警设置
	 * @param 需要存放报表的目录
	 * @return
	 */
	public List<String> generateAlerts(Map<String, Report> reportPool,
			List<ReportAlert> alerts, String dir);

	/**
	 * 分发已经创建的报表
	 * 
	 * @param reports
	 * @param info
	 */
	public void dispatchReports(List<String> reports, String info);

	/**
	 * 分发已经处理完毕的告警信息
	 * 
	 * @param alerts
	 */
	public void dispatchAlerts(List<String> alerts);

}
