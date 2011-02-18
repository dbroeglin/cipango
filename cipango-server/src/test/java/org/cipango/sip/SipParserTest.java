// ========================================================================
// Copyright 2007-2008 NEXCOM Systems
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


package org.cipango.sip;
import static junit.framework.Assert.assertEquals;

import java.io.IOException;

import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.TypeUtil;
import org.junit.Test;

public class SipParserTest
{
	@Test
	public void testRequestLine() throws Exception
	{
		ByteArrayBuffer buffer = new ByteArrayBuffer("INVITE sip:foo.org SIP/2.0\r\n\r\n".getBytes(StringUtil.__UTF8));
		
		SipParser parser = new SipParser(buffer, new Handler());
		parser.parse();
		
		assertEquals("INVITE", _t0);
		assertEquals("sip:foo.org", _t1);
		assertEquals("SIP/2.0", _t2);
	}

	@Test
	public void testStatusLine() throws Exception
	{
		ByteArrayBuffer buffer = new ByteArrayBuffer("SIP/2.0 999 Foo\r\n\r\n".getBytes(StringUtil.__UTF8));
		
		SipParser parser = new SipParser(buffer, new Handler());
		parser.parse();
		
		assertEquals("SIP/2.0", _t0);
		assertEquals("999", _t1);
		assertEquals("Foo", _t2);
	}

	@Test
	public void testSpace() throws Exception
	{
		ByteArrayBuffer buffer = new ByteArrayBuffer(
				("INVITE sip:foo.org SIP/2.0\015\012"
					+ "foo:     bar   \015\012"
					+ "\015\012").getBytes(StringUtil.__UTF8));	
		SipParser parser = new SipParser(buffer, new Handler());
		parser.parse();
		
        assertEquals("foo", _hdr[0]);
        assertEquals("bar", _val[0]);
	}

	@Test
	public void testHeader() throws Exception
	{
		ByteArrayBuffer buffer = new ByteArrayBuffer(
				("INVITE sip:foo.org SIP/2.0\015\012"
					+ "Header1: value1\015\012"
					+ "Header2  :   value 2a  \015\012"
					+ "                    value 2b  \015\012"
					+ "Header3: \015\012"
					+ "Header4 \015\012"
					+ "  value4\015\012"
					+ "\015\012").getBytes(StringUtil.__UTF8));	
		SipParser parser = new SipParser(buffer, new Handler());
		parser.parse();
		
		assertEquals("INVITE", _t0);
        assertEquals("sip:foo.org", _t1);
        assertEquals("SIP/2.0", _t2);
        assertEquals("Header1", _hdr[0]);
        assertEquals("value1", _val[0]);
        assertEquals("Header2", _hdr[1]);
        assertEquals("value 2a value 2b", _val[1]);
        assertEquals("Header3", _hdr[2]);
        assertEquals("", _val[2]);
        assertEquals("Header4", _hdr[3]);
        assertEquals("value4", _val[3]);
        assertEquals(3, _h);
	}

	@Test
	public void testopid()
	{
		int i = 20801;
		byte[] b = new byte[16];
		b[15] = (byte) (i & 0xff);
		b[14] = (byte) (i >> 8 & 0xff);
		b[13] = (byte) (i >> 16 & 0xff);
		b[12] = (byte) (i >> 24 & 0xff);
		System.out.println(TypeUtil.toHexString(b));
		
	}

	@Test
	public void testHeaderCRLF() throws Exception
	{
		ByteArrayBuffer buffer = new ByteArrayBuffer(
				("INVITE sip:foo.org SIP/2.0\015\012"
					+ "Header1: \"value1\r\nvalue2\"\r\n\r\n").getBytes(StringUtil.__UTF8));	
		SipParser parser = new SipParser(buffer, new Handler());
		parser.parse();
		
		System.out.println(_val[0]);
		System.out.println(_hdr[1]);
		
	}

	@Test
	public void testCached() throws Exception 
	{
		ByteArrayBuffer buffer = new ByteArrayBuffer(_msg.getBytes(StringUtil.__UTF8));	
		SipParser parser = new SipParser(buffer, new Handler());
		parser.parse();
	}
	
	/*
	public void testPerfFraming() throws Exception
	{

		ByteArrayBuffer b = new ByteArrayBuffer(_msg.getBytes(StringUtil.__UTF8));
		SipParser parser = new SipParser(b, new Handler2());
		
		int nb = 1000000;
		long start = System.currentTimeMillis();
		for (int i = 0; i < nb; i++)
		{
			parser.parse();
			parser.setBuffer(b);
			b.setGetIndex(0);
		}
		System.out.println("Framing perfs: " + (nb / ((System.currentTimeMillis() - start) / 1000f)));
	}
	
	public void testPerfParsing() throws Exception
	{
		ByteArrayBuffer b = new ByteArrayBuffer(_msg.getBytes(StringUtil.__UTF8));
		SipParser parser = new SipParser(b, new Handler());
		
		int nb = 100000;
		long start = System.currentTimeMillis();
		for (int i = 0; i < nb; i++)
		{
			parser.parse();
			parser.setBuffer(b);
			b.setGetIndex(0);
		}
		System.out.println("Parsing perfs: " + (nb / ((System.currentTimeMillis() - start) / 1000f)));
	}
	*/
	
    String _t0;
    String _t1;
    String _t2;
    
    String[] _hdr;
    String[] _val;
    int _h;
    
    class Handler2 extends SipParser.EventHandler
    {
    	
    }
    
	class Handler extends SipParser.EventHandler
	{   
		public void header(Buffer name, Buffer value) throws IOException
		{
			_hdr[++_h] = name.toString();
			_val[_h] = value.toString();
		}
		
		private void clear()
		{
			_h= -1;
            _hdr= new String[100];
            _val= new String[100];
		}

		public void startResponse(Buffer version, int status, Buffer reason)
				throws IOException
		{
			clear();
            
			_t0 = version.toString();
			_t1 = Integer.toString(status);
			_t2 = new String(reason.array(), reason.getIndex(), reason.length(), StringUtil.__UTF8);
		}

		public void startRequest(Buffer method, Buffer uri, Buffer version)
				throws IOException
		{
			clear();
            
			_t0 = method.toString();
			_t1 = new String(uri.array(), uri.getIndex(), uri.length(), StringUtil.__UTF8);
			_t2 = version.toString();
		}
	}
	
	String _msg = 
        "REGISTER sip:127.0.0.1:5070 SIP/2.0\r\n"
        + "Call-ID: c117fdfda2ffd6f4a859a2d504aedb25@127.0.0.1\r\n"
        + "CSeq: 2 REGISTER\r\n"
        + "From: <sip:cipango@cipango.org>;tag=9Aaz+gQAAA\r\n"
        + "To: <sip:cipango@cipango.org>\r\n"
        + "Via: SIP/2.0/UDP 127.0.0.1:6010\r\n"
        + "Max-Forwards: 70\r\n"
        + "User-Agent: Test Script\r\n"
        + "Contact: \"Cipango\" <sip:127.0.0.1:6010;transport=udp>\r\n"
        + "Allow: INVITE, ACK, BYE, CANCEL, PRACK, REFER, MESSAGE, SUBSCRIBE\r\n"
        + "MyHeader: toto\r\n"
        + "Content-Length: 0\r\n\r\n";
}
