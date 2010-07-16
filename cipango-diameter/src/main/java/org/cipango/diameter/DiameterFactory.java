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

import javax.servlet.sip.SipApplicationSession;

/**
 * Factory interface for Diameter abstractions. 
 * An instance this class is available to applications through the <code>org.cipango.diameter.DiameterFactory</code>
 * attribute of {@link javax.servlet.ServletContext}
 */
public class DiameterFactory 
{
	private Node _node;
	
	public DiameterRequest createRequest(SipApplicationSession appSession, ApplicationId id, DiameterCommand command, String destinationRealm)
	{
		String suffix = "appid-" + appSession.getId();
		DiameterSession session = _node.getSessionManager().createSession(suffix);
		return null;
	}
	
	public DiameterRequest createRequest(ApplicationId id, DiameterCommand command, String destinationRealm)
	{
		return createRequest(id, command, destinationRealm, null);
	}
	
	public DiameterRequest createRequest(ApplicationId id, DiameterCommand command, String destinationRealm, String destinationHost)
	{
		DiameterSession session = _node.getSessionManager().createSession();
		session.setNode(_node);
		session.setApplicationId(id);
		session.setDestinationRealm(destinationRealm);
		session.setDestinationHost(destinationHost);
		return session.createRequest(command, false);
	}
	
	public void setNode(Node node)
	{
		_node = node;
	}
	
	protected Node getNode()
	{
		return _node;
	}
}
