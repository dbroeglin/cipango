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

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.cipango.diameter.base.Base;
import org.mortbay.util.LazyList;

public abstract class DiameterMessage
{
	protected int _command;
	protected AVPList _avps = new AVPList();

	protected int _applicationId;
	protected int _hopByHopId;
	protected int _endToEndId;

	protected Node _node;
	protected DiameterConnection _connection;
	
	protected DiameterSession _session;
	
	private Map<String, Object> _attributes;
	
	public DiameterMessage()
	{
	}
	
	public DiameterMessage(Node node, int appId, int command, int endToEndId, int hopByHopId, String sessionId)
	{
		_node = node;
		_applicationId = appId;
		_command = command;
		_hopByHopId = hopByHopId;
		_endToEndId = endToEndId;
		if (sessionId != null)
			_avps.add(AVP.ofString(Base.SESSION_ID, sessionId));
		_avps.add(AVP.ofString(Base.ORIGIN_HOST, node.getIdentity()));
		_avps.add(AVP.ofString(Base.ORIGIN_REALM, node.getRealm()));
	}
	
	public DiameterMessage(DiameterMessage message)
	{
		this(message._node, 
				message._applicationId, 
				message._command, 
				message._endToEndId, 
				message._hopByHopId, 
				message.getSessionId());
	}
	
	public Node getNode()
	{
		return _node;
	}
	
	public void setNode(Node node)
	{
		_node = node;
	}
	
	public void setConnection(DiameterConnection connection)
	{
		_connection = connection;
	}
	
	public DiameterConnection getConnection()
	{
		return _connection;
	}

	public int getApplicationId()
	{
		return _applicationId;
	}
	
	public void setApplicationId(int applicationId)
	{
		_applicationId = applicationId;
	}
	
	public int getHopByHopId()
	{
		return _hopByHopId;
	}
	
	public void setHopByHopId(int hopByHopId)
	{
		_hopByHopId = hopByHopId;
	}
	
	public int getEndToEndId()
	{
		return _endToEndId;
	}
	
	public void setEndToEndId(int endToEndId)
	{
		_endToEndId = endToEndId;
	}
	
	public void setCommand(int command)
	{
		_command = command;
	}
	
	public int getCommand()
	{
		return _command;
	}
	
	public AVP get(int index)
	{
		return _avps.get(index);
	}
	
	public String getOriginHost()
	{
		return _avps.getString(Base.ORIGIN_HOST);
	}
	
	public String getOriginRealm()
	{
		return _avps.getString(Base.ORIGIN_REALM);
	}
	
	public String getSessionId()
	{
		return _avps.getString(Base.SESSION_ID);
	}
	
	public int size()
	{
		return _avps.size();
	}
	
	public AVPList getAVPs()
	{
		return _avps;
	}
	
	public void add(AVP avp)
	{
		_avps.add(avp);
	}
	
	public AVP getAVP(int code)
	{
		return getAVP(Base.IETF_VENDOR_ID, code);
	}
	
	public AVP getAVP(int vendorId, int code)
	{
		for (int i = 0; i < _avps.size(); i++)
		{
			AVP avp = _avps.get(i);
			if (avp.getVendorId() == vendorId && avp.getCode() == code)
				return avp;
		}
		return null;
	}
	
	public Iterator<AVP> getAVPs(int code)
	{
		return _avps.getAVPs(code);
	}
	
	public Iterator<AVP> getAVPs(int vendorId, int code)
	{
		return _avps.getAVPs(vendorId, code);
	}
	
	public DiameterSession getSession()
	{
		return _session;
	}
	
	public void setSession(DiameterSession session)
	{
		_session = session;
	}
	
	public abstract boolean isRequest();
	public abstract void send() throws IOException;
	
	public String toString()
	{
		return "[" + _applicationId + "," + _endToEndId + "," + _hopByHopId + "] " + _command;
	}
	
	public Object getAttribute(String name) 
	{
		if (_attributes != null) 
			return _attributes.get(name);
		return null;
	}
	
	public void removeAttribute(String name)
	{
		if (_attributes == null)
			return;
		_attributes.remove(name);
	}
	

	@SuppressWarnings("unchecked")
	public Enumeration<String> getAttributeNames() 
	{
		if (_attributes != null) 
			return Collections.enumeration(_attributes.keySet());
		
		return Collections.enumeration(Collections.EMPTY_LIST);
	}
	
	public void setAttribute(String name, Object o) 
	{
		if (o == null || name == null) 
			throw new NullPointerException("name or value is null");
		
		if (_attributes == null) 
			_attributes = new HashMap<String, Object>();

		_attributes.put(name, o);
	}
}
