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

package org.cipango;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.cipango.servlet.AppSession;
import org.cipango.servlet.Session;
import org.cipango.sip.ClientTransaction;
import org.cipango.sip.ServerTransaction;
import org.cipango.sip.Transaction;
import org.mortbay.log.Log;
import org.mortbay.util.LazyList;

/**
 * Holds all information related to a given call (transactions, sessions, timers ...). 
 */
public class Call
{	
	private static final long serialVersionUID = 1L;

	private String _callId;
	
	private Object _clientTxs;
	private Object _serverTxs;
	private Object _sessions;

	private TimerQueue _timers = new TimerQueue();
	
	protected ReentrantLock _lock = new ReentrantLock();
	private Server _server;
	
	public Call(String id)
	{	
		_callId = id;
	}
	
	public String getCallId()
	{
		return _callId;
	}
	
	public void setServer(Server server) 
	{
		_server = server;
	}
	
	public Server getServer() 
	{
		return _server;
	}
	
	// lock
	
	public Call lock() 
	{
		_lock.lock();
		return this;
	}
	
	public void unlock() 
	{
		_lock.unlock();
	}
	
	public int getHoldCount()
	{
		return _lock.getHoldCount();
	}
	
	public void checkOwner()
	{
		if (!_lock.isHeldByCurrentThread())
			throw new IllegalStateException("!owner");
	}
	
	// transactions
	
	public synchronized void addServerTx(ServerTransaction tx)
	{
		_serverTxs = LazyList.add(_serverTxs, tx);
	}
	
	public synchronized void addClientTx(ClientTransaction tx)
	{
		_clientTxs = LazyList.add(_clientTxs, tx);
	}
	
	public synchronized ServerTransaction getServerTx(String key) 
	{
		for (int i = LazyList.size(_serverTxs); i-->0;)
		{
			ServerTransaction tx = (ServerTransaction) LazyList.get(_serverTxs, i);
			if (tx.getKey().equals(key))
				return tx;
		}
		return null;
	}
	
	public ClientTransaction getClientTx(String key) 
	{
		for (int i = LazyList.size(_clientTxs); i-->0;)
		{
			ClientTransaction tx = (ClientTransaction) LazyList.get(_clientTxs, i);
			if (tx.getKey().equals(key))
				return tx;
		}
		return null;
	}
	
	public synchronized void removeServerTx(ServerTransaction tx) 
	{
		_serverTxs = LazyList.remove(_serverTxs, tx);
	}
	
	public synchronized void removeClientTx(ClientTransaction tx) 
	{
		_clientTxs = LazyList.remove(_clientTxs, tx);
	}
	
	public synchronized List<ServerTransaction> getServerTransactions(Session session) 
	{
		List<ServerTransaction> list = new ArrayList<ServerTransaction>();
		for (int i = LazyList.size(_serverTxs); i-->0;)
		{
			ServerTransaction tx = (ServerTransaction) LazyList.get(_serverTxs, i);
			if (tx.getRequest().session().equals(session))
				list.add(tx);
		}
		return list;
	}
	
	public synchronized boolean hasRunningTransactions(Session session)
	{
		for (int i = LazyList.size(_clientTxs); i-->0;)
		{
			ClientTransaction tx = (ClientTransaction) LazyList.get(_clientTxs, i);
			if (tx.getState() < Transaction.STATE_COMPLETED 
					&& tx.getRequest().session().equals(session))
				return true;
		}
		for (int i = LazyList.size(_serverTxs); i-->0;)
		{
			ServerTransaction tx = (ServerTransaction) LazyList.get(_serverTxs, i);
			if (tx.getState() < Transaction.STATE_COMPLETED 
					&& tx.getRequest().session().equals(session))
				return true;
		}
		return false;
	}
	
	public synchronized List<ClientTransaction> getClientTransactions(Session session) 
	{
		List<ClientTransaction> list = new ArrayList<ClientTransaction>();
		for (int i = LazyList.size(_clientTxs); i-->0;)
		{
			ClientTransaction tx = (ClientTransaction) LazyList.get(_clientTxs, i);
			if (tx.getRequest().session().equals(session))
				list.add(tx);
		}
		return list;
	}
	
	// sessions
	
	public AppSession newSession()
    {
        AppSession session = new AppSession(this);
        synchronized (this)
        {
        	_sessions = LazyList.add(_sessions, session);
        }
        return session;
    }
	
	public synchronized AppSession getSession(String id)
    {
		for (int i = LazyList.size(_sessions); i-->0;)
		{
			AppSession session = (AppSession) LazyList.get(_sessions, i);
			if (session.getAid().equals(id))
				return session;
		}
		return null;
    }

	public synchronized void removeSession(AppSession session)
	{
		_sessions = LazyList.remove(_sessions, session);
	}
	
