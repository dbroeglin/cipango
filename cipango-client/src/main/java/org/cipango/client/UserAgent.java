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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;

import org.cipango.sip.NameAddr;
import org.cipango.sip.SipHeaders;
import org.cipango.sip.SipMethods;
import org.eclipse.jetty.util.log.Log;

public class UserAgent
{
	private Address _contact;
	private Address _localAddress;
	private SipFactory _factory;
	
	private MessageHandler _defaultHandler = new InitialRequestHandler();
	private UserAgentListener _eventListener;
	
	private SipSession _registerSession;
	private List<Call> _calls = new ArrayList<Call>();
	
	private String _user;
	private String _passwd;
	
	public UserAgent(SipURI aor)
	{
		_localAddress = new NameAddr(aor);
	}
	
	public void setDefaultHandler(MessageHandler handler)
	{
		_defaultHandler = handler;
	}
	
	public void setCredentials(String user, String passwd)
	{
		_user = user;
		_passwd = passwd;
	}
	
	public Address getLocalAddress()
	{
		return _localAddress;
	}
	
	public void setFactory(SipFactory factory)
	{
		_factory = factory;
	}
	
	public void setContact(Address contact)
	{
		_contact = contact;
	}
	
	public void setEventListener(UserAgentListener listener)
	{
		_eventListener = listener;
	}
	
	public void handleResponse(SipServletResponse response)
	{
		try
		{
			getHandler(response).handleResponse(response);
		}
		catch (Exception e)
		{
			Log.debug(e);
		}
	}
	
	public void handleRequest(SipServletRequest request)
	{
		try
		{
			getHandler(request).handleRequest(request);
		}
		catch (Exception e)
		{
			Log.debug(e);
		}
	}
	
	protected MessageHandler getHandler(SipServletMessage message)
	{
		SipSession session = message.getSession();
		MessageHandler handler = (MessageHandler) session.getAttribute(MessageHandler.class.getName());
		
		if (handler == null)
			handler = _defaultHandler;
		
		return handler;
	}
	
	protected void setHandler(SipServletMessage message, MessageHandler handler)
	{
		message.getSession().setAttribute(MessageHandler.class.getName(), handler);
	}
	
	public SipServletRequest createRequest(String method, Address destination)
	{
		SipApplicationSession appSession = _factory.createApplicationSession();
		SipServletRequest request = _factory.createRequest(appSession, method, _localAddress, destination);
		request.addHeader(SipHeaders.USER_AGENT, "Cipango-Client");
		return request;
	}

	public SipServletRequest createRequest(String method, String destination) throws ServletParseException
	{
		return createRequest(method, _factory.createAddress(destination));
	}

	public SipServletRequest createRequest(SipSession session, String method)
	{
		SipServletRequest request = session.createRequest(method);
		request.addHeader(SipHeaders.USER_AGENT, "Cipango-Client");
		return request;
	}
	
	protected SipServletRequest createRegister(SipSession session) 
	{
		SipServletRequest register;
		if (session == null)
			register = createRequest(SipMethods.REGISTER, _localAddress);
		else
			register = createRequest(session, SipMethods.REGISTER);
			
		SipURI registrar = _factory.createSipURI(null, ((SipURI) _localAddress.getURI()).getHost());
		register.setRequestURI(registrar);
		register.setAddressHeader(SipHeaders.CONTACT, _contact);
		register.setExpires(3600);
		
		return register;
	}
	
	public Call createCall(String destination) throws ServletParseException
	{
		return createCall(_factory.createAddress(destination));
	}
	
	public Call createCall(Address destination)
	{
		synchronized (_calls)
		{
			Call call = newCall(destination);
			_calls.add(call);
			return call;
		}
	}
	
	protected Call newCall(Address destination)
	{
		return new Call(destination);
	}
	
	public boolean isRegistered()
	{
		if (_registerSession != null)
		{ 
			Long expiryTime = (Long) _registerSession.getAttribute("expiryTime");
			if (expiryTime != null)
				return expiryTime.longValue() > System.currentTimeMillis();
		}
		return false;
	}
	
	public synchronized void startRegistration() throws IOException
	{
		if (_registerSession == null)
		{
			SipServletRequest register = createRegister(null);
			_registerSession = register.getSession();
			_registerSession.setAttribute(MessageHandler.class.getName(), new RegistrationHandler());
			register.send();
		}
	}
	
	class RegistrationHandler implements MessageHandler
	{
		public void handleRequest(SipServletRequest request) { }
		
		public void handleResponse(SipServletResponse response)
		{
			System.out.println("handling response " + response);
			if (_registerSession != null)
			{
				int status = response.getStatus();
				if (status == SipServletResponse.SC_OK)
				{
					try
					{
						int expires = response.getExpires();
					
						if (expires == -1)
						{
							Address contact = response.getAddressHeader(SipHeaders.CONTACT);
							expires = contact.getExpires();
						}
						long expiryTime = System.currentTimeMillis() + expires * 1000l;
						_registerSession.setAttribute("expiryTime", expiryTime);
						
						System.out.println("expires " + expires);
					}
					catch (Exception e)
					{
						// registration failure
					}
				}
				else if (status == SipServletResponse.SC_UNAUTHORIZED)
				{
					if (response.getRequest().getHeader(SipHeaders.AUTHORIZATION) == null)
					{
						SipServletRequest register = createRegister(_registerSession);
						register.addAuthHeader(response, _user, _passwd);
						try
						{
							register.send();
						}
						catch (Exception e)
						{
							// registration failed
						}
					}
					else 
					{
						System.out.println(status);
						// stale ?
						System.out.println("registration failed");
					}
				}
			}
		}
	}
	
	public String toString()
	{
		return _localAddress + "[" + _contact + "]";
	}
	
	class InitialRequestHandler implements MessageHandler
	{
		public void handleRequest(SipServletRequest request) 
		{
			if (request.isInitial())
			{
				if (request.getMethod().equalsIgnoreCase(SipMethods.INVITE))
				{
					Call call = new Call(request.getFrom());
					call.handleRequest(request);
				}
			}
			else
			{
				Log.warn("unexpected request " + request);
			}
		}

		public void handleResponse(SipServletResponse response) 
		{
			Log.warn("unexpected response " + response);
		}
	}
	
	public class Call implements MessageHandler
	{
		private SipSession _session;
		private Address _remoteAddress;
		
		public Call(Address destination)
		{
			_remoteAddress = destination;
		}
		
		public SipServletRequest createInvite()
		{
			if (_session != null)
				throw new IllegalStateException();
			
			SipApplicationSession appSession = _factory.createApplicationSession();
			SipServletRequest invite = _factory.createRequest(
					appSession,
					SipMethods.INVITE,
					_localAddress, 
					_remoteAddress);
			setHandler(invite, Call.this);
			return invite;
		}
		
		public void handleRequest(SipServletRequest request) 
		{
			System.out.println("got incoming call");
		}

		public void handleResponse(SipServletResponse response) 
		{
		}		
	}
}