package org.cipango.diameter.io;

import org.cipango.diameter.base.Common;
import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;

import junit.framework.TestCase;

public class CodecTest extends TestCase
{
	public void testSize() throws Exception
	{
		Buffer buffer = new ByteArrayBuffer(1);
		for (int i = 0; i < 10000; i++)
		{
			buffer = Common.__unsigned32.encode(buffer, i);
		}
		
		for (int i = 0; i < 10000; i++)
		{
			assertTrue(buffer.hasContent());
			assertEquals(i, (int) Common.__unsigned32.decode(buffer)); 
		}
		assertFalse(buffer.hasContent());
	}
}
