// ========================================================================
// Copyright 2008-2009 NEXCOM Systems
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

package org.cipango.server.session.scope;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.URI;

import org.cipango.server.session.SessionManager.SessionScope;
import org.cipango.server.session.AppSession;
import org.cipango.server.session.AppSessionIf;
import org.cipango.server.session.CallSession;
import org.cipango.server.session.Session;

public class ScopedAppSession extends ScopedObject implements AppSessionIf
{
	protected AppSession _appSession;

	public ScopedAppSession(AppSession appSession)
	{
		_appSession = appSession;
	}
	
	public CallSession getCallSession()
	{
		return _appSession.getCallSession();
	}
	
	public void encodeURI(URI uri) 
	{
		_appSession.encodeURI(uri);
	}

	public URL encodeURL(URL url) 
	{
		return _appSession.encodeURL(url);
	}

	public String getApplicationName() 
	{
		return _appSession.getApplicationName();
	}

	public Object getAttribute(String name) 
	{
		return _appSession.getAttribute(name);
	}

	public Iterator<String> getAttributeNames() 
	{
		return _appSession.getAttributeNames();
	}

	public long getCreationTime() 
	{
		return _appSession.getCreationTime();
	}

	public long getExpirationTime() 
	{
		return _appSession.getExpirationTime();
	}

	public String getId() 
	{
		return _appSession.getId();
	}

	public boolean getInvalidateWhenReady() 
	{
		return _appSession.getInvalidateWhenReady();
	}

	public long getLastAccessedTime() 
	{
		return _appSession.getLastAccessedTime();
	}

	public Object getSession(String id, Protocol protocol) 
	{
		Object session = _appSession.getSession(id, protocol);
		if (session instanceof Session)
			return new ScopedSession((Session) session);
		return session;
	}

	@SuppressWarnings("unchecked")
	public Iterator<?> getSessions() 
	{
		// TODO returns SipSessionInterceptor ?
		List list = new ArrayList();
		Iterator it = _appSession.getSessions();
		while (it.hasNext())
		{
			Object session = (Object) it.next();
			if (session instanceof Session)
				list.add(new ScopedSession((Session) session));
			else
				list.add(session);
		}
		
		return list.iterator();
	}

	public Iterator<?> getSessions(String protocol) 
	{
		// TODO returns SipSessionInterceptor ?
		Iterator<?> it = _appSession.getSessions(protocol);
		if (Protocol.SIP.toString().equalsIgnoreCase(protocol))
		{
			List<ScopedSession> list = new ArrayList<ScopedSession>();
			while (it.hasNext())
			{
				Session session = (Session) it.next();
				list.add(new ScopedSession(session));
			}
			return list.iterator();
		}
		return it;
	}

	public SipSession getSipSession(String id) {
		// TODO returns SipSessionInterceptor ?
		Session session = (Session) _appSession.getSipSession(id);
		if (session != null)
			return new ScopedSession(session);
		return null;
	}

	public ServletTimer getTimer(String id) {
		ServletTimer timer = _appSession.getTimer(id);
		if (timer != null)
			return new ScopedTimer(timer);
		return null;
	}

	public Collection<ServletTimer> getTimers() {
		Iterator<ServletTimer> it = _appSession.getTimers().iterator();
		if (!it.hasNext())
			return Collections.emptyList();
		Collection<ServletTimer> timers = new ArrayList<ServletTimer>();
		while (it.hasNext())
			timers.add(new ScopedTimer(it.next()));
		return timers;
	}

	public void invalidate()
	{
		SessionScope scope = openScope();
		try
		{
			_appSession.invalidate();
		}
		finally
		{
			scope.close();
		}
	}

	public boolean isReadyToInvalidate() 
	{
		return _appSession.isReadyToInvalidate();
	}

	public boolean isValid() 
	{
		return _appSession.isValid();
	}

	public void removeAttribute(String name)
	{
		SessionScope scope = openScope();
		try
		{
			_appSession.removeAttribute(name);
		}
		finally
		{
			scope.close();
		}
	}

	public void setAttribute(String name, Object value) 
	{
		SessionScope scope = openScope();
		try
		{
			_appSession.setAttribute(name, value);
		}
		finally
		{
			scope.close();
		}
	}

	public int setExpires(int deltaMinutes) 
	{
		SessionScope scope = openScope();
		try
		{
			return _appSession.setExpires(deltaMinutes);
		}
		finally
		{
			scope.close();
		}
	}

	public void setInvalidateWhenReady(boolean invalidateWhenReady)
	{
		SessionScope scope = openScope();
		try
		{
			_appSession.setInvalidateWhenReady(invalidateWhenReady);
		}
		finally
		{
			scope.close();
		}
	}
	
	public AppSession getAppSession()
	{
		return _appSession;
	}
		
	@Override
	public String toString()
	{
		return _appSession.toString();
	}

	@Override
	public boolean equals(Object o)
	{
		return _appSession.equals(o);
	}

	@Override
	public int hashCode()
	{
		return _appSession.hashCode();
	}

}
