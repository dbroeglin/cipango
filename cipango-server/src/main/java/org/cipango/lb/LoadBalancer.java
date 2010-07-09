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

package org.cipango.lb;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.servlet.sip.SipServletMessage;

import org.cipango.server.SipHandler;
import org.cipango.server.bio.UdpConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class LoadBalancer extends AbstractLifeCycle implements SipHandler
{
	private UdpConnector connector;
	
	protected void doStart() throws Exception 
	{
		connector = new UdpConnector();
		connector.setThreadPool(new QueuedThreadPool());
		//connector.setHandler(this);
		connector.start();
	}
	
	public void handle(SipServletMessage message)
	{
		System.out.println("Got message: " + message);
	}
	
	public static void main(String[] args) throws Exception 
	{
		LoadBalancer server = new LoadBalancer();
		server.start();
		byte[] b = __msg.getBytes();
		DatagramPacket packet = new DatagramPacket(b, b.length);
		packet.setAddress(InetAddress.getLocalHost());
		packet.setPort(5060);
		DatagramSocket socket = new DatagramSocket();
		socket.send(packet);
	}
	
	static String __msg = 
        "REGISTER sip:oahu:5070 SIP/2.0\r\n"
        + "Call-ID: c117fdfda2ffd6f4a859a2d504aedb25@127.0.0.1\r\n"
        + "CSeq: 2 REGISTER\r\n"
        + "From: <sip:alice@cipango.org>;tag=9Aaz+gQAAA\r\n"
        + "To: <sip:alice@cipango.org>\r\n"
        + "Via: SIP/2.0/UDP 127.0.0.1:6010\r\n"
        + "Max-Forwards: 70\r\n"
        + "User-Agent: Test Script\r\n"
        + "Contact: \"Alice\" <sip:127.0.0.1:6010;transport=udp>\r\n"
        + "Allow: INVITE, ACK, BYE, CANCEL, PRACK, REFER, MESSAGE, SUBSCRIBE\r\n"
        + "MyHeader: toto\r\n"
        + "Content-Length: 0\r\n\r\n";

	public Server getServer() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setServer(Server server) {
		// TODO Auto-generated method stub
		
	}
}
