// ========================================================================
// Copyright 2008-2010 NEXCOM Systems
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.sip.SipApplicationSession;

import org.cipango.diameter.base.Common;
import org.cipango.diameter.base.Common.AuthSessionState;
import org.cipango.server.session.AppSession;
import org.cipango.server.session.scope.ScopedAppSession;

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
	
	private SipApplicationSession _appSession;
	
	private boolean _valid = true;
	
	private Map<String, Object> _attributes;
		
	public DiameterSession(SipApplicationSession appSession, String sessionId)
	{
		_sessionId = sessionId;
		_appSession = appSession;
	}
	
	public SipApplicationSession getApplicationSession()
	{
		if (_appSession instanceof AppSession)
			return new ScopedAppSession((AppSession) _appSession);
		return _appSession;
	}

	public void setApplicationId(ApplicationId appId)
	{
		_appId = appId;
	}
	
	public void setDestinationRealm(String destinationRealm)
	{
		_destinationRealm = destinationRealm;
	}
	
	/**
	 * Returns a new <code>DiameterRequest</code>.
	 * @param command the command of the new <code>DiameterRequest</code>.
	 * @param maintained if <code>true</code>, add the AVP Auth-Session-State with the value AuthSessionState.STATE_MAINTAINED.
	 * @return a new <code>DiameterRequest</code>.
	 * @throws java.lang.IllegalStateException if this <code>DiameterSession</code> has been invalidated.
	 * @see Common#AUTH_SESSION_STATE
	 * @see AuthSessionState#STATE_MAINTAINED
	 */
	public DiameterRequest createRequest(DiameterCommand command, boolean maintained)
	{
		checkValid();
		
		DiameterRequest request = new DiameterRequest(_node, command, _appId.getId(), _sessionId);
		request.getAVPs().add(Common.DESTINATION_REALM, _destinationRealm);
		if (_destinationHost != null)
			request.getAVPs().add(Common.DESTINATION_HOST, _destinationHost);
		
		request.getAVPs().add(_appId.getAVP());
		
		if (maintained)
			request.getAVPs().add(Common.AUTH_SESSION_STATE, AuthSessionState.STATE_MAINTAINED);
		
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

	/**
	 * Returns <code>true</code> if this <code>DiameterSession</code> is valid, <code>false</code>
	 * otherwise. The <code>DiameterSession</code> can be invalidated by calling the method
	 * invalidate() on it.
	 * 
	 * @return <code>true</code> if this <code>DiameterSession</code> is valid, <code>false</code>
	 *         otherwise.
	 */
	public boolean isValid()
	{
		return _valid;
	}
	
	/**
	 * Invalidates this session and unbinds any objects bound to it.
	 * 
	 * @throws java.lang.IllegalStateException if this method is called on an invalidated session
	 */
	public void invalidate()
	{
		checkValid();
		_valid = false;
		_node.getSessionManager().removeSession(this);
	}
	
	private void checkValid()
	{
		if (!_valid)
			throw new IllegalStateException("Session has been invalidated");
	}
	
	/**
	 * Returns the object bound with the specified name in this session, or null if no object is
	 * bound under the name.
	 * 
	 * @param name a string specifying the name of the object
	 * @return the object with the specified name
	 * @throws NullPointerException if the name is null.
	 * @throws IllegalStateException if session is invalidated
	 */
	public Object getAttribute(String name) 
	{
		checkValid();
		if (name == null)
			throw new NullPointerException("Attribute name is null");
		if (_attributes == null)
			return null;
		return _attributes.get(name);
	}

	/**
	 * Returns an Enumeration over the <code>String</code> objects containing the names of all the
	 * objects bound to this session.
	 * 
	 * @return Returns an Enumeration over the <code>String</code> objects containing the names of
	 *         all the objects bound to this session.
	 * @throws IllegalStateException if session is invalidated
	 */
	public Enumeration<String> getAttributeNames() 
	{
		checkValid();
		List<String> names;
		if (_attributes == null)
			names = Collections.emptyList();
		else
			names = new ArrayList<String>(_attributes.keySet());
		return Collections.enumeration(names);
	}
	
	/**
	 * Removes the object bound with the specified name from this session. If the session does not have an object bound with the specified name, this method does nothing. 
	 * @param name the name of the object to remove from this session 
	 * @throws IllegalStateException if session is invalidated
	 */
	public void removeAttribute(String name) 
	{
		checkValid();
		
		if (_attributes == null)
			return;
		
		 _attributes.remove(name);
	}

	/**
	 * Binds an object to this session, using the name specified. If an object of the same name is
	 * already bound to the session, the object is replaced.
	 * 
	 * @param name the name to which the object is bound
	 * @param value the object to be bound
	 * @throws IllegalStateException if session is invalidated
	 * @throws NullPointerException on <code>null</code> <code>name</code> or <code>value</code>.
	 */
	public void setAttribute(String name, Object value) 
	{
		checkValid();
		
		if (name == null || value == null)
			throw new NullPointerException("name or value is null");
		
		if (_attributes == null)
			_attributes = new HashMap<String, Object>(3);
		
		_attributes.put(name, value);	
	}
}