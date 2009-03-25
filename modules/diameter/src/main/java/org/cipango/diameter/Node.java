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
import java.util.Map;

import org.cipango.diameter.base.Base;
import org.cipango.diameter.bio.DiameterSocketConnector;
import org.cipango.diameter.ims.IMS;
import org.cipango.diameter.log.BasicMessageLog;
import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.jetty.Server;
import org.mortbay.log.Log;
import org.mortbay.util.LazyList;
import org.mortbay.util.MultiException;

/**
 * A Diameter node is a host process that implements the Diameter protocol, 
 * and acts either as a Client, Agent or Server.
 * Can be used standalone or linked to a {@link Server}.
 *
 */
public class Node extends AbstractLifeCycle implements DiameterHandler
{
	//public static String[] __dictionaryClasses = {"org.cipango.diameter.base.Base"};
	
	private Server _server;
	
	private String _realm = "cipango.org";
	private String _identity;
	private int _vendorId = 26588;
	private String _productName = "cipango";
	
	private DiameterConnector[] _connectors;
	private Map<String, Peer> _peers = new HashMap<String, Peer>();
	
	private DiameterHandler _handler;
	private SessionManager _sessionManager;
	
	public Node()
	{
	}
	
	public Node(int port)
	{
		DiameterSocketConnector connector = new DiameterSocketConnector();
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
		/*for (int i = 0; i < __dictionaryClasses.length; i++)
		{
			__dictionary.load(Loader.loadClass(getClass(), __dictionaryClasses[i]));
		}
		*/
			
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
		Log.info("Started {}", this);
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
				AVP avp = message.getAVP(Base.ORIGIN_HOST);
				
				if (avp == null)
				{
					Log.debug("No Origin-Host in CER");
					message.getConnection().stop();
					return;
				}
				
				String originHost = avp.getString();
				
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
		_handler.handle(message);
	}
	
	public void addCapabilities(DiameterMessage message)
	{	
		for (int i = 0; i < _connectors.length; i++)
			message.add(AVP.ofAddress(Base.HOST_IP_ADDRESS, _connectors[i].getLocalAddress()));
		
		message.add(AVP.ofInt(Base.VENDOR_ID, getVendorId()));
		message.add(AVP.ofString(Base.PRODUCT_NAME, getProductName()));
		
		message.add(AVP.ofAVPs(Base.VENDOR_SPECIFIC_APPLICATION_ID,
				AVP.ofInt(Base.VENDOR_ID, IMS.IMS_VENDOR_ID),
				AVP.ofInt(Base.AUTH_APPLICATION_ID, IMS.CX_APPLICATION_ID)));
	}
	
	public String toString()
	{
		return _identity;
	}
}
