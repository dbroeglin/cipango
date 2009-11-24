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

package org.cipango.diameter;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.cipango.diameter.base.Base;
import org.cipango.diameter.bio.DiameterSocketConnector;
import org.cipango.diameter.ims.Cx;
import org.cipango.diameter.ims.IMS;
import org.cipango.diameter.log.BasicMessageLog;
import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.component.LifeCycle;
import org.mortbay.jetty.Server;
import org.mortbay.log.Log;
import org.mortbay.util.LazyList;
import org.mortbay.util.Loader;
import org.mortbay.util.MultiException;


/**
 * A Diameter node is a host process that implements the Diameter protocol, 
 * and acts either as a Client, Agent or Server.
 * Can be used standalone or linked to a {@link Server}.
 */
public class Node extends AbstractLifeCycle implements DiameterHandler
{
	public static String[] __dictionaryClasses = {
		"org.cipango.diameter.base.Base", "org.cipango.diameter.ims.IMS", "org.cipango.diameter.ims.Cx", "org.cipango.diameter.ims.Sh"};
	
	public static final String DEFAULT_REALM = "cipango.org";
	public static final String DEFAULT_PRODUCT_NAME = "cipango";
	public static final int NEXCOM_OID = 26588;
	
	public static final long DEFAULT_TW = 30000;
	public static final long DEFAULT_TC = 30000;
	
	private Server _server;
	
	private String _realm = DEFAULT_REALM;
	private String _identity;
	private int _vendorId = NEXCOM_OID;
	private String _productName = DEFAULT_PRODUCT_NAME;
	
	private long _tw = DEFAULT_TW;
	private long _tc = DEFAULT_TC;
	
	private DiameterConnector[] _connectors;
	private Map<String, Peer> _peers = new HashMap<String, Peer>();
	
	private DiameterHandler _handler;
	private SessionManager _sessionManager;
	
	private Timer _timer = new Timer("Diameter-watchdog");
	private Random _random = new Random();
		
	public Node()
	{
	}
	
	public Node(int port) throws IOException
	{
		DiameterSocketConnector connector = new DiameterSocketConnector();
		connector.setHost(InetAddress.getLocalHost().getHostAddress());
		connector.setMessageListener(new BasicMessageLog());
		connector.setPort(port);
		setConnectors(new DiameterConnector[] {connector});
	}
	
	public void setServer(Server server)
	{
		_server = server;
	}
	
	public Server getServer()
	{
		return _server;
	}
	
	public void setConnectors(DiameterConnector[] connectors)
	{
		if (connectors != null)
		{
			for (int i = 0; i < connectors.length; i++)
				connectors[i].setNode(this);
		}
		_connectors = connectors;
	}
	
	public DiameterConnector[] getConnectors()
    {
        return _connectors;
    }
	
	public void addConnector(DiameterConnector connector)
    {
        setConnectors((DiameterConnector[])LazyList.addToArray(getConnectors(), connector, DiameterConnector.class));
    }
	
	@Override
	protected void doStart() throws Exception 
	{
		for (int i = 0; i < __dictionaryClasses.length; i++)
		{
			Dictionary.getInstance().load(Loader.loadClass(getClass(), __dictionaryClasses[i]));
		}
			
		if (_identity == null) 
			_identity = InetAddress.getLocalHost().getHostName();
		
		/*
		if (_server != null)
		{
			System.out.println("Waiting for server to be started");
			_server.addLifeCycleListener(new BaseLifeCycleListener()
			{
				public void lifeCycleStarted(LifeCycle event)
				{
					System.out.println("lifecycle " + event + " started");
				}
			});
		}
		*/
		_sessionManager = new SessionManager();
		_sessionManager.setNode(this);
		
		MultiException mex = new MultiException();
		
		if (_connectors != null)
		{
			for (int i = 0; i < _connectors.length; i++)
			{
				try { _connectors[i].start(); }
				catch (Throwable t)
				{
					mex.add(t);
				}
			}
		}
		mex.ifExceptionThrow();
		
		for (Peer peer : _peers.values())
		{
			peer.start();
		}
		_timer.schedule(new WatchdogTask(), _tw, 1000);
		
		Log.info("Started {}", this);
	}
	
	@Override
	protected void doStop() throws Exception 
	{
		Iterator<Peer> peers = _peers.values().iterator();
		while (peers.hasNext()) 
		{
			Peer peer = peers.next();
			Log.debug("Stopping peer: " + peer);
			peer.stop();
		} 
		
		// Wait at most 1 seconds for DPA reception
		try {
			boolean allClosed = false;
			int iter = 20;
			while (iter-- > 0 && allClosed)
			{
				allClosed = true;
				peers = _peers.values().iterator();
				while (peers.hasNext()) 
				{
					Peer peer = peers.next();
					if (!peer.isClosed())
					{
						allClosed = false;
						Log.info("Wait 50ms for " + peer + " closing");
						Thread.sleep(50);
						break;
					}
				} 
			}
		} catch (Exception e) {
			Log.ignore(e);
		}
		for (int i = 0; i < _connectors.length; i++)
		{
			if (_connectors[i] instanceof LifeCycle) 		
				((LifeCycle) _connectors[i]).stop();
		}
	}
	
	public void setIdentity(String identity)
	{
		_identity = identity;
	}
	
	public String getIdentity()
	{
		return _identity;
	}
	
	public void setRealm(String realm)
	{
		_realm = realm;
	}
	
	public String getRealm()
	{
		return _realm;
	}
	
	public int getVendorId()
	{
		return _vendorId;
	}
	
