package org.cipango.kaleo.event;

import org.cipango.kaleo.presence.Presentity.ExpirationSubscriptionTask;

public class SubscribeTimedValue implements TimedValue<Subscription, ExpirationSubscriptionTask>
{
	private Subscription _subscription;

	private ExpirationSubscriptionTask _task;

	public SubscribeTimedValue(Subscription subscription, ExpirationSubscriptionTask task)
	{
		_subscription = subscription;
		_task = task;
	}

	public void closeTask() 
	{
		if(_task != null)
			_task.cancel();
	}

	public Subscription getValue() 
	{
		return _subscription;
	}
	
	public ExpirationSubscriptionTask getTask() 
	{
		return _task;
	}

	public void resetTask(ExpirationSubscriptionTask task) 
	{
		if(_task != null)
			_task.cancel();
		_task = task;
	}
}
