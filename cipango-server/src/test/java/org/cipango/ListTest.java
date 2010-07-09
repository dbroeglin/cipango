package org.cipango;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.jetty.util.LazyList;

public class ListTest extends TestCase
{
	public void testRemove()
	{
		List<Integer> l = new ArrayList<Integer>();
		l.add(1);
		l.add(2);
		l.add(3);
		
		Iterator<Integer> it = l.iterator();
		
		while (it.hasNext())
		{
			System.out.println(it.next());
			it.remove();
		}
		System.out.println(l.size());
		
	}
	
	public void testLazy()
	{
		long start = System.currentTimeMillis();
		long memory = Runtime.getRuntime().freeMemory();
		
		for (int i = 0; i < 10000000; i++)
		{
			Object list = null;
			list = LazyList.add(list, "hello world");
			list = LazyList.add(list, "hello world2");
			
			int size = LazyList.size(list);
			String s = (String) LazyList.get(list, 0);
			assertEquals(2, size);
			assertEquals("hello world", s);
		}
		
		System.out.println("lazy " + (System.currentTimeMillis() - start));
	}
	
	public void testList()
	{
		long start = System.currentTimeMillis();
		long memory = Runtime.getRuntime().freeMemory();
		
		for (int i = 0; i < 10000000; i++)
		{
			List<String> list = new ArrayList<String>(2);
			list.add("hello world");
			list.add("hello world2");
			
			int size = list.size();
			String s = list.get(0);
			assertEquals(2, size);
			assertEquals("hello world", s);
		}
		
		System.out.println(System.currentTimeMillis() - start);
	}
}
