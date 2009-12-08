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

import java.net.DatagramSocket;

import org.cipango.media.UdpEndPoint;
import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;

import junit.framework.TestCase;

public class UdpEndPointTest extends TestCase
{
	public void testEndpoints() throws Exception
	{
		DatagramSocket socket1 = new DatagramSocket();
		DatagramSocket socket2 = new DatagramSocket();
		
		UdpEndPoint ep1 = new UdpEndPoint(socket1);
		final UdpEndPoint ep2 = new UdpEndPoint(socket2);
		
		Buffer buffer1 = new ByteArrayBuffer("hello world");
		final Buffer buffer2 = new ByteArrayBuffer(1024);

		new Thread() 
		{
			public void run() 
			{
				try { ep2.read(buffer2); } catch (Exception e) { e.printStackTrace(); }
			}
		}.start();
		ep1.send(buffer1, socket2.getLocalSocketAddress());
		Thread.sleep(100);
		
		String s = new String(buffer2.asArray());
		assertEquals("hello world", s);
	}
}
