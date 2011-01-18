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
package org.cipango.client.labs.interceptor;

import java.util.Iterator;
import java.util.ListIterator;

import javax.servlet.sip.Address;
import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.SipSession.State;

import org.cipango.client.labs.SipRequest;
import org.cipango.client.labs.SipRequestImpl;
import org.cipango.client.labs.SipResponse;
import org.cipango.client.labs.SipResponseImpl;
import org.cipango.sip.SipHeaders;
import org.cipango.sip.SipHeaders.HeaderInfo;
import org.eclipse.jetty.io.BufferCache.CachedBuffer;

public class AuthenticationInterceptor implements MessageInterceptor
{
	private static final String INTERCEPTED_REQUEST = "org.cipango.client.interceptor.interceptedRequest";
	
	private AuthInfo _authInfo;
	private String _username;
	private String _password;

	public AuthenticationInterceptor(AuthInfo authInfo)
	{
		_authInfo = authInfo;
	}
	
	public AuthenticationInterceptor(String username, String password)
	{
		_username = username;
		_password = password;
	}

	public boolean intercept(SipRequest request)
	{
		return false;
	}

	public boolean intercept(SipResponse response) throws Exception
	{
		if (response.getStatus() == SipResponse.SC_UNAUTHORIZED
				|| response.getStatus() == SipResponse.SC_PROXY_AUTHENTICATION_REQUIRED)
		{
			SipRequestImpl request = (SipRequestImpl) response.getSession().createRequest(response.getMethod());
			SipRequest initialRequest = response.getRequest();
			Iterator<String> it = response.getRequest().getHeaderNames();
			while (it.hasNext())
			{
				String name = it.next();
				CachedBuffer buffer = SipHeaders.getCachedName(name);
				HeaderInfo hi = SipHeaders.getType(buffer);
				if (!hi.isSystem())
				{
					Iterator<String> it2 = initialRequest.getHeaders(name.toString());
					while (it2.hasNext())
						request.addHeader(name, it2.next());
				}
				else if (SipHeaders.ROUTE_BUFFER.equals(buffer) && response.getSession().getState() == State.INITIAL)
				{
					ListIterator<Address> it2 = initialRequest.getAddressHeaders(name.toString());
					while (it2.hasNext())
						it2.next();
					while (it2.hasPrevious())
						request.pushRoute(it2.previous());
				}
			}
			if (initialRequest.getRawContent() != null)
				request.setContent(initialRequest.getRawContent(), initialRequest.getContentType());

			if (_authInfo != null)
				request.addAuthHeader(response, _authInfo);
			else
				request.addAuthHeader(response, _username, _password);
			
			request.setAttribute(INTERCEPTED_REQUEST, response.getRequest());
			((SipResponseImpl) response).setHasBeenRead(true);
			request.send();
			return true;
		}
		
		SipRequestImpl request = (SipRequestImpl) response.getRequest().getAttribute(INTERCEPTED_REQUEST);
		if (request != null)
		{
			synchronized (request)
			{
				request.addSipResponse((SipResponseImpl) response);

				request.notify();
			}
		}
		
		return false;
	}

}
