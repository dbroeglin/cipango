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

import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import junit.framework.Assert;

import org.cipango.client.SipRequest;
import org.cipango.client.SipResponse;
import org.cipango.client.SipSession;
import org.cipango.client.UA;
import org.cipango.client.interceptor.AuthenticationInterceptor;
import org.cipango.sip.SipHeaders;

public class RegisterSession
{
	private SipSession _session;
	private UA _ua;
	private AuthenticationInterceptor _authenticationInterceptor;
	
	public RegisterSession(UA ua)
	{
		_ua = ua;
	}
		
	/**
	 * <pre>
	 * SipUnit                    Remote
	 * | REGISTER                   |
	 * |--------------------------->| 
	 * |                        200 | 
	 * |<---------------------------|     
	 * </pre>
	 * @throws ServletParseException 
	 */
	public void register(int expires) throws ServletParseException
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
	 * @throws ServletParseException 
	 */
	public void register(int expires, String username, String password) throws ServletParseException
	{
		SipRequest request = createRegister(expires);
		if (_authenticationInterceptor != null)
			request.getSession().addMessageInterceptor(_authenticationInterceptor);
		else if (username != null)
		{
			_authenticationInterceptor = new AuthenticationInterceptor(username, password);
			request.getSession().addMessageInterceptor(_authenticationInterceptor);
		}
		request.send();
		SipResponse response = request.waitResponse();
		
		if (!response.is2xx())
			Assert.fail("Registration failed\n" + response);	
	}
	
	public SipRequest createRegister(int expires) throws ServletParseException
	{
		SipRequest request;
		if (_session == null)
		{
			request = _ua.createRequest(SipRequest.REGISTER, _ua.getAor());
			_session = request.getSession();
		}
		else
		{
			request = _session.createRequest(SipRequest.REGISTER);
			if (_ua.getProxy() != null)
				request.pushRoute(_ua.getProxy());
		}
		request.setExpires(expires);
		request.setAddressHeader(SipHeaders.CONTACT, _ua.getContact());
		URI requestUri = request.getRequestURI();
		if (requestUri.isSipURI())
		{
			requestUri = _ua.getSipFactory().createSipURI(null, ((SipURI) requestUri).getHost());
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
