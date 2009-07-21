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
import java.util.List;
import java.util.Map;

public abstract class AbstractResource implements Resource
{
	private String _uri;
	
	private Map<String, Subscription> _subscriptions = new HashMap<String, Subscription>();
	private List<ResourceListener> _listeners = new ArrayList<ResourceListener>();
	
	public AbstractResource(String uri)
	{
		_uri = uri;
	}
	
	public abstract State getState();
	
	public void addListener(ResourceListener listener)
	{
		synchronized (_listeners)
		{
			_listeners.add(listener);
		}
	}
	
	public List<ResourceListener> getListeners()
	{
		synchronized (_listeners)
		{
			return new ArrayList<ResourceListener>(_listeners);
		}
	}
	
	protected void fireStateChanged()
	{
		for (ResourceListener listener : _listeners)
		{
			listener.stateChanged(this);
		}
	}
	
	public void addSubscription(Subscription subscription) 
	{
		synchronized (_subscriptions)
		{
			System.out.println(this + " **** added " + subscription.getId());
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

	public Subscription removeSubscription(String id) 
	{
		System.out.println(this + " **** removing " + id);
		System.out.println(_subscriptions);
		synchronized (_subscriptions) 
		{
			return _subscriptions.remove(id);
		}
	}
	
	public String getUri()
	{
		return _uri;
	}
}
