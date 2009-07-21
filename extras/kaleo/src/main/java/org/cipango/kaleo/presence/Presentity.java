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
import java.util.List;

import org.cipango.kaleo.event.AbstractResource;
import org.cipango.kaleo.event.State;
import org.cipango.kaleo.presence.pidf.Presence;
import org.cipango.kaleo.presence.pidf.PresenceDocument;

public class Presentity extends AbstractResource
{	
	private List<SoftState> _states = new ArrayList<SoftState>();
	
	//private Map<String, Subscription> _subscriptions = new HashMap<String, Subscription>();
	//private List<ResourceListener> _listeners = new ArrayList<ResourceListener>();
	
	public Presentity(String uri)
	{
		super(uri);
	}

	public void addState(SoftState state, int expires)
	{
		synchronized (_states)
		{
			_states.add(state);
		}
		fireStateChanged();
	}
	
	public SoftState getState(String etag)
	{
		synchronized(_states)
		{
			for (SoftState state : _states)
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
				SoftState state = _states.get(i);
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
	
	public void refreshState(SoftState state, int expires)
	{
		state.updateETag();
	}

	protected State getNeutralState() 
	{
		PresenceDocument document = PresenceDocument.Factory.newInstance();
		Presence presence = document.addNewPresence();
		presence.setEntity(getUri());
		//document.getPresence().addNewTuple().addNewStatus().setBasic(Basic.CLOSED);
		return new State(PresenceEventPackage.PIDF, document);
	}
	
	protected State getCurrentState()
	{
		synchronized (_states)
		{
			if (_states.size() == 0)
				return null;
			return _states.get(0);
		}
	}
	
	public State getState()
	{
		if(_states.isEmpty())
			return getNeutralState();
		else
			return getCurrentState();
	}

	@Override
	public String toString()
	{
		return getUri();
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
