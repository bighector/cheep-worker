package com.taobao.top.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.top.analysis.data.Alias;
import com.taobao.top.analysis.data.Report;
import com.taobao.top.analysis.data.ReportAlert;
import com.taobao.top.analysis.data.ReportEntry;
import com.taobao.top.analysis.data.Rule;
import com.taobao.top.analysis.util.ReportUtil;

public class ReportUtilTest
{

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	@Test
	public void testBuildReportModule()
	{
		Rule rule = new Rule();

		// ReportUtil.buildReportModule("file:c:/report.xml", entryPool,
		// reportPool);
		ReportUtil.buildReportModule("top-report-api2.xml", rule);

		Assert.assertTrue(rule.getEntryPool().size() > 0);
		Assert.assertTrue(rule.getReportPool().size() > 0);
	}

}
