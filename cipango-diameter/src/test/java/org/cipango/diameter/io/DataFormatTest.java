// ========================================================================
// Copyright 2009 NEXCOM Systems
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================
package org.cipango.diameter.io;
import static junit.framework.Assert.assertEquals;

import java.net.InetAddress;
import java.util.Date;

import org.cipango.diameter.base.Common;
import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.junit.Before;
import org.junit.Test;

public class DataFormatTest
{
	private Buffer _buffer;
	
	@Before
	public void setUp() 
	{
		_buffer = new ByteArrayBuffer(1024);
	}
	
	@Test
	public void testOctetString() throws Exception
	{
		byte[] b = "cipango".getBytes();
		Common.__octetString.encode(_buffer, b);

		assertEquals("cipango", new String(Common.__octetString.decode(_buffer)));
	}
	
	@Test
	public void testUtf8String() throws Exception
	{
		String s = "Û�fjRPsl0ˆ¤";
		Common.__utf8String.encode(_buffer, s);
		
		assertEquals(s, Common.__utf8String.decode(_buffer));
	}
	
	@Test
	public void testUnsigned32() throws Exception
	{
		int i = 123456789;
		Common.__unsigned32.encode(_buffer, i);
		assertEquals(i, (int) Common.__unsigned32.decode(_buffer));
	}
	
	@Test
	public void testAddress() throws Exception
	{
		InetAddress address = InetAddress.getByName("127.0.0.1");
		Common.__address.encode(_buffer, address);
		assertEquals(address, Common.__address.decode(_buffer));
		
		address = InetAddress.getByName("[::1]");
		Common.__address.encode(_buffer, address);
		assertEquals(address, Common.__address.decode(_buffer));
	}
	
	@Test
	public void testDate() throws Exception
	{
		Date date = new Date();
		Common.__date.encode(_buffer, date);
		assertEquals(date.getTime() /1000, Common.__date.decode(_buffer).getTime() / 1000);
	}
}
