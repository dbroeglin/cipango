package org.cipango.diameter.node;

import java.io.IOException;
import java.net.InetAddress;

import junit.framework.TestCase;

import org.cipango.diameter.AVP;
import org.cipango.diameter.AVPList;
import org.cipango.diameter.api.DiameterServletAnswer;
import org.cipango.diameter.api.DiameterServletRequest;
import org.cipango.diameter.base.Common;
import org.cipango.diameter.base.Common.AuthSessionState;
import org.cipango.diameter.ims.Cx;
import org.cipango.diameter.ims.Sh;
import org.cipango.diameter.ims.Sh.DataReference;

public class NodeTest extends TestCase
{
	public void testConnect() throws Exception
	{
		//org.eclipse.jetty.util.log.Log.getLog().setDebugEnabled(true);
		
		Node client = new Node(38681);
		client.getConnectors()[0].setHost("127.0.0.1");
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
	
	public void testUdr() throws Exception
	{
		//Log.getLog().setDebugEnabled(true);
		
		Node client = new Node(38681);
		client.getConnectors()[0].setHost("127.0.0.1");
		client.setIdentity("client");
		
		Peer peer = new Peer("server");
		peer.setAddress(InetAddress.getByName("127.0.0.1"));
		peer.setPort(38680);
		client.addPeer(peer);
		
		Node server = new Node(38680);
		server.getConnectors()[0].setHost("127.0.0.1");
		server.setIdentity("server");
		ServerDiameterHandler serverHandler = new ServerDiameterHandler();
		server.setHandler(serverHandler);
		server.start();
		
		ClientDiameterHandler clientHandler = new ClientDiameterHandler();
		client.setHandler(clientHandler);
		client.start();
		
		Thread.sleep(1000);
		assertTrue(peer.isOpen());
				
		DiameterRequest udr = new DiameterRequest(client, Sh.UDR, Sh.SH_APPLICATION_ID.getId(), "123");
		udr.getAVPs().add(Common.DESTINATION_REALM, "server");
		udr.getAVPs().add(Common.DESTINATION_HOST, "server");
		udr.getAVPs().add(Sh.DATA_REFERENCE, DataReference.SCSCFName);
		AVP<AVPList> userIdentity = new AVP<AVPList>(Sh.USER_IDENTITY, new AVPList());
        userIdentity.getValue().add(Cx.PUBLIC_IDENTITY, "sip:alice@cipango.org");
		udr.getAVPs().add(userIdentity);
		udr.getAVPs().add(Common.AUTH_SESSION_STATE, AuthSessionState.NO_STATE_MAINTAINED);
		
		udr.send();
		Thread.sleep(1000);
		assertTrue(serverHandler._handleUdr);
		assertTrue(clientHandler._handleUda);
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
	
	class ServerDiameterHandler implements DiameterHandler
	{
		
		private boolean _handleUdr = false;

		public void handle(DiameterMessage message) throws IOException
		{
			DiameterServletAnswer uda;
			DiameterServletRequest request = (DiameterServletRequest) message;
			try
			{
				assertEquals(true, message.isRequest());
				assertEquals(Sh.UDR, request.getCommand());
				assertEquals(request.getApplicationId(), Sh.SH_APPLICATION_ID.getId());
				assertEquals(request.getDestinationHost(), "server");
				uda = request.createAnswer(Common.DIAMETER_SUCCESS);
				_handleUdr = true;
			}
			catch (Throwable e) 
			{
				System.err.println("Failed to handle: " + message);
				e.printStackTrace();
				uda = request.createAnswer(Common.DIAMETER_UNABLE_TO_COMPLY);
			}
			uda.send();
			
		}
	}
	
	class ClientDiameterHandler implements DiameterHandler
	{
		private boolean _handleUda = false;

		public void handle(DiameterMessage message) throws IOException
		{
			DiameterServletAnswer uda = (DiameterServletAnswer) message;
			try
			{
				assertFalse(message.isRequest());
				assertEquals(Sh.UDA, uda.getCommand());
				assertEquals(uda.getApplicationId(), Sh.SH_APPLICATION_ID.getId());
				_handleUda = true;
			}
			catch (Throwable e) 
			{
				System.err.println("Failed to handle: " + message);
				e.printStackTrace();
			}
		}
		
	}
}
