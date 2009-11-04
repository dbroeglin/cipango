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

import org.cipango.diameter.base.Base;
import org.cipango.diameter.base.Base.AuthSessionState;

/**
 * Point-to-point Diameter relationship. 
 */
public class DiameterSession 
{
	private Node _node;
	
	private ApplicationId _appId;
	private String _sessionId;
	
	private String _destinationRealm;
	private String _destinationHost;
	
	public DiameterSession(String sessionId)
	{
		_sessionId = sessionId;
	}

	public void setApplicationId(ApplicationId appId)
	{
		_appId = appId;
	}
	
	public void setDestinationRealm(String destinationRealm)
	{
		_destinationRealm = destinationRealm;
	}
	
	public DiameterRequest createRequest(DiameterCommand command, boolean maintained)
	{
		DiameterRequest request = new DiameterRequest(_node, command, _appId.getId(), _sessionId);
		request.getAVPs().add(Base.DESTINATION_REALM, _destinationRealm);
		if (_destinationHost != null)
			request.getAVPs().add(Base.DESTINATION_HOST, _destinationHost);
		
		request.getAVPs().add(_appId.getAVP());
		
		if (maintained)
			request.getAVPs().add(Base.AUTH_SESSION_STATE, AuthSessionState.STATE_MAINTAINED);
		
		return request;
	}
	
	public String getId()
	{
		return _sessionId;
	}
	
	public ApplicationId getApplicationId()
	{
		return _appId;
	}
	
	public String getDestinationRealm()
	{
		return _destinationRealm;
	}
	
	public String getDestinationHost()
	{
		return _destinationHost;
	}
	
	public void setDestinationHost(String destinationHost)
	{
		_destinationHost = destinationHost;
	}
	
	public void setNode(Node node)
	{
		_node = node;
	}
}