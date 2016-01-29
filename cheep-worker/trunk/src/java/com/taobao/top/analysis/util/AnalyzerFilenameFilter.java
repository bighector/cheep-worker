/**
 * 
 */
package com.taobao.top.analysis.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * 需要分析的日志文件过滤类，根据后缀名
 * 
 * @author fangweng
 * 
 */
public class AnalyzerFilenameFilter implements FilenameFilter
{
	String extension;

	public AnalyzerFilenameFilter(String ext)
	{
		extension = ext;
	}

	@Override
	public boolean accept(File dir, String name)
	{

		if (name.endsWith(extension))
			return true;
		else
			return false;
	}
}
