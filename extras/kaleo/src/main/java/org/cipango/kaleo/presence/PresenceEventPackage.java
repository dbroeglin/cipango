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

package org.cipango.kaleo.presence;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;

import org.cipango.kaleo.Constants;
import org.cipango.kaleo.event.AbstractEventPackage;
import org.cipango.kaleo.event.ContentHandler;
import org.cipango.kaleo.event.Notifier;
import org.cipango.kaleo.event.Resource;
import org.cipango.kaleo.event.ResourceListener;
import org.cipango.kaleo.event.State;
import org.cipango.kaleo.event.Subscription;
import org.cipango.kaleo.presence.pidf.PidfHandler;
import org.cipango.util.PriorityQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Presence Event Package
 */
public class PresenceEventPackage extends AbstractEventPackage<Presentity>
{
	public Logger _log = LoggerFactory.getLogger(PresenceEventPackage.class);
	
	public static final String NAME = "presence";
	public static final String PIDF = "application/pidf+xml";

	private Map<String, PresentityHolder> _presentities = new HashMap<String, PresentityHolder>();
	
	private PidfHandler _pidfHandler = new PidfHandler();
	private ResourceListener _presenceListener = new PresentityListener();
	private Notifier<Presentity> _notifier;
	
	public int _minStateExpires = 60;
	public int _maxStateExpires = 3600;
	public int _defaultStateExpires = 3600;
	
	private Thread _scheduler;
	private PriorityQueue _queue;
	
	public PresenceEventPackage()
	{
	}
	
	@Override
	protected void doStart() throws Exception
	{
		new Thread(new Scheduler()).start();
	}
	
	public String getName()
	{
		return NAME;
	}

	public int getMinStateExpires()
	{
		return _minStateExpires;
	}
	
	public int getMaxStateExpires()
	{
		return _maxStateExpires;
	}
	
	public int getDefaultStateExpires()
	{
		return _defaultStateExpires;
	}
	
	protected Presentity newResource(String uri)
	{
		return null;
	}
	
	public List<String> getSupportedContentTypes()
	{
		return Collections.singletonList(PIDF);
	}

	public Presentity getResource(String uri) 
	{
		synchronized (_presentities)
		{
			PresentityHolder holder = _presentities.get(uri);
			if (holder == null)
			{
				Presentity presentity = new Presentity(uri);
				presentity.addListener(_presenceListener);
				holder = new PresentityHolder(presentity);
				
				_presentities.put(uri, holder);
			}
			return holder.lock();
		}
	}

	public void put(Presentity presentity)
	{
		PresentityHolder holder = null;
		synchronized(_presentities)
		{
			holder = _presentities.get(presentity.getUri());
		}
		if (holder != null)
			put(holder);
	}
	
	protected void put(PresentityHolder holder)
	{
		holder.unlock();
	}
	
	public Iterator<String> getResourceUris()
	{
		synchronized (_presentities)
		{
			return _presentities.keySet().iterator();
		}
	}

	public ContentHandler<?> getContentHandler(String contentType)
	{
		if (PIDF.equals(contentType))
			return _pidfHandler;
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	protected void notify(Subscription subscription)
	{
		try
		{
			SipSession session = subscription.getSession();
			if (session.isValid())
			{
				SipServletRequest notify = session.createRequest(Constants.NOTIFY);
				notify.addHeader(Constants.EVENT, getName());
				
				String s = subscription.getState().getName();
				if (subscription.getState() == Subscription.State.ACTIVE)
					s = s + ";expires=" + ((subscription.getExpirationTime()-System.currentTimeMillis()) / 1000);
				notify.addHeader(Constants.SUBSCRIPTION_STATE, s);
				
				State state = subscription.getResource().getState();				
				ContentHandler handler = getContentHandler(state.getContentType());
				byte[] b = handler.getBytes(state.getContent());
				
				notify.setContent(b, state.getContentType());
				notify.send();
			}
		}
		catch (Exception e) 
		{
			_log.warn("Exception while sending notification {}", e);
		}
	}
	
	class PresentityListener implements ResourceListener
	{
		public void stateChanged(Resource resource) 
		{
			if (_log.isDebugEnabled())
				_log.debug("State changed for resource {}", resource);
			
			for (Subscription subscription : resource.getSubscriptions())
			{
				PresenceEventPackage.this.notify(subscription);
			}
		}

		public void subscriptionAdded(Subscription subscription) 
		{
			if (_log.isDebugEnabled())
				_log.debug("Subscription added {} for resource {}", subscription, subscription.getResource());
			
		}
		
		public void subscriptionStarted(Subscription subscription)
		{
			if (_log.isDebugEnabled())
				_log.debug("Subscription {} started", subscription);
			PresenceEventPackage.this.notify(subscription);
		}
	}
	
	class PresentityHolder 
	{
		private Presentity _presentity;
		private Lock _lock = new ReentrantLock();
		
		public PresentityHolder(Presentity presentity)
		{
			_presentity = presentity;
		}
		
		public Presentity lock()
		{
			_lock.lock();
			return _presentity;
		}
		
		public void unlock()
		{
			_lock.unlock();
		}
	}
	
	class Scheduler extends Thread
	{
		public void run()
		{
			_scheduler = Thread.currentThread();
			_scheduler.setName("presence-scheduler");
			
			try
			{
				do
				{
					try
					{
						PresentityHolder holder = null;
						synchronized (_queue)
						{
							_queue.wait();
						}
					}
					catch (InterruptedException e) { continue; }
    				catch (Throwable t) { _log.warn("Exception in scheduler", t); }
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
