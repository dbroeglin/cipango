package org.cipango.kaleo.event;

import java.util.TimerTask;

import javax.servlet.sip.SipSession;

import org.cipango.kaleo.util.Timer;

public class Subscription 
{
	private Resource _resource;
	private SipSession _session;
	private State _state = State.ACTIVE;
	private String _id;
	private ExpirationSubscriptionTask _task;
	
	public enum State
	{
		ACTIVE("active"), 
		PENDING("pending"), 
		TERMINATED("terminated");

		private String _name;

		private State(String name)
		{
			_name = name;
		}

		public String getName()
		{
			return _name;
		}
	}
	
	public Subscription(Resource resource, SipSession session) 
	{
		_resource = resource;
		_session = session;
		_id = session.getId();
	}
	
	public String getId()
	{
		return _id;
	}
	
	public Resource getResource() 
	{
		return _resource;
	}
	
	public SipSession getSession() 
	{
		return _session;
	}
	
	public State getState() 
	{
		return _state;
	}
	
	public void setState(State state) 
	{
		_state = state;
	}
	
	public int getRemainingTime()
	{
		return _task.getRemainingTime();
	}
	
	public void closeTask() 
	{
		if(_task != null)
			_task.cancel();
	}

	public void startTask(int expires) 
	{
		if(_task != null)
			_task.cancel();
		_task = new ExpirationSubscriptionTask(expires);
	}
	
	class ExpirationSubscriptionTask extends TimerTask
	{
		Long _startDate = System.currentTimeMillis();
		int _expires;

		public ExpirationSubscriptionTask(int expires)
		{
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
			_resource.removeSubscription(_id, Reason.TIMEOUT);
		}
	}
}
