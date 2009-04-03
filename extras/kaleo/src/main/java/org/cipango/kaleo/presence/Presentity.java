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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;

import org.cipango.kaleo.Constants;
import org.cipango.kaleo.event.PublishTimedValue;
import org.cipango.kaleo.event.Resource;
import org.cipango.kaleo.event.State;
import org.cipango.kaleo.event.SubscribeTimedValue;
import org.cipango.kaleo.event.Subscription;
import org.cipango.kaleo.event.TimedValue;
import org.cipango.kaleo.presence.pidf.PresenceDocument;
import org.cipango.kaleo.util.Timer;

public class Presentity implements Resource
{
	private String _uri; 

	private Map<String, TimedValue<State, ExpirationPublishingTask>> _states 
	= new HashMap<String, TimedValue<State, ExpirationPublishingTask>>();

	//TODO : authorized subscriptions / pending subscriptions

	private Map<String, TimedValue<Subscription, ExpirationSubscriptionTask>> _subscriptions 
	= new HashMap<String, TimedValue<Subscription,ExpirationSubscriptionTask>>();

	private PresenceEventPackage _presence;

	public Presentity(String uri, PresenceEventPackage presenceEventPackage)
	{
		_uri = uri;
		_presence = presenceEventPackage;
	}

	public String getUri()
	{
		return _uri;
	}

	public void addState(State state, int expires)
	{
		synchronized(_states)
		{
			PublishTimedValue p = new PublishTimedValue(state, new ExpirationPublishingTask(state.getETag(), expires));
			_states.put(state.getETag(), p);
			notifySubscribers(state);
		}
	}

	public State getState(String etag)
	{
		synchronized(_states)
		{
			return _states.get(etag).getValue();
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
		State newState = new State(contentType, content);
		addState(newState, expires);
		return newState;
	}


	private void notifySubscribers(State state) 
	{
		synchronized (_subscriptions) 
		{
			//Open for only change e-tag specific state notifications
			Iterator<TimedValue<Subscription, ExpirationSubscriptionTask>> subscriptions = _subscriptions.values().iterator();
			while(subscriptions.hasNext())
			{
				TimedValue<Subscription, ExpirationSubscriptionTask> subscription = subscriptions.next();
				int expires = subscription.getTask().getRemainingTime();
				_presence.createNotification(subscription.getValue().getSession(), this, expires, getContent(), subscription.getValue().getState(), null);
			}
		}
	}

	public State refreshState(State oldState, int expires) 
	{
		removeEntryState(oldState.getETag());
		State newState = oldState.resetETag();
		synchronized(_states)
		{
			PublishTimedValue p = new PublishTimedValue(newState, new ExpirationPublishingTask(newState.getETag(), expires));
			_states.put(newState.getETag(), p);
		}
		return newState;
	}

	public void addSubscription(Subscription subscription, int expires) 
	{
		synchronized (_subscriptions) 
		{
			if(expires > 0)
			{
				SubscribeTimedValue p = new SubscribeTimedValue(subscription, new ExpirationSubscriptionTask(subscription.getId(), expires));
				_subscriptions.put(subscription.getId(), p);
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
			_subscriptions.get(id).resetTask(new ExpirationSubscriptionTask(id, expires));
			notifySubscriber(_subscriptions.get(id).getValue(), expires, null);
		}
	}

	private void notifySubscriber(Subscription subscription, int expires, String reason) 
	{
		_presence.createNotification(subscription.getSession(), this, expires, getContent(), subscription.getState(), reason);
	}

	public void startSubscription(String id) 
	{
		synchronized (_subscriptions) 
		{
			TimedValue<Subscription,ExpirationSubscriptionTask> subscriptionTimed = _subscriptions.get(id);
			notifySubscriber(subscriptionTimed.getValue(), subscriptionTimed.getTask().getRemainingTime(), null);
		}
	}

	public class ExpirationPublishingTask extends TimerTask
	{
		String _etag;

		public ExpirationPublishingTask(String etag, int expires)
		{
			_etag = etag;
			Timer.getTimer().schedule(this, expires*1000);
		}

		@Override
		public void run() 
		{
			removeState(_etag);
		}
	}	

	public class ExpirationSubscriptionTask extends TimerTask
	{
		String _id;
		Long _startDate = System.currentTimeMillis();
		int _expires;

		public ExpirationSubscriptionTask(String id, int expires)
		{
			_id = id;
			_expires = expires;
			Timer.getTimer().schedule(this, expires*1000);
		}

		public int getRemainingTime()
		{
			return (int) (_expires - (System.currentTimeMillis() - _startDate)/1000);
		}

		@Override
		public void run() 
		{
			removeSubscription(_id, Constants.TIMEOUT);
		}
	}	

	public void removeSubscription(String id) 
	{
		removeSubscription(id, null); 
	}
	
	//FIXME
	public void eraseSubscription(String id) 
	{
		synchronized (_subscriptions) 
		{
			TimedValue<Subscription,ExpirationSubscriptionTask> subscription = _subscriptions.get(id);
			subscription.closeTask();
			_subscriptions.remove(id);
		}
	}
	
	private void removeSubscription(String id, String reason) 
	{
		synchronized (_subscriptions) 
		{
			TimedValue<Subscription,ExpirationSubscriptionTask> subscription = _subscriptions.get(id);
			subscription.closeTask();
			subscription.getValue().setState(Subscription.State.TERMINATED);
			notifySubscriber(subscription.getValue(), 0, reason);
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
			State state = _states.values().iterator().next().getValue();
			return new Content(state.getContent(), PresenceEventPackage.PIDF);
		}
	}

	public String getContentType() 
	{
		return PresenceEventPackage.PIDF;
	}

	public Content getContent() 
	{
		if(_states.isEmpty())
		{
			return getNeutralContent();
		}
		else
		{
			return getCurrentContent();
		}
	}

	@Override
	public String toString()
	{
		String etags = "";
		synchronized (_states) 
		{
			Iterator<TimedValue<State, ExpirationPublishingTask>> it = _states.values().iterator();
			while(it.hasNext())
			{
				State state = it.next().getValue();
				etags = etags+"/"+state.getETag();
			}
		}
		return "{"+_uri+etags+"}";
	}
}
