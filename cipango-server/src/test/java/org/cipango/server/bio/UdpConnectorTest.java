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

package org.cipango.server.bio;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipURI;

import junit.framework.TestCase;

import org.cipango.server.SipHandler;
import org.cipango.server.bio.UdpConnector;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class UdpConnectorTest extends TestCase
{
	UdpConnector _connector;
	SipServletMessage _message;
	
	protected void setUp() throws Exception
	{
		_connector = new UdpConnector();
		_connector.setPort(5040);
		_connector.setThreadPool(new QueuedThreadPool());
		_connector.setHandler(new TestHandler());
		_connector.start();
		_message = null;
	}
	
	protected void tearDown() throws Exception
	{
		Thread.sleep(40);
		_connector.stop();
		Thread.sleep(10);
	}
	
	public void testLifeCycle() throws Exception
	{
		UdpConnector connector = new UdpConnector();
		connector.setHost("localhost");
		connector.setPort(5070);
		connector.setThreadPool(new QueuedThreadPool());
		for (int i = 0; i < 10; i++)
		{
			connector.start();
			assertTrue(connector.isRunning());
			connector.stop();
			assertFalse(connector.isRunning());
			Thread.sleep(10);
		}
	}
	
	public void testPing() throws Exception
	{
		for (int i = 0; i < 100; i++)
		{
			send(_pingEol);
			send(_pingEolEol);
		}
	}
	
	public void testMessage() throws Exception
	{
		send(_msg);
		
		SipServletMessage message = getMessage(1000);
		send(_msg2);
		Thread.sleep(300);
		assertNotNull(message);
		assertEquals("REGISTER", message.getMethod());
		assertEquals("c117fdfda2ffd6f4a859a2d504aedb25@127.0.0.1", message.getCallId());
	}
	
	private SipServletMessage getMessage(long timeout) throws InterruptedException
	{
		if (_message != null)
			return _message;
		long absTimeout = System.currentTimeMillis() + timeout;
		while (absTimeout - System.currentTimeMillis() > 0)
		{
			Thread.sleep(50);
			if (_message != null)
				return _message;
		}
		return null;
	}

	public void testRoute() throws Exception
	{
		
		send(_test);
		
		SipServletMessage message = getMessage(1000);
		send(_msg2);
		send(_msg2);
		send(_msg2);
		send(_msg2);
		
		Thread.sleep(100);
		assertNotNull(_message);
		
		Iterator<Address> it = message.getAddressHeaders("route");
		assertEquals("proxy-gen2xx", ((SipURI) it.next().getURI()).getUser());
		assertTrue(it.hasNext());
		
		assertEquals("com.bea.sipservlet.tck.apps.spectestapp.uas", message.getHeader("application-name"));
	}

	private void send(String message) throws Exception
	{
		DatagramSocket ds = new DatagramSocket();
		
		byte[] b = message.getBytes("UTF-8");
		DatagramPacket packet = new DatagramPacket(b, 0, b.length, InetAddress.getLocalHost(), 5040);
	
		ds.send(packet);
	}
	
	class TestHandler implements SipHandler
	{
		
		public void handle(SipServletMessage message) throws IOException, ServletException
		{
			_message = message;
		}

		public Server getServer() {
			// TODO Auto-generated method stub
			return null;
		}

		public void setServer(Server server) {
			// TODO Auto-generated method stub
			
		}	
	}
	
	String _pingEolEol = "\r\n\r\n";
	String _pingEol = "\r\n";
	
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
	
	String _msg2 = 
        "REGISTER sip:127.0.0.1:5070 SIP/2.0\r\n"
        + "Call-ID: foo@bar\r\n"
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
	
	String _test = 
		"MESSAGE sip:proxy-gen2xx@127.0.0.1:5060 SIP/2.0\r\n"
		+ "Call-ID: 13a769769217a57d911314c67df8c729@192.168.1.205\r\n"
		+ "CSeq: 1 MESSAGE\r\n"
		+ "From: \"Alice\" <sip:alice@192.168.1.205:5071>;tag=1727584951\r\n"
		+ "To: \"JSR289_TCK\" <sip:JSR289_TCK@127.0.0.1:5060>\r\n"
		+ "Via: SIP/2.0/UDP 192.168.1.205:5071;branch=z9hG4bKaf9d7cee5d176c7edf2fbf9b1e33fc3a\r\n"
		+ "Max-Forwards: 5\r\n"
		+ "Route: \"JSR289_TCK\" <sip:proxy-gen2xx@127.0.0.1:5060;lr>,<sip:127.0.0.1:5060;transport=udp;lr>\r\n"
		+ "Application-Name: com.bea.sipservlet.tck.apps.spectestapp.uas\r\n"
		+ "Servlet-Name: Addressing\r\n"
		+ "Content-Type: text/plain\r\n"
		+ "Content-Length: 0\r\n\r\n";
}
