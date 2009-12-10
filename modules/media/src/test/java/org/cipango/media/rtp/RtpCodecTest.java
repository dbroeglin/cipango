// ========================================================================
// Copyright 2008-2009 NEXCOM Systems
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

package org.cipango.media.rtp;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;

import junit.framework.TestCase;


public class RtpCodecTest extends TestCase
{
	protected Buffer load(String name) throws Exception
	{
		URL url = getClass().getClassLoader().getResource(name);
		File file = new File(url.toURI());
		FileInputStream fin = new FileInputStream(file);
		byte[] b = new byte[(int) file.length()];
		fin.read(b);
		
		return new ByteArrayBuffer(b);
	}
	
	public void testParse() throws Exception
	{
		Buffer buffer = load("rtp.dat");
		
		RtpPacket packet = new RtpCodec().decode(buffer);
		assertEquals(160, packet.getData().length());
		assertEquals(260124933, packet.getSsrc());
		assertEquals(5025, packet.getSequenceNumber());
		assertEquals(229080, packet.getTimestamp());
	}
	
	public void testEncode()
	{
		long ts = Integer.MAX_VALUE + 1l;
		
		RtpPacket packet = new RtpPacket(123456789, 987654321, ts);
		packet.setData(new ByteArrayBuffer("hello world".getBytes()));
		
		RtpCodec codec = new RtpCodec();
		Buffer buffer = new ByteArrayBuffer(1024);
		codec.encode(buffer, packet);
		RtpPacket packet2 = codec.decode(buffer);
		
		assertEquals(ts, packet2.getTimestamp());
		assertEquals(123456789, packet2.getSsrc());
		
		assertEquals("hello world", new String(packet2.getData().asArray()));
	}
}
