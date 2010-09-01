package org.cipango.diameter;

import java.net.InetAddress;

import junit.framework.TestCase;

public class NodeTest extends TestCase
{
	public void testConnect() throws Exception
	{
		//Log.getLog().setDebugEnabled(true);
		
		Node client = new Node(3868);
		client.setIdentity("client");
		
		Peer peer = new Peer("server");
		peer.setAddress(InetAddress.getByName("127.0.0.1"));
		peer.setPort(38680);
		client.addPeer(peer);
		
		Node server = new Node(38680);
		server.getConnectors()[0].setHost("127.0.0.1");
		server.setIdentity("server");
		server.start();
		
		client.start();
		
		Thread.sleep(1000);
		assertTrue(peer.isOpen());
		
		peer.stop();
		Thread.sleep(1000);
		assertTrue(peer.isClosed());
		
		server.stop();
		client.stop();
	}
	
	/*
	public void testCnx() throws Exception
	{
		Node server = new Node(3869);
		server.setIdentity("test");
		
		Peer peer = new Peer("cipango");
		peer.setAddress(InetAddress.getLocalHost());
		peer.setPort(3868);
		
		server.addPeer(peer);	
		
		server.start();
		//client.start();
		
		Thread.sleep(10000);
		
		DiameterRequest request = new DiameterRequest(server, IMS.MAR, IMS.CX_APPLICATION_ID, "toto");
		request.getAVPs().addString(Base.DESTINATION_HOST, "cipango");
		request.send();
		
		peer.getConnection().close();
		
		Thread.sleep(5000);
	}
	*/
}