	public Session findSession(SipRequest request) 
	{
		String encodedId = request.getEncodedId();
		String dialogId = request.getDialogId();
		
		if (encodedId != null) 
		{
			AppSession session = getSession(encodedId);
			return session == null ? null : session.getAppSession().getSession(dialogId);
		} 
		else 
		{
			Session proxy = null;
			synchronized (this)
			{
				for (int i = LazyList.size(_sessions); i--> 0;)
				{
					AppSession asession = (AppSession) LazyList.get(_sessions, i);
					Session session = asession.getAppSession().getSession(dialogId);
					if (session != null && !session.isProxy())
						return session;
					else 
						proxy = session; 
				}
			}
			return proxy; // useful when upstream element does not set aid
		}
	}
	
	public Session findSession(SipResponse response) 
	{
		String dialogId = response.getDialogId(false);

		Session proxy = null;
		synchronized (this) 
		{
			for (int i = LazyList.size(_sessions); i--> 0;)
			{
				AppSession asession = (AppSession) LazyList.get(_sessions, i);
				Session session = asession.getAppSession().getSession(dialogId);
				if (session != null && !session.isProxy()) 
				{
					Log.debug("Found session: " + session);
					return session;
				} 
				else 
				{
					proxy = session;
				}
			}
		}
		return proxy;
	}
	
	// timers
	
	public TimerTask schedule(Runnable runnable, long delay)
    {
		checkOwner();
		
    	TimerTask timer = new TimerTask(runnable, System.currentTimeMillis() + delay);
		_timers.addTimer(timer);
    	return timer;
    }
	
	public void cancel(TimerTask timer)
	{
		checkOwner();
		
		if (timer != null)
		{
			timer.cancel();
			_timers.remove(timer);
		}
	}
	
	public void runTimers()
	{ 
		long now = System.currentTimeMillis();
		
		TimerTask timer = null;
		
		while ((timer = _timers.getExpired(now)) != null)
		{
			if (!timer.isCancelled())
			{
				try
				{
					//System.out.println("Running " + timer.getRunnable());
					timer.getRunnable().run();
				}
				catch (Throwable t)
				{
					Log.warn(t);
				}
			}
		}
	}
	
	public long nextExecutionTime()
	{
		TimerTask timer = (TimerTask) _timers.peek();
		return timer != null ? timer.getExecutionTime() : -1;
	}
    
	public synchronized boolean isDone()
	{
		return (_timers.size() == 0) && (LazyList.size(_sessions) == 0) && (LazyList.size(_clientTxs) == 0) && (LazyList.size(_serverTxs) == 0);
	}
	
    public String toString()
    {
    	StringBuffer sb = new StringBuffer();
    	sb.append("[stxs= " + _serverTxs + ", ctxs=" + _clientTxs + ", timers=" + _timers + ", sessions=" + _sessions + "]");
    	return sb.toString();
    }
    
    public class TimerTask implements Comparable<TimerTask>, Serializable
    {
		private static final long serialVersionUID = 1L;
		
		private Runnable _runnable;
    	private long _executionTime;
    	private boolean _cancelled = false;
    	
    	public TimerTask(Runnable runnable, long executionTime)
    	{
    		_runnable = runnable;
    		_executionTime = executionTime;
    	}
    	
    	public long getExecutionTime()
    	{
    		return _executionTime;
    	}
    	
    	public Runnable getRunnable()
    	{
    		return _runnable;
    	}
    	
		public int compareTo(TimerTask t)
		{
			long executionTimeOther = t._executionTime;
			return _executionTime < executionTimeOther ? -1 : (_executionTime == executionTimeOther ? 0 : 1);
		}
		
		public boolean isCancelled()
		{
			return _cancelled;
		}
		
		public void cancel()
		{
			_cancelled = true;
		}
		
		public String toString()
		{
			return _runnable + " @ " + (_executionTime - System.currentTimeMillis()) / 1000;
		}
    }
    
    class TimerQueue extends ArrayList<TimerTask>
    {
		private static final long serialVersionUID = 1L;

		public synchronized void addTimer(TimerTask timer)
    	{ 
	    	int index = 0;
			
	    	while (index < size() && (get(index).compareTo(timer) < 0))
	    		index++;
	    	
	    	add(index, timer);
    	}
    	
    	public synchronized TimerTask getExpired(long time)
    	{
    		if (size() != 0)
    		{
    			TimerTask timer = (TimerTask) get(0);
    			if (timer._executionTime <= time)
    				return (TimerTask) remove(0);
    		}
    		return null;
    	}
    	
    	public synchronized TimerTask peek()
    	{
    		return size() != 0 ? (TimerTask) get(0) : null;
    	}
    }
}