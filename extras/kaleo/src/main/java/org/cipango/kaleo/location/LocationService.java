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

package org.cipango.kaleo.location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.cipango.util.PriorityQueue;
import org.mortbay.component.AbstractLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocationService extends AbstractLifeCycle
{
	private Map<String, RegistrationHolder> _registrations = new HashMap<String, RegistrationHolder>();
	
	private Thread _scheduler;
	private PriorityQueue _queue = new PriorityQueue();
	
	private Logger _log = LoggerFactory.getLogger(LocationService.class);
	
	@Override
	protected void doStart() throws Exception
	{
		new Thread(new Scheduler()).start();
	}
	
	@Override
	protected void doStop() throws Exception
	{
		if (_scheduler != null)
			_scheduler.interrupt();
	}
	
	public Registration getRegistration(String uri)
	{
		synchronized (_registrations)
		{
			RegistrationHolder holder = _registrations.get(uri);
			if (holder == null)
			{
				Registration registration = new Registration(uri);
				holder = new RegistrationHolder(registration);
				_registrations.put(uri, holder);
			}
			return holder.lock();
		}
	}
	
	public void put(Registration registration)
	{
		long time = registration.getNextExpirationTime();
		
		RegistrationHolder holder = null;
		synchronized (_registrations)
		{
			holder = _registrations.get(registration.getAor());
		}
		
		if (time > 0)
		{
		
			if (time < System.currentTimeMillis())
    			time = System.currentTimeMillis() + 100;
			
			synchronized (_queue)
    		{
    			_queue.offer(holder, time);
    			_queue.notifyAll();
    		}
		}
		
		if (registration.isEmpty())
		{
			synchronized (_registrations) 
			{
				_registrations.remove(registration.getAor());
			}
		}
		holder.unlock();
	}
	
	public List<Registration> getRegistrations()
	{
		ArrayList<Registration> registrations = new ArrayList<Registration>();
		synchronized (_registrations)
		{
			Iterator<RegistrationHolder> it = _registrations.values().iterator();
			while (it.hasNext())
				registrations.add(it.next()._registration);
		}
		return registrations;
	}
	
	public List<Binding> getBindings(String uri)
	{
		synchronized (_registrations)
		{
			RegistrationHolder holder = _registrations.get(uri);
			if (holder == null)
				return Collections.emptyList();
			return holder._registration.getBindings();
		}
	}
	
	class RegistrationHolder extends PriorityQueue.Node implements Runnable
	{
		private Registration _registration;
		private Lock _lock = new ReentrantLock();
		
		public RegistrationHolder(Registration registration)
		{
			super(Long.MAX_VALUE);
			_registration = registration;
		}
		
		public Registration lock()
		{
			_lock.lock();
			if (_log.isDebugEnabled())
				_log.debug("locked aor {}", _registration.getAor());
			return _registration;
		}
		
		public void unlock()
		{
			_lock.unlock();
			if (_log.isDebugEnabled())
				_log.debug("unlocked aor {}", _registration.getAor());
		}
		
		public void run()
		{
			lock();
			
			if (_log.isDebugEnabled())
				_log.debug("removing expired bindings for aor {}", _registration.getAor());
			try
			{
				List<Binding> expired = _registration.removeExpired(System.currentTimeMillis());
				
				if (_log.isDebugEnabled())
					_log.debug("removed expired bindings {} for aor  {}", expired, _registration.getAor());
			}
			catch (Throwable t)
			{
				_log.warn("exception while running timers {}", t);
			}
			finally
			{
				put(_registration);
			}
		}
	}
	
	class Scheduler extends Thread
	{
		public void run()
    	{
    		_scheduler = Thread.currentThread();
    		_scheduler.setName("registration-scheduler");
    		
    		try
    		{
    			do
    			{
    				try
    				{
    					RegistrationHolder holder;
    					long timeout;
    					
    					synchronized (_queue)
    					{
    						holder = (RegistrationHolder) _queue.peek();
    						timeout = (holder != null ? holder.getValue() - System.currentTimeMillis() : Long.MAX_VALUE);
    						if (timeout > 0)
    							_queue.wait(timeout);
    						else
    							_queue.poll();
    					}
    					if (timeout <= 0)
    						holder.run();
    				}
    				catch (InterruptedException e) { continue; }
    				catch (Throwable t) { _log.warn("Exception in scheduler {}", t); }
    			}
    			while (isStarted());
    		}
    		finally
    		{
    			_scheduler = null;
    			String exit = "Call scheduler exited";
    			if (isStarted())
    				_log.warn(exit);
    			else
    				_log.debug(exit);
    		}
    	}
	}
}
