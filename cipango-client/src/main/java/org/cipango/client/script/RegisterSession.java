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
package org.cipango.client.script;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import junit.framework.Assert;

import org.cipango.client.CipangoClient;
import org.cipango.client.SipRequest;
import org.cipango.client.SipResponse;
import org.cipango.client.SipSession;
import org.cipango.client.interceptor.AuthenticationInterceptor;
import org.cipango.sip.SipHeaders;

public class RegisterSession
{
	private SipSession _session;
	private CipangoClient _client;
	private String _from;
	private AuthenticationInterceptor _authenticationInterceptor;
	
	public RegisterSession(CipangoClient client, String from)
	{
		_client = client;
		_from = from;
	}
		
	/**
	 * <pre>
	 * SipUnit                    Remote
	 * | REGISTER                   |
	 * |--------------------------->| 
	 * |                        200 | 
	 * |<---------------------------|     
	 * </pre>
	 */
	public void register(int expires) throws ServletException, IOException
	{
		register(expires, null, null);
	}
	
	/**
	 * <pre>
	 * SipUnit                    Remote
	 * | REGISTER                   |
	 * |--------------------------->| 
	 * |                        401 | 
	 * |<---------------------------| 
	 * | REGISTER                   |
	 * |--------------------------->| 
	 * |                        200 | 
	 * |<---------------------------|     
	 * </pre>
	 */
	public void register(int expires, String username, String password) throws ServletException, IOException
	{
		SipRequest request = createRegister(expires);
		if (_authenticationInterceptor != null)
			request.getSession().addMessageInterceptor(_authenticationInterceptor);
		else if (username != null)
			request.getSession().addMessageInterceptor(new AuthenticationInterceptor(username, password));
		request.send();
		SipResponse response = request.waitResponse();
		
		if (!response.is2xx())
			Assert.fail("Registration failed\n" + response);	
		/*
		while (true)
		{
			request.send();
			SipResponse response = request.waitResponse();
			
			if (response.is2xx())
				return;
			
			if (response.getStatus() == SipResponse.SC_UNAUTHORIZED
					|| response.getStatus() == SipResponse.SC_PROXY_AUTHENTICATION_REQUIRED)
			{	
				request = createRegister(expires);
				if (_authInfo != null)
					request.addAuthHeader(response, _authInfo);
				if (request.getHeader(SipHeaders.AUTHORIZATION) == null
						&& request.getHeader(SipHeaders.PROXY_AUTHORIZATION) == null)
				{
					if (username == null)
						Assert.fail("Got " + response.getRequestLine() +  " and no authentication information provided");
					else
						request.addAuthHeader(response, username, password);
				}
			}
			else
				Assert.fail("Registration failed\n" + response);		
		}
		*/
	}
	
	public SipRequest createRegister(int expires) throws ServletException
	{
		SipRequest request;
		if (_session == null)
		{
			request = _client.createRequest(SipRequest.REGISTER, _from, _from);
			_session = request.getSession();
		}
		else
		{
			request = _session.createRequest(SipRequest.REGISTER);
			if (_client.getProxy() != null)
				request.pushRoute(_client.getProxy());
		}
		request.setExpires(expires);
		request.setAddressHeader(SipHeaders.CONTACT, _client.getContact());
		URI requestUri = request.getRequestURI();
		if (requestUri.isSipURI())
		{
			requestUri = _client.getSipFactory().createSipURI(null, ((SipURI) requestUri).getHost());
			request.setRequestURI(requestUri);
		}
		return request;
	}

	public AuthenticationInterceptor getAuthenticationInterceptor()
	{
		return _authenticationInterceptor;
	}

	public void setAuthenticationInterceptor(AuthenticationInterceptor authenticationInterceptor)
	{
		_authenticationInterceptor = authenticationInterceptor;
	}
		
}
