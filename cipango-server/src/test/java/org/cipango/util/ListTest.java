package org.cipango.util;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class ListTest extends TestCase
{
	public void testList()
	{
		List<String> list = new ArrayList<String>();
		list.add("one"); list.add("two"); list.add("three"); list.add("four");
		
		for (int i = list.size(); i-->0;)
		{
			String s = list.get(i);
			list.remove(s);
			//System.out.println(s);
		}
	}
}
