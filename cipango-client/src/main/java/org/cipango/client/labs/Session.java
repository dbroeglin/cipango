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

import static junit.framework.Assert.fail;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.sip.Address;
import javax.servlet.sip.SipSession.State;

import org.cipango.client.labs.interceptor.MessageInterceptor;

public class Session implements SipSession
{
	private javax.servlet.sip.SipSession _session;
	private List<SipRequestImpl> _requests = new ArrayList<SipRequestImpl>();
	private Map<String,List<String>> _headers;
	private List<MessageInterceptor> _interceptors = new ArrayList<MessageInterceptor>();
	
	protected Session(javax.servlet.sip.SipSession session)
	{
		setSipSession(session);
	}
	
	public SipRequest waitRequest()
	{
		synchronized (this)
		{
			SipRequest request = getUnreadRequests();
			if (request != null)
				return request;
			try { wait(UaManager.getRequestTimeout()); } catch (InterruptedException e) {}
			request = getUnreadRequests();
			if (request == null)
				fail("No requests received on session: " + this);
			return request;
		}
	}
	

	private SipRequest getUnreadRequests()
	{
		for (SipRequestImpl request: _requests)
		{
			if (!request.hasBeenRead())
			{
				request.setHasBeenRead(true);
				return request;
			}
		}
		return null;
	}
	
	public void addSipRequest(SipRequestImpl sipRequest)
	{
		_requests.add(sipRequest);
	}
	
	public void setSipSession(javax.servlet.sip.SipSession session)
	{
		if (_session != null)
			throw new IllegalStateException("Session already set");
		_session = session;
		if (session != null)
			_session.setAttribute(SipSession.class.getName(), this);
	}

	public SipRequest createRequest(String method)
	{
		SipRequest request = new SipRequestImpl(_session.createRequest(method));
		addHeaders(request);
		return request;
	}

	public Object getAttribute(String name)
	{
		return _session.getAttribute(name);
	}

	public Enumeration<String> getAttributeNames()
	{
		return _session.getAttributeNames();
	}

	public String getCallId()
	{
		return _session.getCallId();
	}

	public long getCreationTime()
	{
		return _session.getCreationTime();
	}

	public String getId()
	{
		return _session.getId();
	}

	public boolean getInvalidateWhenReady()
	{
		return _session.getInvalidateWhenReady();
	}

	public void setInvalidateWhenReady(boolean invalidateWhenReady)
	{
		_session.setInvalidateWhenReady(invalidateWhenReady);
	}

	public long getLastAccessedTime()
	{
		return _session.getLastAccessedTime();
	}

	public Address getLocalParty()
	{
		return _session.getLocalParty();
	}

	public Address getRemoteParty()
	{
		return _session.getRemoteParty();
	}

	public State getState()
	{
		return _session.getState();
	}

	public void invalidate()
	{
		_session.invalidate();
	}

	public boolean isValid()
	{
		return _session.isValid();
	}

	public boolean isReadyToInvalidate()
	{
		return _session.isReadyToInvalidate();
	}

	public void removeAttribute(String name)
	{
		_session.removeAttribute(name);
	}

	public void setAttribute(String name, Object attribute)
	{
		_session.setAttribute(name, attribute);
	}
	
	@Override
	public int hashCode()
	{
		return _session.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		return _session.equals(obj);
	}

	@Override
	public String toString()
	{
		if (_session == null)
			return "UAS session";
		return _session.toString();
	}

	public void setHeaders(Map<String,List<String>> headers)
	{
		_headers = headers;
	}
	
	public Map<String,List<String>> getHeaders()
	{
		return _headers;
	}
	
    /**
     * Add headers set in session with {@link #setHeaders(List)} on the messsage.
     */
    public void addHeaders(SipMessage message)
    {
    	if (_headers != null)
		{
			for (Entry<String, List<String>> header : _headers.entrySet())
			{
				for (String value: header.getValue())
					message.addHeader(header.getKey(), value);
			}
		}		
    }

	public void addMessageInterceptor(MessageInterceptor interceptor)
	{
		_interceptors.add(interceptor);
	}

	public List<MessageInterceptor> getMessageInterceptors()
	{
		return _interceptors;
	}
	
}
