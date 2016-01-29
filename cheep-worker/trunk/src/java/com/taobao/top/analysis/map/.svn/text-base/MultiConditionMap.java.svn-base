package com.taobao.top.analysis.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.top.analysis.AnalysisConstants;
import com.taobao.top.analysis.data.Alias;
import com.taobao.top.analysis.data.ReportEntry;
import com.taobao.top.analysis.util.ReportUtil;

/**
 * mapParams中支持多个map，每个map可以有自己的参数，格式如下
 * map1;map1Params,map2;map2Params,map3;map3Params
 * 使用第一个map生成主键,其他map作为过滤条件
 * 
 * @author zhenzi 2010-11-15 下午01:29:10
 */
public class MultiConditionMap implements IReportMap {
	private static final Log logger = LogFactory.getLog(MultiConditionMap.class);
	private static final int c = ';';
	private ConcurrentHashMap<String, List<MapAndParam>> mapParamsClass = new ConcurrentHashMap<String, List<MapAndParam>>();

	@Override
	public String generateKey(ReportEntry entry, String[] contents,
			Map<String, Alias> aliasPool,String tempMapParams) {
		String mapParams = entry.getMapParams();
		if (StringUtils.isEmpty(mapParams)) {
			return ReportUtil.generateKey(entry, contents);
		}else{
			List<MapAndParam> multiMap = mapParamsClass.get(mapParams);
			if(multiMap == null){
				multiMap = analysisMapParams(mapParams);
				mapParamsClass.put(mapParams, multiMap);
			}
			if(multiMap.size() == 0){
				return AnalysisConstants.IGNORE_PROCESS;
			}
			MapAndParam generateKeyMap = multiMap.get(0);
			for(int i = 1,n = multiMap.size(); i < n;i++){
				if (AnalysisConstants.IGNORE_PROCESS.equals(multiMap.get(i).getMapInstance()
						.generateKey(entry, contents, aliasPool,
								multiMap.get(i).getMapParam()))) {
					return AnalysisConstants.IGNORE_PROCESS;
				}
			}
			String key = generateKeyMap.getMapInstance().generateKey(entry,
					contents, aliasPool, generateKeyMap.getMapParam());
			return key;
		}
	}

	private List<MapAndParam> analysisMapParams(String mapParams) {
		String[] params = StringUtils.split(mapParams, ",");
		List<MapAndParam> mapAndParamList = new ArrayList<MapAndParam>(
				params.length);

		for (int i = 0, n = params.length; i < n; i++) {
			String mapClassStr = null;
			String mapParam = null;

			int sepIndex = params[i].indexOf(c);
			if (sepIndex > -1) {
				mapClassStr = params[i].substring(0, sepIndex);
				mapParam = params[i]
						.substring(sepIndex + 1, params[i].length());
			} else {
				mapClassStr = params[i];
			}

			try {
				mapAndParamList.add(new MapAndParam((IReportMap) Class.forName(mapClassStr)
						.newInstance(), mapParam));
			} catch (Exception e) {
				logger.error(e, e);
				continue;
			}
		}
		if(mapAndParamList.size() == 0){
			logger.warn(new StringBuilder().append(" mapParam:").append(mapParams).append(" is not valid."));
		}
		return mapAndParamList;
	}
	/**
	 * 保存一个map的实例以及这个map的输入参数
	 * @author zhenzi
	 * 2010-12-7 上午07:58:22
	 */
	private class MapAndParam{
		private IReportMap mapInstance;
		private String mapParam;
		public MapAndParam(IReportMap mapInstance,String mapParam){
			this.mapInstance = mapInstance;
			this.mapParam = mapParam;
		}
		public IReportMap getMapInstance() {
			return mapInstance;
		}
		public String getMapParam() {
			return mapParam;
		}
	}
}
