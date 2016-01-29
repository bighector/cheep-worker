/**
 * 
 */
package com.taobao.top.analysis.worker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import com.taobao.top.analysis.TopAnalysisConfig;
import com.taobao.top.analysis.util.FTPUtil;

/**
 * 
 * 从数据源拖拉日志文件，当前通过ftp获得文件
 * 
 * @author fangweng
 * 
 */
public class PullFileJobWorker implements IWorker
{

	private static final Log logger = LogFactory
			.getLog(PullFileJobWorker.class);

	private String username = "pubftp";
	private String password = "look";
	private String targetDir = "d:\\testlog";
	FTPClient ftp;
	private int workerNum = 5;
	private ExecutorService jobExecuter;
	Calendar calendar = Calendar.getInstance();
	Map<String, String> exists;
	Map<String, String> jobfiles;
	private String filelist;
	private boolean pullflag = true;

	/**
	 * 全局配置
	 */
	protected TopAnalysisConfig topAnalyzerConfig;

	public void setTopAnalyzerConfig(TopAnalysisConfig topAnalyzerConfig)
	{
		this.topAnalyzerConfig = topAnalyzerConfig;
	}

	public void setPullflag(boolean pullflag)
	{
		this.pullflag = pullflag;
	}

	public String getFilelist()
	{
		return filelist;
	}

	public void setFilelist(String filelist)
	{
		this.filelist = filelist;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getTargetDir()
	{
		return targetDir;
	}

	public void setTargetDir(String targetDir)
	{
		this.targetDir = targetDir;
	}

	public int getWorkerNum()
	{
		return workerNum;
	}

	public void setWorkerNum(int workerNum)
	{
		this.workerNum = workerNum;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.taobao.top.analysis.worker.IWorker#init()
	 */
	@Override
	public void init()
	{

		targetDir = new StringBuilder(targetDir).append(File.separator).append(
				calendar.get(Calendar.YEAR)).append("-").append(
				calendar.get(Calendar.MONTH) + 1).append("-").append(
				calendar.get(Calendar.DAY_OF_MONTH)).toString();

		new File(targetDir).mkdirs();

		File[] files = new File(targetDir).listFiles();

		exists = new HashMap<String, String>();
		jobfiles = new HashMap<String, String>();

		if (files != null && files.length > 0)
		{
			for (File file : files)
			{
				exists.put(file.getName(), file.getName());
			}
		}

		if (filelist != null && filelist.length() > 0)
		{
			String[] fs = filelist.split(",");

			for (String f : fs)
			{
				jobfiles.put(f, f);
			}
		}

		jobExecuter = Executors.newFixedThreadPool(workerNum);

		ftp = null;

		try
		{
			ftp = FTPUtil.getFtpClient(topAnalyzerConfig);
		}
		catch (Exception ex)
		{
			if (ftp != null && ftp.isConnected())
			{
				try
				{
					ftp.disconnect();
				}
				catch (IOException f)
				{
					// do nothing
				}
			}

			logger.error("PullFileJobWorker connect ftp error!", ex);
			// throw new
			// java.lang.RuntimeException("PullFileJobWorker connect ftp error!");
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.taobao.top.analysis.worker.IWorker#destory()
	 */
	@Override
	public void destory()
	{
		if (ftp.isConnected())
		{
			try
			{
				ftp.disconnect();
			}
			catch (IOException f)
			{
				// do nothing
			}
		}

		if (jobExecuter != null)
		{
			jobExecuter.shutdown();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.taobao.top.analysis.worker.IWorker#doJob()
	 */
	@Override
	public void doJob()
	{

		try
		{
			FTPFile[] files = ftp.listFiles();
			List<FTPFile> downloads = new ArrayList<FTPFile>();

			if (files != null && files.length > 0)
			{
				for (FTPFile f : files)
				{
					if (topAnalyzerConfig.isInMatchFiles(f.getName()))
					{
						// 只是获取列表
						if (!pullflag)
						{
							jobfiles.put(f.getName(), f.getName());
						}
						else
						{

							if (exists != null && exists.size() > 0)
							{
								if (exists.get(f.getName()) != null)
								{
									continue;
								}
							}

							if (jobfiles == null
									|| (jobfiles != null && jobfiles.size() == 0))
								downloads.add(f);
							else if (jobfiles.get(f.getName()) != null)
								downloads.add(f);

						}
					}

				}
			}

			// 只是获取列表
			if (!pullflag)
				return;

			for (FTPFile f : downloads)
			{
				final FTPFile tf = f;

				OutputStream output = null;

				try
				{
					output = new FileOutputStream(targetDir + File.separator
							+ tf.getName());

					ftp.retrieveFile(tf.getName(), output);

					// 删除ftp服务器上的数据
					// ftp.deleteFile(tf.getName());
				}
				catch (Exception e)
				{
					handleError(e, "doJob error!");
				}
				finally
				{
					if (output != null)
						try
						{
							output.close();
						}
						catch (IOException e)
						{
							handleError(e, "doJob error!");
						}
				}
			}

			if (downloads != null && downloads.size() > 0)
			{
				String OS = System.getProperty("os.name").toLowerCase();

				String unzipCommand;

				if (OS.indexOf("linux") >= 0)
				{
					String command = "sh " + System.getProperty("user.dir")
							+ "/unzip.sh " + targetDir;
					Process process = Runtime.getRuntime().exec(command);

					process.waitFor();
				}
				else
				{
					unzipCommand = new StringBuilder().append(
							topAnalyzerConfig.getUnzipCommand()).append(" x ")
							.append(targetDir).append(File.separator).append(
									"*.tgz ").append(targetDir).append(
									File.separator).toString();

					Process process = Runtime.getRuntime().exec(unzipCommand);

					process.waitFor();
				}

				File[] logfiles = new File(targetDir).listFiles();

				for (File f : logfiles)
				{
					if (f.getName().endsWith(".tgz"))
						continue;

					f.renameTo(new File(f.getAbsolutePath() + ".log"));
				}
			}
		}
		catch (Exception ex)
		{
			handleError(ex, "doJob error!");
		}

	}

	public Map<String, String> getJobfiles()
	{
		return jobfiles;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taobao.top.analysis.worker.IWorker#handleError(java.lang.Exception,
	 * java.lang.Object)
	 */
	@Override
	public void handleError(Exception ex, Object detail)
	{
		logger.error(detail, ex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{

		init();

		try
		{
			doJob();
		}
		finally
		{
			destory();
		}

	}

}
