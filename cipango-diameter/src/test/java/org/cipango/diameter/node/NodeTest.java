package org.cipango.diameter.node;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.cipango.diameter.AVP;
import org.cipango.diameter.AVPList;
import org.cipango.diameter.api.DiameterFactory;
import org.cipango.diameter.api.DiameterServletAnswer;
import org.cipango.diameter.api.DiameterServletRequest;
import org.cipango.diameter.api.DiameterSession;
import org.cipango.diameter.base.Common;
import org.cipango.diameter.base.Common.AuthSessionState;
import org.cipango.diameter.ims.Cx;
import org.cipango.diameter.ims.Sh;
import org.cipango.diameter.ims.Sh.DataReference;

public class NodeTest extends TestCase
{
	private Node _client;
	private Node _server;
	private Peer _peer;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		_client = new Node(38681);
		_client.getConnectors()[0].setHost("127.0.0.1");
		_client.setIdentity("client");
		
		_peer = new Peer("server");
		_peer.setAddress(InetAddress.getByName("127.0.0.1"));
		_peer.setPort(38680);
		_client.addPeer(_peer);
		
		_server = new Node(38680);
		_server.getConnectors()[0].setHost("127.0.0.1");
		_server.setIdentity("server");
	}

	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
		_server.stop();
		_client.stop();
	}
	
	public void testConnect() throws Exception
	{
		//org.eclipse.jetty.util.log.Log.getLog().setDebugEnabled(true);
		
		_server.start();
		
		_client.start();
		
		waitPeerOpened();
		
		_peer.stop();
		Thread.sleep(100);
		assertTrue(_peer.isClosed());
	}
	
	public void testUdr() throws Throwable
	{
		//Log.getLog().setDebugEnabled(true);
		
		TestDiameterHandler serverHandler = new TestDiameterHandler()
		{

			@Override
			public void doHandle(DiameterMessage message) throws Throwable
			{
				DiameterServletAnswer uda;
				DiameterServletRequest request = (DiameterServletRequest) message;

				assertEquals(true, message.isRequest());
				assertEquals(Sh.UDR, request.getCommand());
				assertEquals(request.getApplicationId(), Sh.SH_APPLICATION_ID.getId());
				assertEquals(request.getDestinationHost(), "server");
				uda = request.createAnswer(Common.DIAMETER_SUCCESS);
				uda.send();
			}
			
		};
		_server.setHandler(serverHandler);
		_server.start();
		
		TestDiameterHandler clientHandler = new TestDiameterHandler()
		{
			
			@Override
			public void doHandle(DiameterMessage message) throws Throwable
			{
				DiameterServletAnswer uda = (DiameterServletAnswer) message;
	
				assertFalse(message.isRequest());
				assertEquals(Sh.UDA, uda.getCommand());
				assertEquals(uda.getApplicationId(), Sh.SH_APPLICATION_ID.getId());

			}
		};
		_client.setHandler(clientHandler);
		_client.start();
		
		waitPeerOpened();
				
		DiameterRequest udr = new DiameterRequest(_client, Sh.UDR, Sh.SH_APPLICATION_ID.getId(), _client.getSessionManager().newSessionId());
		udr.getAVPs().add(Common.DESTINATION_REALM, "server");
		udr.getAVPs().add(Common.DESTINATION_HOST, "server");
		udr.getAVPs().add(Sh.DATA_REFERENCE, DataReference.SCSCFName);
		AVP<AVPList> userIdentity = new AVP<AVPList>(Sh.USER_IDENTITY, new AVPList());
        userIdentity.getValue().add(Cx.PUBLIC_IDENTITY, "sip:alice@cipango.org");
		udr.getAVPs().add(userIdentity);
		udr.getAVPs().add(Common.AUTH_SESSION_STATE, AuthSessionState.NO_STATE_MAINTAINED);
		udr.getSession();
		udr.send();
		serverHandler.assertDone();
		clientHandler.assertDone();
	}
	
	protected DiameterFactory createFactory(Node node)
	{
		DiameterFactoryImpl factory = new DiameterFactoryImpl();
		factory.setNode(node);
		return factory;
	}
	
	public void testDiameterFactory() throws Throwable
	{
		//Log.getLog().setDebugEnabled(true);
		
		TestDiameterHandler serverHandler = new TestDiameterHandler()
		{

			@Override
			public void doHandle(DiameterMessage message) throws Throwable
			{
				DiameterServletAnswer uda;
				DiameterServletRequest request = (DiameterServletRequest) message;

				assertEquals(true, message.isRequest());
				assertEquals(Sh.UDR, request.getCommand());
				assertEquals(request.getApplicationId(), Sh.SH_APPLICATION_ID.getId());
				assertEquals(request.getDestinationHost(), "server");
				uda = request.createAnswer(Common.DIAMETER_SUCCESS);
				uda.send();
			}
			
		};
		_server.setHandler(serverHandler);
		_server.start();
		
		TestDiameterHandler clientHandler = new TestDiameterHandler()
		{
			
			@Override
			public void doHandle(DiameterMessage message) throws Throwable
			{
				DiameterServletAnswer uda = (DiameterServletAnswer) message;
	
				assertFalse(message.isRequest());
				assertEquals(Sh.UDA, uda.getCommand());
				assertEquals(uda.getApplicationId(), Sh.SH_APPLICATION_ID.getId());

			}
		};
		_client.setHandler(clientHandler);
		_client.start();
		
		waitPeerOpened();
		

		DiameterFactory clientFactory = createFactory(_client);
		DiameterServletRequest udr = clientFactory.createRequest(null, Sh.SH_APPLICATION_ID, Sh.UDR, "server");
		
		udr.add(Common.DESTINATION_HOST, "server");
		udr.getAVPs().add(Sh.DATA_REFERENCE, DataReference.SCSCFName);
		AVP<AVPList> userIdentity = new AVP<AVPList>(Sh.USER_IDENTITY, new AVPList());
        userIdentity.getValue().add(Cx.PUBLIC_IDENTITY, "sip:alice@cipango.org");
		udr.getAVPs().add(userIdentity);
		udr.getAVPs().add(Common.AUTH_SESSION_STATE, AuthSessionState.NO_STATE_MAINTAINED);
		udr.getSession();
		udr.send();
		serverHandler.assertDone();
		clientHandler.assertDone();
	}
	
	private void waitPeerOpened()
	{
		int i = 50;
		while (i != 0)
		{
			if (_peer.isOpen())
				return;
			try { Thread.sleep(20); } catch (InterruptedException e) {}
			i++;
		}
		assertTrue(_peer.isOpen());
	}
	
	public void testSession() throws Throwable
	{
		//Log.getLog().setDebugEnabled(true);
		
		TestDiameterHandler serverHandler = new TestDiameterHandler()
		{
			private String _sessionId;
			private DiameterSession _session;
			
			@Override
			public void doHandle(DiameterMessage message) throws Throwable
			{
				if (message instanceof DiameterServletAnswer)
				{
					assertEquals(Sh.PNA, message.getCommand());
					assertEquals(_sessionId, message.getSessionId());
					assertEquals(_session, message.getSession());
				}
				else
				{
					DiameterServletAnswer sna;
					DiameterServletRequest request = (DiameterServletRequest) message;
	
					assertEquals(true, message.isRequest());
					assertEquals(Sh.SNR, request.getCommand());
					assertEquals(request.getApplicationId(), Sh.SH_APPLICATION_ID.getId());
					assertEquals(request.getDestinationHost(), "server");
					sna = request.createAnswer(Common.DIAMETER_SUCCESS);
					_sessionId = request.getSessionId();
					assertNotNull(_sessionId);
					_session = request.getSession();
					assertNotNull(_session);
					sna.send();
					
					Thread.sleep(50);
					DiameterServletRequest pnr = _session.createRequest(Sh.PNR, true);
					pnr.send();
				}
			}
			
		};
		_server.setHandler(serverHandler);
		_server.start();
		
		TestDiameterHandler clientHandler = new TestDiameterHandler()
		{
			private String _sessionId;
			private DiameterSession _session;
			
			@Override
			public void doHandle(DiameterMessage message) throws Throwable
			{
				if (message instanceof DiameterServletAnswer)
				{
					DiameterServletAnswer sna = (DiameterServletAnswer) message;
					assertEquals(Sh.SNA, sna.getCommand());
					assertEquals(sna.getApplicationId(), Sh.SH_APPLICATION_ID.getId());
					_sessionId = sna.getSessionId();
					_session = sna.getSession();
					assertNotNull(_sessionId);
					assertNotNull(_session);
					assertEquals(_sessionId, sna.getRequest().getSessionId());
				}
				else
				{
					DiameterServletRequest pnr = (DiameterServletRequest) message;
					assertEquals(Sh.PNR, pnr.getCommand());
					assertEquals(_sessionId, pnr.getSessionId());
					assertEquals(_session, pnr.getSession());
					pnr.createAnswer(Common.DIAMETER_SUCCESS).send();
				}
			}
		};
		_client.setHandler(clientHandler);
		_client.start();
		
		waitPeerOpened();
		
		String id = _client.getSessionManager().newSessionId();
		DiameterRequest snr = new DiameterRequest(_client, Sh.SNR, Sh.SH_APPLICATION_ID.getId(), id);
		snr.add(Common.DESTINATION_REALM, "server");
		snr.add(Common.DESTINATION_HOST, "server");
		snr.add(Sh.DATA_REFERENCE, DataReference.SCSCFName);
		AVP<AVPList> userIdentity = new AVP<AVPList>(Sh.USER_IDENTITY, new AVPList());
        userIdentity.getValue().add(Cx.PUBLIC_IDENTITY, "sip:alice@cipango.org");
		snr.getAVPs().add(userIdentity);
		snr.add(Common.AUTH_SESSION_STATE, AuthSessionState.NO_STATE_MAINTAINED);
		snr.getAVPs().add(Sh.SH_APPLICATION_ID.getAVP());
		
		snr.send();
		
		serverHandler.assertDone(2);
		clientHandler.assertDone(2);
	}
		
	public static abstract class TestDiameterHandler implements DiameterHandler
	{
		private Throwable _e;
		private AtomicInteger _msgReceived = new AtomicInteger(0);
				
		public void handle(DiameterMessage message)
		{
			try
			{
				doHandle(message);
			}
			catch (Throwable e)
			{
				e.printStackTrace();
				_e = e;
			}
			finally
			{
				_msgReceived.incrementAndGet();
				synchronized (_msgReceived)
				{
					_msgReceived.notify();
				}
			}
		}
		
		public abstract void doHandle(DiameterMessage message) throws Throwable;
		
		
		public void assertDone() throws Throwable
		{
			assertDone(1);
		}
		
		public void assertDone(int msgExpected) throws Throwable
		{
			if (_e != null)
				throw _e;
			
			long end = System.currentTimeMillis() + 5000;
			
			synchronized (_msgReceived)
			{
				while (end > System.currentTimeMillis() && _msgReceived.get() < msgExpected)
				{
					try
					{
						_msgReceived.wait(end - System.currentTimeMillis());
					}
					catch (InterruptedException e)
					{
					}
				}
			}
			if (_e != null)
				throw _e;
			if (_msgReceived.get() != msgExpected)
				Assert.fail("Received " + _msgReceived + " messages when expected " + msgExpected);
		}
	}
	
}
