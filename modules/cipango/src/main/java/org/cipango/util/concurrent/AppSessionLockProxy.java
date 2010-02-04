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

package org.cipango.util.concurrent;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.URI;

import org.cipango.SessionManager;
import org.cipango.SessionManager.SessionTransaction;
import org.cipango.servlet.AppSession;
import org.cipango.servlet.AppSessionIf;
import org.cipango.servlet.Session;

public class AppSessionLockProxy implements AppSessionIf
{
	protected AppSession _appSession;

	public AppSessionLockProxy(AppSession appSession)
	{
		_appSession = appSession;
	}
	
	private transient SessionManager _callManager;
	
	protected SessionManager getCallManager()
	{
		if (_callManager == null)
			_callManager = _appSession.getCallSession().getServer().getSessionManager();
		return _callManager;
	}
	
	private SessionTransaction begin()
	{	
		return getCallManager().begin(_appSession.getCallSession());
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
			return new SessionLockProxy((Session) session);
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
				list.add(new SessionLockProxy((Session) session));
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
			List<SessionLockProxy> list = new ArrayList<SessionLockProxy>();
			while (it.hasNext())
			{
				Session session = (Session) it.next();
				list.add(new SessionLockProxy(session));
			}
			return list.iterator();
		}
		return it;
	}

	public SipSession getSipSession(String id) {
		// TODO returns SipSessionInterceptor ?
		Session session = (Session) _appSession.getSipSession(id);
		if (session != null)
			return new SessionLockProxy(session);
		return null;
	}

	public ServletTimer getTimer(String id) {
		ServletTimer timer = _appSession.getTimer(id);
		if (timer != null)
			return new TimerLockProxy(timer);
		return null;
	}

	public Collection<ServletTimer> getTimers() {
		Iterator<ServletTimer> it = _appSession.getTimers().iterator();
		if (!it.hasNext())
			return Collections.emptyList();
		Collection<ServletTimer> timers = new ArrayList<ServletTimer>();
		while (it.hasNext())
			timers.add(new TimerLockProxy(it.next()));
		return timers;
	}

	public void invalidate()
	{
		SessionTransaction transaction = begin();
		try
		{
			_appSession.invalidate();
		}
		finally
		{
			transaction.done();
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
		SessionTransaction transaction = begin();
		try
		{
			_appSession.removeAttribute(name);
		}
		finally
		{
			transaction.done();
		}
	}

	public void setAttribute(String name, Object value) 
	{
		SessionTransaction transaction = begin();
		try
		{
			_appSession.setAttribute(name, value);
		}
		finally
		{
			transaction.done();
		}
	}

	public int setExpires(int deltaMinutes) 
	{
		SessionTransaction workUnit = begin();
		try
		{
			return _appSession.setExpires(deltaMinutes);
		}
		finally
		{
			workUnit.done();
		}
	}

	public void setInvalidateWhenReady(boolean invalidateWhenReady)
	{
		SessionTransaction transaction = begin();
		try
		{
			_appSession.setInvalidateWhenReady(invalidateWhenReady);
		}
		finally
		{
			transaction.done();
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
