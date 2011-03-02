package org.cipango.diameter.io;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.cipango.diameter.base.Common;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.junit.Test;

public class CodecTest
{
	@Test
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
