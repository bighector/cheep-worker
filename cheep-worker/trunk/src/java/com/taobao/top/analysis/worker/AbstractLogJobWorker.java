package com.taobao.top.analysis.worker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.top.analysis.AnalysisConstants;
import com.taobao.top.analysis.data.Alias;
import com.taobao.top.analysis.data.ReportEntry;
import com.taobao.top.analysis.data.ReportEntryValueType;
import com.taobao.top.analysis.map.IReportMap;
import com.taobao.top.analysis.reduce.IReportReduce;
import com.taobao.top.analysis.util.ReportUtil;

/**
 * 日志工作者抽象类
 * 
 * @author fangweng
 * 
 */
public abstract class AbstractLogJobWorker implements IWorker
{
	private static final Log logger = LogFactory
			.getLog(AbstractLogJobWorker.class);

	protected CountDownLatch countDownLatch;
	protected String workerName;
	protected AtomicLong errorCounter;

	/**
	 * entry规则定义元池
	 */
	protected Map<String, ReportEntry> entryPool;
	/**
	 * 父亲entry规则定义元池
	 */
	protected Map<String, ReportEntry> parentEntryPool;

	/**
	 * 别名定义池
	 */
	protected Map<String, Alias> aliasPool;
	/**
	 * 处理后的结果池，key是entry的id， value是Map(key是entry定义的key组合,value是统计后的结果)
	 * 采用线程不安全，只有单线程操作此结果集
	 */
	protected Map<String, Map<String, Object>> resultPool;
	/**
	 * 脚本执行管理类，动态执行统计结果中的简单运算
	 */
	protected ScriptEngineManager manager;
	protected ScriptEngine engine;

	/**
	 * 是否成功
	 */
	protected boolean success = true;

	public boolean isSuccess()
	{
		return success;
	}

	public void setSuccess(boolean success)
	{
		this.success = success;
	}

	public CountDownLatch getCountDownLatch()
	{
		return countDownLatch;
	}

	public void setCountDownLatch(CountDownLatch countDownLatch)
	{
		this.countDownLatch = countDownLatch;
	}

	public String getWorkerName()
	{
		return workerName;
	}

	public void setWorkerName(String workerName)
	{
		this.workerName = workerName;
	}

	public AtomicLong getErrorCounter()
	{
		return errorCounter;
	}

	public void setErrorCounter(AtomicLong errorCounter)
	{
		this.errorCounter = errorCounter;
	}

	public Map<String, ReportEntry> getEntryPool()
	{
		return entryPool;
	}

	public void setEntryPool(Map<String, ReportEntry> entryPool)
	{
		this.entryPool = entryPool;
	}

	public Map<String, ReportEntry> getParentEntryPool()
	{
		return parentEntryPool;
	}

	public void setParentEntryPool(Map<String, ReportEntry> parentEntryPool)
	{
		this.parentEntryPool = parentEntryPool;
	}

	public Map<String, Alias> getAliasPool()
	{
		return aliasPool;
	}

	public void setAliasPool(Map<String, Alias> aliasPool)
	{
		this.aliasPool = aliasPool;
	}

	public Map<String, Map<String, Object>> getResultPool()
	{
		return resultPool;
	}

	public void setResultPool(Map<String, Map<String, Object>> resultPool)
	{
		this.resultPool = resultPool;
	}

	@Override
	public void run()
	{

		try
		{
			init();

			doJob();

			destory();
		}
		catch (Exception ex)
		{
			logger.error(ex, ex);
			this.setSuccess(false);
		}
		finally
		{
			if (countDownLatch != null)
			{
				countDownLatch.countDown();

				if (logger.isInfoEnabled())
					logger.info(new StringBuilder().append("Worker ").append(
							workerName).append(" end").append(".  Leave Job ")
							.append(countDownLatch.getCount()).append(
									" not end...").toString());
			}
		}
	}

