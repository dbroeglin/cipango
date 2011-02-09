// ========================================================================
// Copyright 2011 NEXCOM Systems
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

package org.cipango.client;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.sip.Address;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import javax.servlet.sip.SipServletRequest;

import org.cipango.client.test.SipTester;
import org.cipango.server.Server;
import org.cipango.server.bio.UdpConnector;
import org.cipango.server.handler.SipContextHandlerCollection;
import org.cipango.servlet.SipServletHolder;
import org.cipango.sip.NameAddr;
import org.cipango.sipapp.SipAppContext;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

public class SipClient extends AbstractLifeCycle
{
	private Server _server;
	private SipAppContext _context;
	
	private List<UserAgent> _userAgents = new ArrayList<UserAgent>();
	
	public SipClient(int port)
	{
		_server = new Server();
		
		UdpConnector connector = new UdpConnector();
		connector.setPort(port);
		
		_server.getConnectorManager().addConnector(connector);
		_server.setApplicationRouter(new SipClientApplicationRouter());
		
		SipContextHandlerCollection handler = new SipContextHandlerCollection();
		_server.setHandler(handler);
		
		_context = new SipAppContext();
		_context.setConfigurationClasses(new String[0]);
		_context.setContextPath("/");
		_context.setName(SipClient.class.getName());
		
		SipServletHolder holder = new SipServletHolder();
		holder.setServlet(new ClientServlet());
		holder.setName(ClientServlet.class.getName());
		
		_context.getSipServletHandler().addSipServlet(holder);
		_context.getSipServletHandler().setMainServletName(ClientServlet.class.getName());
		
		handler.addHandler(_context);
	}
	
	@Override
	protected void doStart() throws Exception
	{
		_server.start();
	}
	
	@Override
	protected void doStop() throws Exception
	{
		_server.stop();
	}
	
	public SipURI getContact()
	{
		return _server.getConnectorManager().getDefaultConnector().getSipUri();
	}
	
	public UserAgent getUserAgent(URI uri)
	{
		synchronized (_userAgents)
		{
			for (UserAgent agent : _userAgents)
			{
				if (agent.getLocalAddress().getURI().equals(uri))
					return agent;
			}
		}
		return null;
	}
	
	protected void addAgent(UserAgent agent)
	{
		SipURI contact = (SipURI) getContact().clone();
		
		agent.setFactory(_context.getSipFactory());
		agent.setContact(new NameAddr(contact));
		
		synchronized(_userAgents)
		{
			_userAgents.add(agent);
		}
	}
	
	public UserAgent createUserAgent(SipURI uri)
	{
		UserAgent agent = new UserAgent(uri);
		addAgent(agent);
		return agent;
	}
	
	
	
	public UserAgent createUserAgent(String user, String host)
	{
		return createUserAgent(createSipURI(user, host));
	}
	
	
	
	protected SipURI createSipURI(String user, String host)
	{
		return _context.getSipFactory().createSipURI(user, host);
	}
	
	@SuppressWarnings("serial")
	class ClientServlet extends SipServlet
	{
		@Override
		protected void doRequest(SipServletRequest request)
		{
			Address local = request.getTo();
			UserAgent agent = getUserAgent(local.getURI());
			
			if (agent != null)
				agent.handleRequest(request);
		}
		
		@Override
		protected void doResponse(SipServletResponse response)
		{
			Address local = response.getFrom();
			UserAgent agent = getUserAgent(local.getURI());
			
			System.out.println("response " + response);
			if (agent != null)
				agent.handleResponse(response);
		}
	}
}
