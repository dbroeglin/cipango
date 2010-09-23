package org.cipango.sip;

import junit.framework.TestCase;

public class RAckTest extends TestCase
{
	public void testParse() throws Exception
	{
		String s = "   775656    1    INVITE  ";
		
		RAck rack = new RAck(s);
		assertEquals(775656, rack.getRSeq());
		assertEquals(1, rack.getCSeq());
		assertEquals("INVITE", rack.getMethod());
	}
}
