package org.cipango.sip;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;
public class RAckTest
{
	@Test
	public void testParse() throws Exception
	{
		String s = "   775656    1    INVITE  ";
		
		RAck rack = new RAck(s);
		assertEquals(775656, rack.getRSeq());
		assertEquals(1, rack.getCSeq());
		assertEquals("INVITE", rack.getMethod());
	}
}
