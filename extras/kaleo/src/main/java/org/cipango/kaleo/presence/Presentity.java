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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cipango.kaleo.event.Resource;
import org.cipango.kaleo.event.ResourceListener;
import org.cipango.kaleo.event.State;
import org.cipango.kaleo.event.Subscription;
import org.cipango.kaleo.presence.pidf.Presence;
import org.cipango.kaleo.presence.pidf.PresenceDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Presentity implements Resource
{
	public final Logger _log = LoggerFactory.getLogger(Presentity.class);
	
	private String _uri; 
	
	private List<State> _states = new ArrayList<State>();
	private Map<String, Subscription> _subscriptions = new HashMap<String, Subscription>();
	private List<ResourceListener> _listeners = new ArrayList<ResourceListener>();
	
	public Presentity(String uri, PresenceEventPackage presenceEventPackage)
	{
		_uri = uri;
	}
	
	public String getUri()
	{
		return _uri;
	}

	public void addState(State state, int expires)
	{
		if (_log.isDebugEnabled())
			_log.debug("added state {} to presentity {}", state, this);
		
		synchronized (_states)
		{
			_states.add(state);
		}
		fireStateChanged();
	}
	
	public State getState(String etag)
	{
		synchronized(_states)
		{
			for (State state : _states)
			{
				if (state.getETag().equals(etag))
					return state;
			}
		}
		return null;
	}

	public void removeState(String etag)
	{
		synchronized(_states)
		{
			for (int i = 0; i < _states.size(); i++)
			{
				State state = _states.get(i);
				if (state.getETag().equals(etag))
					_states.remove(i);
			}
		}
		fireStateChanged();
	}
	
	public void modifyState(State state, int expires, String contentType, Object content)
	{
		state.setContent(contentType, content);
		fireStateChanged();
	}
	
	public void refreshState(State state, int expires)
	{
		state.updateETag();
	}
	
	protected void fireStateChanged()
	{
		for (ResourceListener listener : _listeners)
		{
			listener.stateChanged(this);
		}
	}

	protected Content getNeutralContent() 
	{
		PresenceDocument document = PresenceDocument.Factory.newInstance();
		Presence presence = document.addNewPresence();
		presence.setEntity(_uri);
		//document.getPresence().addNewTuple().addNewStatus().setBasic(Basic.CLOSED);
		return new Content(document, PresenceEventPackage.PIDF);
	}
	
	protected Content getCurrentContent()
	{
		synchronized (_states)
		{
			if (_states.size() == 0)
				return null;
			State state = _states.get(0);
			return new Content(state.getContent(), PresenceEventPackage.PIDF);
		}
	}
	
	public Content getContent()
	{
		if(_states.isEmpty())
			return getNeutralContent();
		else
			return getCurrentContent();
	}

	@Override
	public String toString()
	{
		return _uri;
	}
	
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

	public void addSubscription(Subscription subscription, int expires) 
	{
		synchronized (_subscriptions)
		{
			_subscriptions.put(subscription.getId(), subscription);
		}	
		for (ResourceListener listener : _listeners)
		{
			listener.subscriptionAdded(subscription);
		}
	}

	public List<Subscription> getSubscriptions()
	{
		synchronized (_subscriptions)
		{
			return new ArrayList<Subscription>(_subscriptions.values());
		}
	}
	
	public void refreshSubscription(String id, int expires) 
	{
		// TODO Auto-generated method stub	
	}

	public void removeSubscription(String id, Subscription.Reason reason) 
	{
		// TODO Auto-generated method stub	
	}
	
	/*
	private Map<String, State> _states = new HashMap<String, State>();

	//TODO : authorized subscriptions / pending subscriptions

	private Map<String, Subscription> _subscriptions = new HashMap<String, Subscription>();
	private NotificationsManager _notificationManager;

	

	
	public void addState(State state, int expires)
	{
		synchronized(_states)
		{
			state.startTask(expires);
			_states.put(state.getETag(), state);
			notifySubscribers(state);
		}
		
	}

	public void removeState(String etag)
	{
		synchronized(_states)
		{
			removeEntryState(etag);
			notifySubscribers(null);
		}
	}
	
	private void removeEntryState(String etag)
	{
		synchronized(_states)
		{
			_states.get(etag).closeTask();
			_states.remove(etag);
		}
	}
	
	public State modifyState(State oldState, int expires, String contentType,
			Object content) 
	{
		removeEntryState(oldState.getETag());
		State newState = new State(this, contentType, content);
		addState(newState, expires);
		return newState;
	}
	
	private void notifySubscribers(State state) 
	{
		synchronized (_subscriptions) 
		{
			//Open for only change e-tag specific state notifications
			Iterator<Subscription> subscriptions = _subscriptions.values().iterator();
			while(subscriptions.hasNext())
			{
				Subscription subscription = subscriptions.next();
				int expires = subscription.getRemainingTime();
				_notificationManager.createNotification(_presence, subscription.getSession(), expires, getContent(), subscription.getState(), null);
			}
		}
	}

	public void refreshState(State state, int expires) 
	{
		removeEntryState(oldState.getETag());
		State newState = oldState.resetETag();
		synchronized(_states)
		{
			newState.startTask(expires);
			_states.put(newState.getETag(), newState);
		}
		return newState;
	} */

	/*
	public void addSubscription(Subscription subscription, int expires) 
	{
		synchronized (_subscriptions) 
		{
			if(expires > 0)
			{
				subscription.startTask(expires);
				_subscriptions.put(subscription.getId(), subscription);
			}
		}
	}

	public boolean isSubscribed(String id) 
	{
		synchronized (_subscriptions) 
		{
			return _subscriptions.get(id) != null;
		}
	}

	public void refreshSubscription(String id, int expires) 
	{
		synchronized (_subscriptions) 
		{
			_subscriptions.get(id).startTask(expires);
			notifySubscriber(_subscriptions.get(id), expires, null);
		}
	}

	private void notifySubscriber(Subscription subscription, int expires, Reason reason) 
	{
		_notificationManager.createNotification(_presence, subscription.getSession(), expires, getContent(), subscription.getState(), reason);
	}

	public void startSubscription(String id) 
	{
		synchronized (_subscriptions) 
		{
			Subscription subscription = _subscriptions.get(id);
			notifySubscriber(subscription, subscription.getRemainingTime(), null);
		}
	}	
	
	public void removeSubscription(String id, Reason reason) 
	{
		synchronized (_subscriptions) 
		{
			Subscription subscription = _subscriptions.get(id);
			subscription.closeTask();
			subscription.setState(Subscription.State.TERMINATED);
			notifySubscriber(subscription, 0, reason);
			_subscriptions.remove(id);
		}
	}

	private Content getNeutralContent() 
	{
		PresenceDocument document = PresenceDocument.Factory.newInstance();
		document.addNewPresence();
		document.getPresence().setEntity(_uri);
		return new Content(document, PresenceEventPackage.PIDF);
	}

	private Content getCurrentContent() 
	{
		//TODO: we suppose that we have only one state
		synchronized (_states) 
		{
			if (_states.size() == 0)
				return null;
			State state = _states.get(0);
			return new Content(state.getContent(), PresenceEventPackage.PIDF);
		}
	}
	*/
}
