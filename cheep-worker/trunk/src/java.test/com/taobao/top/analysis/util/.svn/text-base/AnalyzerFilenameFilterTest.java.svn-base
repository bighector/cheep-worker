/**
 * 
 */
package com.taobao.top.analysis.util;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author fangweng
 * 
 */
public class AnalyzerFilenameFilterTest
{

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	/**
	 * Test method for
	 * {@link com.taobao.top.analysis.util.AnalyzerFilenameFilter#accept(java.io.File, java.lang.String)}
	 * .
	 */
	@Test
	public void testAccept()
	{
		AnalyzerFilenameFilter filer = new AnalyzerFilenameFilter(".data");

		Assert.assertTrue(filer.accept(null, "1.data"));
		Assert.assertFalse(filer.accept(null, "1.dat"));

	}

}
