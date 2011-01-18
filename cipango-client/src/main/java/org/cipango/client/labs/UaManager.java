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
package org.cipango.client.labs;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.sip.Address;
import javax.servlet.sip.SipFactory;

import org.cipango.server.Server;
import org.cipango.server.SipConnectors;
import org.cipango.server.bio.TcpConnector;
import org.cipango.server.bio.UdpConnector;
import org.cipango.server.handler.SipContextHandlerCollection;
import org.cipango.server.log.FileMessageLog;
import org.cipango.servlet.SipServletHolder;
import org.cipango.sipapp.SipAppContext;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

public class UaManager extends AbstractLifeCycle
{
	private static int __requestTimeout = 5000;
	private static int __responseTimeout = 5000;
	
	private Server _server;
	private SipAppContext _context;
	
	private List<UA> _userAgents = new ArrayList<UA>();
			
	public UaManager(int port) throws Exception
	{
		_server = new Server();
		UdpConnector udpConnector = new UdpConnector();
		udpConnector.setPort(port);
		TcpConnector tcpConnector = new TcpConnector();
		tcpConnector.setPort(port);
		_server.getConnectorManager().addConnector(udpConnector);
		_server.getConnectorManager().addConnector(tcpConnector);
		FileMessageLog logger = new FileMessageLog();
		//logger.setFilename("yyyy_mm_dd.message.log");
		_server.getConnectorManager().setAccessLog(logger);
		
		_server.setApplicationRouter(new ApplicationRouter());
		
		SipContextHandlerCollection handlerCollection = new SipContextHandlerCollection();
		_server.setHandler(handlerCollection);
		
		_context = new SipAppContext();
		_context.setConfigurationClasses(new String[0]);
		_context.setContextPath("/");
		_context.setName(UaManager.class.getName());
		SipServletHolder holder = new SipServletHolder();
		holder.setServlet(new MainServlet(this));
		holder.setName(MainServlet.class.getName());
		_context.getSipServletHandler().addSipServlet(holder);
		_context.getSipServletHandler().setMainServletName(MainServlet.class.getName());
		
		handlerCollection.addHandler(_context);
	}
	
	public SipFactory getSipFactory()
	{
		return _context.getSipFactory();
	}
	
	public Address getContact()
	{
		return _server.getConnectorManager().getContact(SipConnectors.TCP_ORDINAL);
	}
	
	public void addUserAgent(UA ua)
	{
		_userAgents.add(ua);
	}
	
	public Session findUasSession(SipRequest request)
	{
		for (UA ua : _userAgents)
		{
			if (ua.getAor().getURI().equals(request.getTo().getURI()))
				return ua.getUasSession();
		}
		return null;
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

	public static int getRequestTimeout()
	{
		return __requestTimeout;
	}

	public static void setRequestTimeout(int requestTimeout)
	{
		__requestTimeout = requestTimeout;
	}

	public static int getResponseTimeout()
	{
		return __responseTimeout;
	}

	public static void setResponseTimeout(int responseTimeout)
	{
		__responseTimeout = responseTimeout;
	}	
}
