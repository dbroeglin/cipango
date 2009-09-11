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

package org.cipango.kaleo;

import java.util.ArrayList;
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

public abstract class AbstractResourceManager<T extends Resource> extends AbstractLifeCycle
{	
	private Thread _scheduler;
	private PriorityQueue _queue = new PriorityQueue();
	
	private Map<String, ResourceHolder> _resources = new HashMap<String, ResourceHolder>();
		
	protected Logger _log = LoggerFactory.getLogger(AbstractResourceManager.class);
		
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
	
	public List<T> getResources()
	{
		List<T> resources = new ArrayList<T>();
		synchronized (_resources)
		{
			Iterator<ResourceHolder> it = _resources.values().iterator();
			while (it.hasNext())
				resources.add(it.next().getResource());
		}
		return resources;
	}
	
	protected ResourceHolder getHolder(String uri)
	{
		synchronized (_resources)
		{
			return _resources.get(uri);
		}
	}
	
	public T get(String uri)
	{
		synchronized (_resources)
		{
			ResourceHolder holder = _resources.get(uri);
			if (holder == null)
			{
				holder = new ResourceHolder(newResource(uri));
				_resources.put(uri, holder);
			}
			return holder.lock();
		}
	}
	
	public boolean contains(String uri)
	{
		synchronized (_resources)
		{
			return _resources.containsKey(uri);
		}
	}
	
	public void put(T resource)
	{
		ResourceHolder holder = null;
		synchronized (_resources)
		{
			holder = _resources.get(resource.getUri());
		}
		if (holder != null)
			put(holder);
	}
	
	protected void put(ResourceHolder holder)
	{
		T resource = holder.getResource();
		long time = resource.nextTimeout();
		
		synchronized (_queue)
		{
			if (time > 0)
			{
				if (time < System.currentTimeMillis())
					time = System.currentTimeMillis() + 100;
				_queue.offer(holder, time);
			}
			else 
			{
				_queue.remove(holder);
			}
			_queue.notifyAll();
		}
		
		if (resource.isDone())
		{
			synchronized (_resources)
			{
				_resources.remove(resource.getUri());
			}
		}
		holder.unlock();
	}
	
	protected abstract T newResource(String uri);
	
	protected class ResourceHolder extends PriorityQueue.Node implements Runnable
	{
		private T _resource;
		private Lock _lock = new ReentrantLock();
		
		ResourceHolder(T resource) 
		{
			super(Long.MAX_VALUE);
			_resource = resource;
		}
		
		public T getResource()
		{
			return _resource;
		}
		
		public T lock()
		{
			_lock.lock();
			return _resource;
		}
		
		public void unlock()
		{
			_lock.unlock();
		}
		
		public void run()
		{
			lock();
			if (_log.isDebugEnabled())
				_log.debug("running timeout for resource " + _resource);
			try
			{
				_resource.doTimeout(System.currentTimeMillis());
			}
			finally
			{
				put(this);
			}
		}
		
		@Override
		public String toString()
		{
			return _resource.toString();
		}
	}
	
	
	class Scheduler implements Runnable
	{
		public void run()
		{
			_scheduler = Thread.currentThread();
			_scheduler.setName(AbstractResourceManager.this.getClass().getSimpleName() + " - scheduler");
			try
			{
				do
				{
					try
					{
						ResourceHolder holder = null;
						long timeout;
						
						synchronized (_queue)
						{
							holder = (ResourceHolder) _queue.peek();
							timeout = (holder != null) ? (holder.getValue() - System.currentTimeMillis()) : Long.MAX_VALUE;
							
							if (_log.isDebugEnabled())
								_log.debug("next timeout in {} for node {}", timeout, holder);
							
							if (timeout > 0)
								_queue.wait(timeout);
							else
								_queue.poll();
						}
						if (timeout <= 0)
							holder.run();
					}
					catch (InterruptedException e) { continue; }
					catch (Throwable t) { _log.warn("exception in scheduler", t); };
				}
				while (isStarted());
			}
			finally
			{
				_scheduler = null;
				String exit = Thread.currentThread().getName() + " exited";
				if (isStarted())
					_log.warn(exit);
				else
					_log.debug(exit);
			}
		}
	}
}
