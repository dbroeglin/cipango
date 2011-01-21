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
import java.util.List;

import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.SipURI;

import org.cipango.sip.NameAddr;
import org.cipango.sip.SipHeaders;
import org.cipango.sip.SipMethods;

public class UserAgent
{
	private NameAddr _localAddress;
	private SipFactory _factory;
	
	private List<SipServletResponse> _responses = new ArrayList<SipServletResponse>();
	
	enum RegistrationState { UNREGISTERED, REGISTERING_UNAUTH, REGISTERING_AUTH, REGISTERED };
	
	private RegistrationState _registrationState;
	
	public UserAgent(SipURI aor)
	{
		_localAddress = new NameAddr(aor);
	}
	
	public Address getLocalAddress()
	{
		return _localAddress;
	}
	
	public void setFactory(SipFactory factory)
	{
		_factory = factory;
	}
	
	public void handleResponse(SipServletResponse response)
	{
		if (response.getMethod().equalsIgnoreCase(SipMethods.REGISTER))
		{
			
		}
		else
		{
			synchronized (_responses)
			{
				_responses.add(response);
				_responses.notifyAll();
			}
		}
	}
	
	public void handleRequest(SipServletRequest request)
	{
		
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
	
	public SipServletResponse getResponse(SipServletRequest request, long timeout) throws InterruptedException
	{
		long start = System.currentTimeMillis();
		
		synchronized (_responses)
		{
			for (long remaining = timeout; remaining > 0; remaining = timeout - (System.currentTimeMillis() - start))
			{
				for (SipServletResponse response : _responses)
				{
					if (response.getRequest() == request)
						return response;
				}
				_responses.wait(remaining);	
			}
		}
		return null;
	}
	
	public void register() throws IOException
	{
		if (_registrationState == RegistrationState.UNREGISTERED)
		{
			SipURI registrar = _factory.createSipURI(null, ((SipURI) _localAddress.getURI()).getHost());
			SipServletRequest register = createRequest(SipMethods.REGISTER, new NameAddr(registrar));
			register.setExpires(3600);
			register.send();
		}
	}
	
	
}