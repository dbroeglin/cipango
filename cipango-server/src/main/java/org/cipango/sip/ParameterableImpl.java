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

package org.cipango.sip;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import javax.servlet.sip.Parameterable;
import javax.servlet.sip.ServletParseException;

import org.cipango.sip.SipHeaders.HeaderInfo;

import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;

public class ParameterableImpl implements Parameterable, Serializable
{
	private String _value;
	private HashMap<String, String> _parameters = new HashMap<String, String>();

	public ParameterableImpl(String s) throws ServletParseException
	{
		String parameters = null;
		
		int indexParams = s.indexOf(';');
		if (indexParams > -1) 
		{
			_value = s.substring(0, indexParams).trim();
			parameters = s.substring(indexParams + 1);
		} 
		else 
			_value = s;
		
		if (parameters != null)
			parseParams(parameters);
	}
	
	private void parseParams(String sParams) throws ServletParseException 
	{
		StringTokenizer st = new StringTokenizer(sParams, ";");
		while (st.hasMoreTokens()) 
		{
			String param = st.nextToken();
			String name;
			String value;
			int index = param.indexOf('=');
			
			if (index < 0) 
			{
				name  = param.trim();
				value = "";
			} 
			else 
			{
				name  = param.substring(0, index).trim();
				value = SipGrammar.unquote(param.substring(index + 1).trim());
			}
			if (!SipGrammar.isToken(name)) 
				throw new ServletParseException("Invalid parameter name [" 
						+ name + "] in [" + sParams + "]");
			_parameters.put(name.toLowerCase(), value);
		}
	}
		
	public String getParameter(String name)
	{
		return _parameters.get(name.toLowerCase());
	}

	public Iterator<String> getParameterNames()
	{
		return _parameters.keySet().iterator();
	}

	public Set<Entry<String, String>> getParameters()
	{
		return _parameters.entrySet();
	}

	public String getValue()
	{
		return _value;
	}

	public void removeParameter(String name)
	{
		_parameters.remove(name.toLowerCase());
	}

	public void setParameter(String name, String value)
	{
		_parameters.put(name.toLowerCase(), value);
	}

	public void setValue(String value)
	{
		_value = value;
	}
	
	public int getType()
	{
		return HeaderInfo.PARAMETERABLE;
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer(64);
		
		sb.append(_value);
		Iterator<String> it = getParameterNames();
		while (it.hasNext()) 
		{
			String name = (String) it.next();
			String value = getParameter(name);
			sb.append(';');
			sb.append(name);
			if (value.length() > 0) 
			{
				if (SipGrammar.isToken(value)) 
				{
					sb.append('=');
					sb.append(value);
				} 
				else 
				{
					sb.append('=');
					sb.append('"');
					sb.append(SipGrammar.escapeQuoted(value));
					sb.append('"');
				}
			}
		}
		return sb.toString();
	}
	
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof Parameterable)) 
			return false;
		Parameterable p = (Parameterable) o;
		
		if (!_value.equals(p.getValue()))
			return false;
		
		for (String key : _parameters.keySet())
		{
			String otherValue = p.getParameter(key); 
			if (otherValue != null && !getParameter(key).equalsIgnoreCase(otherValue))
				return false;
		}
		return true;
		
	}
	
	public Buffer toBuffer()
	{
		return new ByteArrayBuffer(toString());
	}
	
	public Object clone()
	{
		return this;
	}
}
