package org.cipango.kaleo.presence.policy;

import java.util.EventObject;

import org.cipango.kaleo.event.Subscription;


public class PolicyEvent extends EventObject {

	private String _entity;
	private String _subscriberAor;
	private Subscription.State _previousState;
	private Subscription.State _newState;

	public PolicyEvent(String entity, String subscriberAor, Subscription.State previousState, Subscription.State newState) {
		super(entity);
		_entity = entity;
		_subscriberAor = subscriberAor;
		_previousState = previousState;
		_newState = newState;
	}

	public String getEntity()
	{
		return _entity;
	}

	public String getSubscriberAor()
	{
		return _subscriberAor;
	}

	public Subscription.State getPreviousState()
	{
		return _previousState;
	}

	public Subscription.State getNewState()
	{
		return _newState;
	}

	
}
