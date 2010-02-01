package org.cipango.util;

import junit.framework.TestCase;

public class LazyMapTest extends TestCase
{
	public void testMap() throws Exception
	{
		Object map = null;
		
		assertNull(LazyMap.get(map, "bob"));
		
		map = LazyMap.add(map, "bob", "sip:bob@biloxi.com");
		assertEquals(LazyMap.get(map, "bob"), "sip:bob@biloxi.com");
		
		map = LazyMap.add(map, "alice", "sip:alice@atlanta.com");
		assertEquals(LazyMap.get(map, "alice"), "sip:alice@atlanta.com");
	}
}
