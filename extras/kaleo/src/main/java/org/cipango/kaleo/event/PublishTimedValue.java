package org.cipango.kaleo.event;

import org.cipango.kaleo.presence.Presentity.ExpirationPublishingTask;

public class PublishTimedValue implements TimedValue<State, ExpirationPublishingTask>
{
	private State _state;

	private ExpirationPublishingTask _task;

	public PublishTimedValue(State state, ExpirationPublishingTask task)
	{
		_state = state;
		_task = task;
	}

	public void closeTask() 
	{
		if(_task != null)
			_task.cancel();
	}

	public State getValue() 
	{
		return _state;
	}

	public ExpirationPublishingTask getTask() 
	{
		return _task;
	}

	public void resetTask(ExpirationPublishingTask task) 
	{
		if(_task != null)
			_task.cancel();
		_task = task;
	}
}
