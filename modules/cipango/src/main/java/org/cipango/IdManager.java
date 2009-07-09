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

package org.cipango;

import java.net.InetAddress;

import org.cipango.util.ID;

public class IdManager
{
	private static String __magicCookie = "m9uT4oX";
	
	private String _localhost;
	
	public IdManager()
	{
		try 
    	{
    		_localhost = InetAddress.getLocalHost().getHostName();
    	} 
    	catch (Exception _)
    	{
    		_localhost = "localhost";
    	}
	}
	
	public String newAppSessionId()
	{
		return ID.newID(4);
	}
	
	public String newSessionId()
	{
		return ID.newID(4);
	}
	
	public String newCallId()
	{
		return ID.newID(4) + '@' + _localhost;
	}
	
	public String newCallId(String callId)
    {
    	if (callId.startsWith(__magicCookie))
    	{
    		int i = callId.indexOf('*');
    		
    		StringBuilder sb = new StringBuilder(callId.length());
    		sb.append(__magicCookie);
    		sb.append('-');
    		sb.append(ID.newID(2));
    		sb.append('*');
    		sb.append(callId.substring(i+1));
    		return sb.toString();
    	}
    	else
    	{
    		StringBuilder sb = new StringBuilder(callId.length() + _localhost.length() + 14);
    		sb.append(__magicCookie);
    		sb.append('-');
    		sb.append(ID.newID(2));
    		sb.append('*');
    		for (int i = 0; i < callId.length(); i++)
    		{
    			char c = callId.charAt(i);
    			if (c == '@')
    				sb.append('%');
    			else if (c == '%')
    				sb.append("%%");
    			else
    				sb.append(c);
    		}
    		sb.append('@');
    		sb.append(_localhost);
    		return sb.toString();
    	}		
    }
	
	 public String getCallId(String callId)
	 {
    	if (!callId.startsWith(__magicCookie))
    		return callId;
    	
    	int i = callId.indexOf('*', __magicCookie.length() + 5);
    	int j = callId.indexOf('@', i+1);
    	
    	StringBuilder sb = null;
    	boolean escape = false;
    	
    	for (int k = i+1; k < j; k++)
    	{
    		char c = callId.charAt(k);
    		if (c == '%')
    		{
    			if (sb == null)
    			{
    				sb = new StringBuilder(j-i);
    				sb.append(callId.substring(i+1, k));
    			}
    			if (escape)
    				sb.append('%');
    			escape = ! escape;
    		}
    		else
    		{
    			if (escape)
    			{
    				sb.append('@');
    				escape = false;
    			}
    			if (sb != null)
    				sb.append(c);
    		}
    	}
    	if (sb != null)
    		return sb.toString();
    	return callId.substring(i+1, j);
    }
}
