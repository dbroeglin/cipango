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

import javax.servlet.sip.SipSession;

public class Subscription 
{
	public enum State
	{
		ACTIVE("active"), PENDING("pending"), TERMINATED("terminated");
		
		private String _name;
		
		private State(String name) { _name = name; }
		public String getName() { return _name; }
	}
	
	public enum Reason 
	{
		DEACTIVATED("deactivated"), PROBATION("probation"), REJECTED("rejected"),
		TIMEOUT("timeout"), GIVEUP("giveup"), NORESOURCE("noresource");
		
		private String _name;
		
		private Reason(String name) { _name = name; }
		public String getName() { return _name; }
	}
	
	private Resource _resource;
	private SipSession _session;
	private State _state = State.ACTIVE;
	
	public Subscription(Resource resource, SipSession session) 
	{
		_resource = resource;
		_session = session;
	}
	
	public String getId()
	{
		return _session.getId();
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
	
	public void start()
	{
		for (ResourceListener listener : _resource.getListeners())
		{
			listener.subscriptionStarted(this);
		}
	}
	
	public String toString()
	{
		return _resource.getUri() + "/" + getId();
	}
}
