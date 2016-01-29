/**
 * 
 */
package com.taobao.top.analysis.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author fangweng
 * 
 */
public class ReportOrderComparatorTest
{

	ReportOrderComparator<Double[]> orderComparator;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		int[] column = new int[] { 1, 2 };
		boolean[] orders = new boolean[] { true, false };
		orderComparator = new ReportOrderComparator<Double[]>(column, orders);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
	}

	/**
	 * Test method for
	 * {@link com.taobao.top.analysis.util.ReportOrderComparator#compare(java.lang.Object, java.lang.Object)}
	 * .
	 */
	@Test
	public void testCompare()
	{
		Double[] content1 = new Double[] { 11.1, 15.2, 98.4, 72.6 };
		Double[] content2 = new Double[] { 19.1, 25.2, 118.4, 72.6 };
		Double[] content3 = new Double[] { 19.1, 25.2, 78.4, 72.6 };

		List<Double[]> list = new ArrayList<Double[]>();
		list.add(content1);
		list.add(content2);
		list.add(content3);

		Collections.sort(list, orderComparator);

		Assert.assertEquals(content3, list.get(0));
		Assert.assertEquals(content2, list.get(1));
		Assert.assertEquals(content1, list.get(2));
	}

}
