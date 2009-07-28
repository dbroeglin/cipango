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

package org.cipango.kaleo.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
 
public abstract class AbstractEventResource implements EventResource
{
	private String _uri;
	
	private Map<String, Subscription> _subscriptions = new HashMap<String, Subscription>();
	private List<EventResourceListener> _listeners = new ArrayList<EventResourceListener>();
	
	public AbstractEventResource(String uri)
	{
		_uri = uri;
	}
	
	public String getUri()
	{
		return _uri;
	}
	
	public abstract State getState();
	
	public void addListener(EventResourceListener listener)
	{
		synchronized (_listeners)
		{
			_listeners.add(listener);
		}
	}
	
	public List<EventResourceListener> getListeners()
	{
		synchronized (_listeners)
		{
			return new ArrayList<EventResourceListener>(_listeners);
		}
	}
	
	protected void fireStateChanged()
	{
		for (EventResourceListener listener : _listeners)
		{
			listener.stateChanged(this);
		}
	}
	
	protected void fireSubscriptionExpired(Subscription subscription)
	{
		for (EventResourceListener listener : _listeners)
		{
			listener.subscriptionExpired(subscription);
		}
	}
	
	public void addSubscription(Subscription subscription) 
	{
		synchronized (_subscriptions)
		{
			_subscriptions.put(subscription.getId(), subscription);
		}	
	}
	
	public List<Subscription> getSubscriptions()
	{
		synchronized (_subscriptions)
		{
			return new ArrayList<Subscription>(_subscriptions.values());
		}
	}
	
	public Subscription getSubscription(String id)
	{
		synchronized (_subscriptions)
		{
			return _subscriptions.get(id);
		}
	}
	
	public void refreshSubscription(String id, int expires) 
	{
		// TODO Auto-generated method stub	
	}
	
	public boolean hasSubscribers()
	{
		return (_subscriptions.size() != 0);
	}
	
	protected long nextSubscriptionExpirationTime()
	{
		long next = -1;
		
		for (Subscription subscription : _subscriptions.values())
		{
			long time = subscription.getExpirationTime();
			
			if (time > 0 && (time < next || next < 0))
				next = time;
		}
		return next;
	}
	
	public Subscription removeSubscription(String id) 
	{
		synchronized (_subscriptions) 
		{
			return _subscriptions.remove(id);
		}
	}
	
	protected void removeExpiredSubscriptions(long time)
	{
		Iterator<Subscription> it = _subscriptions.values().iterator();
		while (it.hasNext())
		{
			Subscription subscription = it.next();
			if (subscription.getExpirationTime() < time)
			{
				it.remove();
				fireSubscriptionExpired(subscription);
			}
		}
	}
	
	
}
