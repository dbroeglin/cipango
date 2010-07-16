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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.cipango.diameter.base.Common;
import org.cipango.diameter.bio.DiameterSocketConnector;
import org.cipango.diameter.log.BasicMessageLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.LazyList;
import org.eclipse.jetty.util.Loader;
import org.eclipse.jetty.util.MultiException;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.log.Log;


/**
 * A Diameter node is a host process that implements the Diameter protocol, 
 * and acts either as a Client, Agent or Server.
 * Can be used standalone or linked to a {@link Server}.
 */
public class Node extends AbstractLifeCycle implements DiameterHandler
{
	public static String[] __dictionaryClasses = 
	{
		"org.cipango.diameter.base.Common", 
		"org.cipango.diameter.base.Accounting",
		"org.cipango.diameter.ims.IMS", 
		"org.cipango.diameter.ims.Cx", 
		"org.cipango.diameter.ims.Sh",
		"org.cipango.diameter.ims.Zh"
	};
	
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
	
	private Peer[] _peers;
		
	private DiameterHandler _handler;
	private SessionManager _sessionManager;
	
	private ScheduledExecutorService _scheduler;

	private Set<ApplicationId> _supportedApplications = new HashSet<ApplicationId>();
		
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
		if (_server != null)
			_server.getContainer().update(this, _connectors, connectors, "connectors");
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
	
	public synchronized void addPeer(Peer peer)
	{
		peer.setNode(this);
		
		Peer[] peers = (Peer[]) LazyList.addToArray(_peers, peer, Peer.class);
		
		if (_server != null)
			_server.getContainer().update(this, _peers, peers, "peers");
		
		_peers = peers;
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
		
		_sessionManager = new SessionManager();
		_sessionManager.setNode(this);
		
		if (_connectors != null)
		{
			for (int i = 0; i < _connectors.length; i++)
			{
				_connectors[i].start();
			}
		}
		
		_scheduler = new ScheduledThreadPoolExecutor(1);
		
		synchronized (this)
		{
			if (_peers != null)
			{
				for (Peer peer : _peers)
				{
					try 
					{
						peer.start(); 
					}
					catch (Exception e) 
					{ 
						Log.warn("failed to start peer: " + peer, e);
					}
				}
			}
		}
		
		_scheduler.scheduleAtFixedRate(new WatchdogTimeout(), 5000, 5000, TimeUnit.MILLISECONDS);
		Log.info("Started {}", this);
	}
	
	@Override
	protected void doStop() throws Exception 
	{	
		MultiException mex = new MultiException();

		for (int i = 0; i < _connectors.length; i++)
		{
			if (_connectors[i] instanceof LifeCycle) 	
			{
				try
				{
					((LifeCycle) _connectors[i]).stop();
				}
				catch (Exception e)
				{
					mex.add(e);
				}
			}
		}
		mex.ifExceptionThrow();
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

	public void setProductName(String productName)
	{
		_productName = productName;
	}
	
	public SessionManager getSessionManager()
	{
		return _sessionManager;
	}
	
	public DiameterConnection getConnection(Peer peer) throws IOException
	{
		return _connectors[0].getConnection(peer);
	}
	
	public Peer getPeer(String host)
	{
		synchronized (this)
		{
			if (_peers != null)
			{
				for (Peer peer : _peers)
				{
					if (peer.getHost().equals(host))
						return peer;
				}
			}
		}
		return null;
	}
	
	public void send(DiameterRequest request) throws IOException
	{
		String host = request.getDestinationHost();
		Peer peer = null;
		
		if (host != null)
			peer = getPeer(host);
		
		if (peer == null)
			throw new IOException("No peer for destination host " + request.getDestinationHost());
		else
			peer.send(request);
	}
	
	public void receive(DiameterMessage message) throws IOException
	{
		Peer peer = message.getConnection().getPeer();
		
		if (peer == null)
		{
			if (message.getCommand() != Common.CER)
			{
				Log.debug("non CER as first message: " + message.getCommand());
				message.getConnection().stop();
				return;
			}
			if (message.getCommand() == Common.CER)
			{
				String originHost = message.getOriginHost();
				
				if (originHost == null)
				{
					Log.debug("No Origin-Host in CER");
					message.getConnection().stop();
					return;
				}
				String realm = message.getOriginRealm();
				if (realm == null)
				{
					Log.debug("No Origin-Realm in CER");
					message.getConnection().stop();
					return;
				}
				
				peer = getPeer(originHost);
				
				if (peer == null)
				{
					Log.warn("Unknown peer " + originHost);
					peer = new Peer(originHost);
					peer.setNode(this);
					addPeer(peer);
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
		if (message instanceof DiameterAnswer)
		{
			DiameterAnswer answer = (DiameterAnswer) message;
			if (Common.DIAMETER_REDIRECT_INDICATION.equals(answer.getResultCode()))
			{
				try 
                {
                    String redirectHost = answer.get(Common.REDIRECT_HOST);
                    Peer peer = getPeer(redirectHost);
                    if (peer != null) 
                    {
                        Log.debug("Redirecting request to: " + peer);
                        peer.send(answer.getRequest());
                    }
                    else
                    	Log.warn("Unknown peer {} indicating in redirect-host AVP", redirectHost);
                    return;
                } 
                catch (Exception e)
                {
                    Log.warn("Failed to redirect request", e);
                    return;
                }
			}
		}
		if (_handler != null)
			_handler.handle(message);
		//System.out.println("Got message: " + message.getAVPs());
	}
	
	public void addSupportedApplication(ApplicationId id)
	{
		_supportedApplications.add(id);
	}
	
	public void addCapabilities(DiameterMessage message)
	{	
		for (DiameterConnector connector : _connectors)
		{
			message.add(Common.HOST_IP_ADDRESS, connector.getLocalAddress());
		}
		
		message.add(Common.VENDOR_ID, getVendorId());
		message.add(Common.PRODUCT_NAME, getProductName());
				
		for (ApplicationId id : _supportedApplications)
		{
			if (id.isVendorSpecific())
			{
				for (Integer i : id.getVendors())
				{
					message.add(Common.SUPPORTED_VENDOR_ID, i);
				}
			}
		}
		
		for (ApplicationId id : _supportedApplications)
		{
			message.getAVPs().add(id.getAVP());
		}
		
		message.add(Common.FIRMWARE_REVISION, 1);
	}
	
	public String toString()
	{
		return _identity + "(" + _supportedApplications + ")";
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
	
	public ScheduledFuture<?> schedule(Runnable runnable, long ms)
	{
		return _scheduler.schedule(runnable, ms, TimeUnit.MILLISECONDS);
	}
	
	public void scheduleReconnect(Peer peer)
	{
		schedule(new ConnectPeerTimeout(peer), _tc);
	}
	
	class ConnectPeerTimeout implements Runnable
	{
		private Peer _peer;
		
		public ConnectPeerTimeout(Peer peer)
		{
			_peer = peer;
		}
		
		public void run()
		{
			try
			{
				if (isStarted())
				{
					if (!_peer.isStopped())
					{
						Log.debug("restarting peer: " + _peer);
						_peer.start();
					}
				}
			}
			catch (Exception e)
			{
				Log.warn("failed to reconnect to peer {} : {}", _peer, e);
			}
		}
	}
	
	class WatchdogTimeout implements Runnable
	{
		public void run()
		{
			for (Peer peer: _peers)
			{
				peer.watchdog();
			}
		}
	}
}