	public void process(ReportEntry entry, String[] contents,
			Map<String, Object> valueTempPool) throws ScriptException
	{
		boolean isChild = true;
		if (entry.getParent() == null)
		{
			isChild = false;
		}

		Map<String, Object> entryResult = resultPool.get(entry.getId());

		if (entryResult == null)
		{
			entryResult = new HashMap<String, Object>();
			resultPool.put(entry.getId(), entryResult);
		}

		String key = null;
		Object value = null;
		// 如果是孩子
		if (isChild)
		{
			String parent = entry.getParent();
			ReportEntry parentEntry = entryPool.get(parent);
			value = valueTempPool.get(parentEntry.getId());
			if (value == null)
			{
				return;
			}
			else
			{
				if (entry.getKeys() == null)
				{
					entry.setKeys(parentEntry.getKeys());
					entry.setValueType(parentEntry.getValueType());
					if (entry.getFormatStack() == null)
					{
						entry.setFormatStack(parentEntry.getFormatStack());
					}
				}

			}
		}

		// 增加全局MapClass的处理
		if (entry.getGlobalMapClass() != null
				&& entry.getGlobalMapClass().size() > 0)
		{
			for (String mc : entry.getGlobalMapClass())
			{
				IReportMap mapClass = ReportUtil.getInstance(IReportMap.class,
						Thread.currentThread().getContextClassLoader(), mc,
						true);

				key = mapClass.generateKey(entry, contents, aliasPool,null);

				if (key.equals(AnalysisConstants.IGNORE_PROCESS))
				{
					return;
				}
			}
		}

		if (entry.getMapClass() == null || "".equals(entry.getMapClass()))
		{
			if (key == null)
				key = ReportUtil.generateKey(entry, contents);
		}
		else
		{
			IReportMap mapClass = ReportUtil.getInstance(IReportMap.class,
					Thread.currentThread().getContextClassLoader(), entry
							.getMapClass(), true);

			key = mapClass.generateKey(entry, contents, aliasPool,null);
		}

		// 该内容忽略，不做统计
		if (key.equals(AnalysisConstants.IGNORE_PROCESS))
		{
			return;
		}

		if (key.equals(""))
			throw new java.lang.RuntimeException("JobWorker create key error!");

		if (!isChild)
		{
			if (entry.getReduceClass() != null
					&& !"".equals(entry.getReduceClass()))
			{
				IReportReduce reduceClass = ReportUtil.getInstance(
						IReportReduce.class, Thread.currentThread()
								.getContextClassLoader(), entry
								.getReduceClass(), true);

				value = reduceClass.generateValue(entry, contents, aliasPool);
			}
			else
				value = ReportUtil.generateValue(entry, contents);

		}
		else
		{
			if (value.equals("NULL"))
			{
				value = null;
			}

		}
		// value filter inject
		if (entry.getValueType() != ReportEntryValueType.COUNT
				&& entry.getValuefilterStack() != null
				&& entry.getValuefilterStack().size() > 0)
		{
			if (!ReportUtil.checkValue(entry.getValuefilterOpStack(), entry
					.getValuefilterStack(), value))
				return;
		}

		if (!isChild)
		{

			if (parentEntryPool.get(entry.getId()) != null)
			{
				if (value == null)
				{
					valueTempPool.put(entry.getId(), "NULL");
				}
				else
				{
					valueTempPool.put(entry.getId(), value);
				}
			}
		}

		switch (entry.getValueType())
		{
			case AVERAGE:
				if (value == null)
					return;

				value = Double.parseDouble(value.toString());

				String sumkey = new StringBuilder().append("a:sum").append(key)
						.toString();
				String countkey = new StringBuilder().append("a:count").append(
						key).toString();

				Double sum = (Double) entryResult.get(sumkey);
				Double count = (Double) entryResult.get(countkey);

				if (sum == null || count == null)
				{
					entryResult.put(sumkey, (Double) value);
					entryResult.put(countkey, (Double) 1.0);
					entryResult.put(key, (Double) value);
				}
				else
				{
					// 再次验证一下
					Object tempvalue = ((Double) value + sum)
							/ (Double) (count + 1);

					entryResult.put(sumkey, (Double) value + sum);
					entryResult.put(countkey, (Double) (count + 1));
					entryResult.put(key, tempvalue);
				}

				break;

			case SUM:

				if (value == null)
					return;

				if (value instanceof String)
				{
					value = Double.parseDouble((String) value);
				}

				Double _sum = (Double) entryResult.get(key);

				if (_sum == null)
					entryResult.put(key, (Double) value);
				else
					entryResult.put(key, (Double) value + _sum);

				break;

			case MIN:
				if (value == null)
					return;

				if (value instanceof String)
				{
					value = Double.parseDouble((String) value);
				}

				Double min = (Double) entryResult.get(key);

				if (min == null)
					entryResult.put(key, (Double) value);
				else if ((Double) value < min)
					entryResult.put(key, (Double) value);

				break;

			case MAX:
				if (value == null)
					return;

				if (value instanceof String)
				{
					value = Double.parseDouble((String) value);
				}

				Double max = (Double) entryResult.get(key);

				if (max == null)
					entryResult.put(key, (Double) value);
				else if ((Double) value > max)
					entryResult.put(key, (Double) value);

				break;

			case COUNT:

				Double total = (Double) entryResult.get(key);

				if (total == null)
					entryResult.put(key, (Double) 1.0);
				else
					entryResult.put(key, total + 1);

				break;

			case PLAIN:

				Object o = entryResult.get(key);

				if (o == null)
					entryResult.put(key, value);

				break;

		}

	}

}