	public String getProductName()
	{
		return _productName;
	}
	
	public SessionManager getSessionManager()
	{
		return _sessionManager;
	}
	
	public DiameterConnection getConnection(Peer peer) throws IOException
	{
		return _connectors[0].getConnection(peer);
	}
	
	public void addPeer(Peer peer)
	{
		synchronized (_peers)
		{
			_peers.put(peer.getHost(), peer);
			peer.setNode(this);
		}
	}
	
	public Peer getPeer(String host)
	{
		synchronized (_peers)
		{
			return _peers.get(host);
		}
	}
	
	public void send(DiameterRequest request) throws IOException
	{
		String destinationHost = request.getDestinationHost();
		if (destinationHost != null)
		{
			Peer peer = getPeer(destinationHost);
			if (peer != null)
				peer.send(request);
			else 
				throw new IOException("No peer for destination " + destinationHost); // TODO
		}
		else
		{
			throw new IOException("No destination-host"); // TODO
		}
	}
	
	public void setPeers(Peer[] peers)
	{
		if (isRunning())
			throw new IllegalArgumentException("Could not set peers while running");
		for (Peer peer: peers)
			addPeer(peer);
	}
	
	public void receive(DiameterMessage message) throws IOException
	{
		Peer peer = message.getConnection().getPeer();
		
		if (peer == null)
		{
			if (message.getCommand() != Base.CER)
			{
				Log.debug("non CER as first message: " + message.getCommand());
				message.getConnection().stop();
				return;
			}
			if (message.getCommand() == Base.CER)
			{
				String originHost = message.getOriginHost();
				
				if (originHost == null)
				{
					Log.debug("No Origin-Host in CER");
					message.getConnection().stop();
					return;
				}
				
				peer = _peers.get(originHost);
				
				if (peer == null)
				{
					Log.warn("Unknown peer " + originHost);
					peer = new Peer(originHost);
					peer.setNode(this);
					_peers.put(originHost, peer);
				}
				message.getConnection().setPeer(peer);
				peer.rConnCER((DiameterRequest) message);
			}
		}
		else 
		{
			peer.receive(message);
		}
	}
	
	public void setHandler(DiameterHandler handler)
	{
		_handler = handler;
	}
	
	public void handle(DiameterMessage message) throws IOException
	{
		if (!message.isRequest())
		{
			int code;
			int vendorId = Base.IETF_VENDOR_ID;
			
			AVP<Integer> avp = message.getAVPs().get(Base.RESULT_CODE);
			if (avp != null)
			{
				code = avp.getValue();
			}
			else
			{
				AVPList expRc = message.get(Base.EXPERIMENTAL_RESULT);
				code = expRc.getValue(Base.EXPERIMENTAL_RESULT_CODE);
				vendorId = expRc.getValue(Base.VENDOR_ID);
			}
			
			ResultCode rc = Dictionary.getInstance().getResultCode(vendorId, code);
			if (rc == null)
				rc = Factory.newResultCode(vendorId, code, "Unknown");
			
			((DiameterAnswer) message).setResultCode(rc);
		}
		/*
		System.out.println("Got message: " + message);
		String sessionId = message.getSessionId();
		
		boolean initial = false;
		DiameterSession session = null;
		
		if (sessionId != null)
		{
			session = _sessionManager.getSession(sessionId);
			if (session == null)
			{
				initial = true;
				session = _sessionManager.newSession();
				
				ApplicationId id = ApplicationId.ofAVP(message);
				String destinationRealm = message.getAVPs().getString(Base.DESTINATION_REALM);
				
				session.setApplicationId(id);
				session.setDestinationRealm(destinationRealm);
			}
			
			message.setSession(session);
		}
		
		System.out.println(message.getSession());
		*/
		if (_handler != null)
			_handler.handle(message);
		//System.out.println("Got message: " + message.getAVPs());
	}
	
	public void addCapabilities(DiameterMessage message)
	{	
		for (DiameterConnector connector : _connectors)
		{
			message.add(Base.HOST_IP_ADDRESS, connector.getLocalAddress());
			System.out.println(connector.getLocalAddress());
		}
		
		message.add(Base.VENDOR_ID, getVendorId());
		message.add(Base.PRODUCT_NAME, getProductName());
		
		AVPList vsai = new AVPList();
		vsai.add(Base.VENDOR_ID, IMS.IMS_VENDOR_ID);
		vsai.add(Base.AUTH_APPLICATION_ID, Cx.CX_APPLICATION);
		
		message.add(Base.VENDOR_SPECIFIC_APPLICATION_ID, vsai);
	}
	
	public String toString()
	{
		return _identity;
	}
	
	public long getTw()
	{
		return _tw;
	}

	public void setTw(long tw)
	{
		if (tw < 6000)
			throw new IllegalArgumentException("Tw MUST NOT be set lower than 6 seconds");
		_tw = tw;
	}

	public long getTc()
	{
		return _tc;
	}

	public void setTc(long tc)
	{
		_tc = tc;
	}
	
	public void scheduleReconnect(TimerTask task)
	{
		_timer.schedule(task, _tc);
	}

    class WatchdogTask extends TimerTask
    {

		public void run()
		{
			if (!isRunning())
			{
				cancel();
				return;
			}
			// Jitter is between -2 and 2 seconds
			long twJitter = _random.nextInt(4000) - 2000;
			Iterator<Peer> it = _peers.values().iterator();
			while (it.hasNext())
				it.next().sendDWRIfNeeded(_tw + twJitter);
		}
    }


}
