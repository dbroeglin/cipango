package org.cipango.util;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import org.junit.Test;

public class LazyMapTest
{
	@Test
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
