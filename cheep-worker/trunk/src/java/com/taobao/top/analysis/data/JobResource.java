/**
 * 
 */
package com.taobao.top.analysis.data;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.taobao.top.analysis.util.AnalyzerFilenameFilter;

/**
 * 解析引擎的配置文件目录,用于监控是否有文件修改 线程不安全,注意
 * 
 * @author fangweng
 * 
 */
public class JobResource
{
	private final Log logger = LogFactory.getLog(JobResource.class);
	
	private File resource;
	private Map<String, String> files;
	private long lastLoadTime;

	public JobResource(String path)
	{
		if (path == null)
			throw new java.lang.RuntimeException("JobResource path can't be null...");
		
		if (path.startsWith("file:"))
			resource = new File(path.substring("file:".length()));
		else
			resource = new File(path);
		
		
		lastLoadTime = System.currentTimeMillis();

		if (!resource.exists())
		{
			if (!path.startsWith("file:"))
			{
				URL url = Thread.currentThread()
						.getContextClassLoader().getResource(path);
				
				if (url == null)
					throw new java.lang.RuntimeException("It is not a validate jobResource..."
							+ path);
				else
					resource = new File(url.getFile());
			}
			else
				throw new java.lang.RuntimeException("It is not a validate jobResource..."
					+ path);
		}			

		files = new HashMap<String, String>();

		if (resource.isDirectory())
		{
			File[] fs = resource.listFiles(new AnalyzerFilenameFilter(".xml"));

			for (File f : fs)
			{
				files.put(f.getName(), f.getName());
			}
		}
		else
			files.put(resource.getName(), resource.getName());
		

	}

	public File getResource()
	{
		return resource;
	}

	/**
	 * 判断是否被修改
	 * 
	 * @return
	 */
	public boolean isModify()
	{
		boolean result = false;

		if (resource.isDirectory())
		{
			File[] fs = resource.listFiles(new AnalyzerFilenameFilter(".xml"));

			for (File f : fs)
			{
				if ((f.lastModified() > lastLoadTime)
						|| (files.get(f.getName()) == null))
				{
					result = true;
					
					logger.info("file: " + f.getName() 
							+ "is modify," + "lastloadtime:" + lastLoadTime 
							+ ",file time:" + f.lastModified());
					break;
				}

			}
		}
		else
		{
			if(resource.lastModified() > lastLoadTime)
			{
				result = true;
			}
		}
		

		return result;
	}

	/**
	 * 重新载入,如果newPath是null，就直接更新原有目录
	 * 
	 * @param newPath
	 */
	public void reload(String newPath)
	{
		if (newPath != null)
		{
			resource = new File(newPath);

			if (!resource.exists() || (resource.exists() && !resource.isDirectory()))
				throw new java.lang.RuntimeException(
						"It is not a validate dir..." + newPath);
		}

		files.clear();

		if (resource.isDirectory())
		{
			File[] fs = resource.listFiles(new AnalyzerFilenameFilter(".xml"));

			for (File f : fs)
			{
				files.put(f.getName(), f.getName());
			}
		}
		else
		{
			files.put(resource.getName(), resource.getName());
		}
		

		lastLoadTime = System.currentTimeMillis();
	}

}
