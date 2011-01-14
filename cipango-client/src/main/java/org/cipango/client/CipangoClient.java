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

import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.cipango.server.Server;
import org.cipango.server.SipConnectors;
import org.cipango.server.bio.TcpConnector;
import org.cipango.server.bio.UdpConnector;
import org.cipango.server.handler.SipContextHandlerCollection;
import org.cipango.server.log.FileMessageLog;
import org.cipango.servlet.SipServletHolder;
import org.cipango.sipapp.SipAppContext;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

public class CipangoClient extends AbstractLifeCycle
{

	private Server _server;
	private SipAppContext _context;
	
	private SipURI _proxy;
	
	private Session _uasSession;
	
	public CipangoClient(int port) throws Exception
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
		_context.setName(CipangoClient.class.getName());
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
	
	public void setProxy(String uri) throws ServletParseException
	{
		URI proxyUri = getSipFactory().createURI(uri);
		if (!proxyUri.isSipURI())
			throw new ServletParseException("Proxy URI: " + uri + " is not a SIP URI");
		_proxy = (SipURI) proxyUri;
		_proxy.setLrParam(true);
	}
	
	public void setProxy(SipURI uri) throws ServletParseException
	{
		_proxy = uri;
		_proxy.setLrParam(true);
	}
	
	public SipURI getProxy()
	{
		return _proxy;
	}
	
	public SipRequest createRequest(String method, String from, String to) throws ServletParseException
	{
		SipApplicationSession appSession = getSipFactory().createApplicationSession();
		SipServletRequest request = getSipFactory().createRequest(appSession, method, from, to);
		if (_proxy != null)
			request.pushRoute(_proxy);
		return new SipRequestImpl(request);
	}
	
	public SipSession createUasSession()
	{
		if (_uasSession != null)
			throw new IllegalStateException("A UAS session is already created");
		
		_uasSession = new Session(null);
		return _uasSession;
	}
	
	public Address getContact()
	{
		return _server.getConnectorManager().getContact(SipConnectors.TCP_ORDINAL);
	}
	
	protected Session getUasSession()
	{
		return _uasSession;
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
}
