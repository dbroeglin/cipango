package org.cipango.kaleo.event;

import javax.servlet.sip.SipSession;

public class Subscription 
{
	private Resource _resource;
	private SipSession _session;
	private State _state = new State();
	private String _id;
	
	public class State {
		
		public static final int ACTIVE = 0;
		public static final int PENDING = 1;
		public static final int TERMINATED = 2;
		
		private int _value;
		
		public int getValue() 
		{
			return _value;
		}
		
		public void setValue(int value) 
		{
			_value = value;
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
}
