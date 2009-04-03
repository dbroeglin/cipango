package org.cipango.kaleo.event;

import javax.servlet.sip.SipSession;

public class Subscription 
{
	private Resource _resource;
	private SipSession _session;
	private State _state = State.ACTIVE;
	private String _id;
	
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
}
