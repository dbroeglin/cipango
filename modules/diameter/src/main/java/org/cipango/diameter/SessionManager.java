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

package org.cipango.diameter;

import java.util.HashMap;
import java.util.Map;

public class SessionManager 
{
	private long _startTimestamp = ((System.currentTimeMillis() / 1000) & 0xffffffffl);
	private long _id;
	
	private Node _node;
	private Map<String, DiameterSession> _sessions = new HashMap<String, DiameterSession>();
	
	public DiameterSession createSession(String appSessionId)
	{
		String sessionId = newSessionId() + ";" + appSessionId;
		return new DiameterSession(sessionId);
	}
	
	public DiameterSession createSession()
	{
		return new DiameterSession(newSessionId());
	}
	
	public DiameterSession getSession(String id)
	{
		synchronized(_sessions)
		{
			return _sessions.get(id);
		}
	}
	
	protected synchronized String newSessionId()
	{
		return _node.getIdentity() + ";" + _startTimestamp + ";" + (++_id);
	}
	
	public void setNode(Node node)
	{
		_node = node;
	}
}
