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

package org.cipango.diameter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.sip.SipApplicationSession;

import org.cipango.server.session.AppSessionIf;
import org.cipango.server.session.CallSession;
import org.cipango.server.session.SessionManager.SessionScope;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.statistic.CounterStatistic;

public class SessionManager 
{
	private long _startTimestamp = ((System.currentTimeMillis() / 1000) & 0xffffffffl);
	private long _id;
	
	private Node _node;
	private Map<String, DiameterSession> _sessions = new HashMap<String, DiameterSession>();
	
	private final AtomicLong _statsStartedAt = new AtomicLong(-1L);
	private CounterStatistic _sessionsCounter = new CounterStatistic();
	
	public DiameterSession createSession(SipApplicationSession appSession)
	{
		DiameterSession diameterSession = new DiameterSession(appSession, newSessionId());
		synchronized (_sessions)
		{
			_sessions.put(diameterSession.getId(), diameterSession);
			if (getStatsOn())
				_sessionsCounter.increment();
		}
		diameterSession.setNode(_node);
		return diameterSession;
	}
	
	protected DiameterSession newSession()
	{
		DiameterSession diameterSession = new DiameterSession(null, newSessionId());
		
		return diameterSession;
	}
	
	public DiameterSession get(String id)
	{
		synchronized(_sessions)
		{
			return _sessions.get(id);
		}
	}
	
	/*
	public DiameterSession openScope(String id)
	{
		DiameterSession session = null;
		synchronized(_sessions)
		{
			session = _sessions.get(id);
			if (session == null)
			{
				session = newSession();
				_sessions.put(session.getId(), session);			
			}
		}
		AppSessionIf appSession = (AppSessionIf) session.getApplicationSession();
		if (appSession != null && appSession.getAppSession().getCallSession() != null)
		{
			CallSession callSession = appSession.getAppSession().getCallSession();
			_node.getServer().getSessionManager().openScope(callSession);
		}
		return session;
	}*/
	
	public SessionScope openScope(SipApplicationSession session)
	{
		AppSessionIf appSession = (AppSessionIf) session;
		if (appSession != null && appSession.getAppSession().getCallSession() != null)
		{
			CallSession callSession = appSession.getAppSession().getCallSession();
			return _node.getServer().getSessionManager().openScope(callSession);
		}
		return null;
	}
		
	public void removeSession(DiameterSession session)
	{
		synchronized(_sessions)
		{
			_sessions.remove(session.getId());
			if (getStatsOn())
				_sessionsCounter.decrement();
		}
	}
	
	protected synchronized String newSessionId()
	{
		return _node.getIdentity() + ";" + _startTimestamp + ";" + (++_id);
	}
	
	public void setNode(Node node)
	{
		_node = node;
	}
	
	public long getCurrentSessions()
	{
		return _sessionsCounter.getCurrent();
	}
	
	public long getMaxSessions()
	{
		return _sessionsCounter.getMax();
	}
	
	public long getTotalSessions()
	{
		return _sessionsCounter.getTotal();
	}
	
	public void statsReset()
    {
        updateNotEqual(_statsStartedAt,-1,System.currentTimeMillis());

        _sessionsCounter.reset();
    }
	
	public void setStatsOn(boolean on)
    {
        if (on && _statsStartedAt.get() != -1)
            return;

        Log.debug("Statistics on = " + on + " for " + this);

        statsReset();
        _statsStartedAt.set(on?System.currentTimeMillis():-1);
    }
	
	public boolean getStatsOn()
    {
        return _statsStartedAt.get() != -1;
    }
	
	public long getStatsStartedAt()
	{
		return _statsStartedAt.get();
	}
	
	private void updateNotEqual(AtomicLong valueHolder, long compare, long value)
    {
        long oldValue = valueHolder.get();
        while (compare != oldValue)
        {
            if (valueHolder.compareAndSet(oldValue,value))
                break;
            oldValue = valueHolder.get();
        }
    }
}
