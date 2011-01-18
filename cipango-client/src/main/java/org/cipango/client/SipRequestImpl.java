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

import static junit.framework.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.sip.Address;
import javax.servlet.sip.AuthInfo;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

public class SipRequestImpl extends SipMessageImpl implements SipRequest
{

	private List<SipResponse> _responses = new ArrayList<SipResponse>();
	
	public SipRequestImpl(SipServletRequest request)
	{
		super(request);
		
		// Ensure unique SipSession creation
		javax.servlet.sip.SipSession sipSession = request.getSession();
		if (sipSession.getAttribute(SipSession.class.getName()) == null)
			sipSession.setAttribute(SipSession.class.getName(), new Session(sipSession));
	}
	
	protected SipServletRequest request()
	{
		return (SipServletRequest) _message;
	}

	public SipResponse waitResponse()
	{
		synchronized (this)
		{
			SipResponse response = getUnreadResponse();
			if (response != null)
				return response;
			try { wait(UaManager.getResponseTimeout()); } catch (InterruptedException e) {}
			response = getUnreadResponse();
			if (response == null)
				fail("No response received for request: " + this);
			return response;
		}
	}
	
	private SipResponse getUnreadResponse()
	{
		for (SipResponse response: _responses)
		{
			SipResponseImpl resp = (SipResponseImpl) response;
			if (!resp.hasBeenRead())
			{
				resp.setHasBeenRead(true);
				return response;
			}
		}
		return null;
	}

	public void addAuthHeader(SipResponse challengeResponse, AuthInfo authInfo)
	{
		request().addAuthHeader(((SipResponseImpl) challengeResponse).response(), authInfo);
	}

	public void addAuthHeader(SipResponse challengeResponse, String username, String password)
	{
		request().addAuthHeader(((SipResponseImpl) challengeResponse).response(), username, password);
	}

	public SipRequest createCancel()
	{
		SipRequestImpl request = new SipRequestImpl(request().createCancel());
		((Session) getSession()).addHeaders(request);
		return request;
	}

	public SipResponse createResponse(int statuscode)
	{
		return createResponse(statuscode, null);
	}

	public SipResponse createResponse(int statusCode, String reasonPhrase)
	{
		SipResponseImpl response = new SipResponseImpl(request().createResponse(statusCode, reasonPhrase));
		
		((Session) getSession()).addHeaders(response);
		
		return response;
	}

	public int getMaxForwards()
	{
		return request().getMaxForwards();
	}

	public Address getPoppedRoute()
	{
		return request().getPoppedRoute();
	}

	public URI getRequestURI()
	{
		return request().getRequestURI();
	}

	public boolean isInitial()
	{
		return request().isInitial();
	}

	public void pushPath(Address uri)
	{
		request().pushPath(uri);
	}

	public void pushRoute(Address uri)
	{
		request().pushRoute(uri);
	}

	public void pushRoute(SipURI uri)
	{
		request().pushRoute(uri);
	}

	public void setMaxForwards(int n)
	{
		request().setMaxForwards(n);
	}

	public void setRequestURI(URI uri)
	{
		request().setRequestURI(uri);
	}
	
	public String getRequestLine()
    {
    	return getMethod() + " " + getRequestURI().toString(); 
    }

	public void addSipResponse(SipResponseImpl response)
	{
		_responses.add(response);
	}
	
	public List<SipResponse> getSipResponses()
	{
		return Collections.unmodifiableList(_responses);
	}


}
