package com.taobao.top.analysis.map;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.top.analysis.AnalysisConstants;
import com.taobao.top.analysis.data.Alias;
import com.taobao.top.analysis.data.ReportEntry;
import com.taobao.top.analysis.util.ReportUtil;
/**
 * 有时候需要有这样的条件：字段被包含在一个集合中，条件就成立，
 * 但是这个集合里有很多内容，所以写成Map的形式。
 * mapParams=文件名的绝对路径"
 * 文件内容的格式为
 * tag=xxx,xx,xx
 * nick=xx,xx,xx
 * 每个字段的配置必须在一行 
 * @author zhenzi
 * 2010-12-14 上午09:14:01
 */
public class ORConditionMap implements IReportMap {
	private static final Log logger = LogFactory.getLog(ORConditionMap.class);
	private ReentrantLock lock = new ReentrantLock();
	public ORConditionMap(){
		Thread t = new Thread(new RebuildWork(),"ORConditionMap-RebuildWorkThread");
		t.setDaemon(true);
		t.start();
	}
	/**
	 * 用于保存每个数据的集合
	 */
	private ConcurrentHashMap<String, MapParam> value = new ConcurrentHashMap<String,MapParam>();
	@Override
	public String generateKey(ReportEntry entry, String[] contents,
			Map<String, Alias> aliasPool, String tempMapParams) {
		String reportKey = ReportUtil.generateKey(entry, contents);
		if (AnalysisConstants.IGNORE_PROCESS.equals(reportKey))
			return AnalysisConstants.IGNORE_PROCESS;
		
		String mapParam = tempMapParams == null ? entry.getMapParams():tempMapParams;
		
		MapParam mp = value.get(mapParam);  
		if(mp == null){
			boolean lockReturn = false;
			try{
				lockReturn = lock.tryLock(500, TimeUnit.MILLISECONDS);
				if(lockReturn){
					mp = value.get(mapParam);
					if(mp == null){
						mp = buildData(mapParam,value);
					}
				}else {
					return AnalysisConstants.IGNORE_PROCESS;
				}
			}catch(Exception e){
				logger.error("lock error");
			}finally{
				if(lockReturn){
					lock.unlock();
				}
			}	
		}
		Map<String,Set<String>> keyValues = mp.getKeyValues();
		if(keyValues == null || (keyValues != null && keyValues.size() == 0)){
			return AnalysisConstants.IGNORE_PROCESS;
		}
		Iterator<Entry<String,Set<String>>> it = keyValues.entrySet().iterator();
		boolean isContain = false; 
		while(it.hasNext()){
			Entry<String,Set<String>> e = it.next();
			if(e.getValue().contains(contents[Integer.valueOf(aliasPool.get(e.getKey()).getKey()) - 1])){
				isContain = true;
				break;
			}
		}
		if(isContain){
			return reportKey;
		}else{
			return AnalysisConstants.IGNORE_PROCESS;
		}
	}
	private MapParam buildData(String mapParam,ConcurrentHashMap<String, MapParam> value){
		MapParam mp = null;
		File f = new File(mapParam);		
		InputStreamReader inReader = null;
		try{
			inReader = new InputStreamReader(new FileInputStream(f),"utf-8");
			
			Properties p = new Properties(); 
			p.load(inReader);
			Map<String,Set<String>> keyValues = new HashMap<String,Set<String>>();
			
			Iterator<Entry<Object, Object>> it = p.entrySet().iterator();
			while(it.hasNext()){
				Entry<Object,Object> e = it.next();
				String[] valueStr = StringUtils.split(e.getValue().toString(), ',');
				Set<String> valueSet = new HashSet<String>(valueStr.length);
				for (String str : valueStr) {
					valueSet.add(str);
				}
				keyValues.put(e.getKey().toString(), valueSet);
			}
			if(keyValues.size() == 0){
				logger.warn(new StringBuilder("mapparam ").append(mapParam).append("is not valid:"));
			}
			mp = new MapParam(mapParam,f.lastModified(),keyValues);
			value.put(mapParam, mp);
			return mp;
		}catch(Exception e){
			logger.error("build data error");
		}finally{
			if(inReader != null){
				try {
					inReader.close();
				} catch (IOException e) {
					logger.error("close file error");
				}
			}
			if(value.get(mapParam) == null){
				mp = new MapParam(mapParam,0,new HashMap<String,Set<String>>());
				value.put(mapParam,mp);
			}
		}
		return mp;
	}
	
	private class RebuildWork implements Runnable{
		private static final long INTERVAL = 10 * 60 * 1000;//间隔10分钟扫描文件
		@Override
		public void run() {
			try{
				Thread.sleep(INTERVAL);//休眠10分钟
				boolean changed = false;
				ConcurrentHashMap<String, MapParam> newvalue = new ConcurrentHashMap<String,MapParam>();
				Iterator<Entry<String,MapParam>> it = value.entrySet().iterator();
				while(it.hasNext()){
					Entry<String,MapParam> e = it.next();
					MapParam mp = e.getValue();
					if(new File(mp.getFileName()).lastModified() != mp.getLastModified()){
						logger.warn(new StringBuilder("file ").append(e.getKey()).append("changed"));
						buildData(e.getKey(),newvalue);
						changed = true;
					}else{
						newvalue.put(e.getKey(), e.getValue());
					}
				}
				if(changed){
					value = newvalue;
				}else{
					newvalue = null;
				}
			}catch(Exception e){
				logger.error(e, e);
			}
		}
	}
	private class MapParam{
		private String fileName;
		private long lastModified;
		private Map<String,Set<String>> keyValues;
		public MapParam(String fileName,long lastModified,Map<String,Set<String>> keyValues){
			this.fileName = fileName;
			this.lastModified = lastModified;
			this.keyValues = keyValues;
		}
		public String getFileName() {
			return fileName;
		}
		public long getLastModified() {
			return lastModified;
		}
		public Map<String, Set<String>> getKeyValues() {
			return keyValues;
		}
	}
}
