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

import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.TelURL;

// TODO extends from URIImpl
public class TelURLImpl implements TelURL 
{	
	private String _uri;
	private String _scheme;
	private String _number;
	private HashMap<String, String> _params = new HashMap<String, String>();
	
	public static final String PHONE_CONTEXT = "phone-context";
	
	public TelURLImpl(String uri) throws ServletParseException 
	{
		_uri = uri;
		parse();
	}
	
	private void parse() throws ServletParseException 
	{
		int indexScheme = _uri.indexOf(':');
		if (indexScheme < 0) 
			throw new ServletParseException("Missing TelURL scheme in [" + _uri + "]");
		
		_scheme = _uri.substring(0, indexScheme);
		if (!"tel".equals(_scheme) && !"fax".equals(_scheme)) 
		{
			throw new ServletParseException("Invalid TelURL scheme [" + _scheme + "] in [" + _uri + "]");
		}
		int indexParam = _uri.indexOf(';', indexScheme);
		if (indexParam < 0) 
		{
			_number = _uri.substring(indexScheme + 1);
		} 
		else 
		{
			_number = _uri.substring(indexScheme + 1, indexParam);
			if (!SipGrammar.__phoneDigits.containsAll(getPhoneNumber())) 
				throw new ServletParseException("Invalid phone number [" + _number
						+ "] in URI [" + _uri + "]");
			String sParams = _uri.substring(indexParam + 1);
			parseParams(sParams);
		}
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
				value = param.substring(index + 1).trim();
			}
			if (!SipGrammar.__param.containsAll(name)) 
			{
				throw new ServletParseException("Invalid parameter name [" 
						+ name + "] in [" + _uri + "]");
			}
			if (!SipGrammar.__param.containsAll(value)) 
			{
				throw new ServletParseException("Invalid parameter value [" 
						+ value + "] in [" + _uri + "]");
			}			
			_params.put(SipGrammar.unescape(name.toLowerCase()), SipGrammar.unescape(value));
		}
	}

	public boolean isSipURI() 
	{
		return false;
	}
	
	public boolean isGlobal() 
	{
		return _number.startsWith("+");
	}
	
	public String getPhoneNumber() 
	{
		if (_number == null) return null;
		
		if (isGlobal()) 
			return _number.substring(1);
		else 
			return _number;
	}
	
	public void setPhoneNumber(String number)
	{
		if (!number.startsWith("+"))
			throw new IllegalArgumentException("Not a global number: " + number);
		String n = number.startsWith("+") ? number.substring(1) : number;
		if (!SipGrammar.__phoneDigits.containsAll(n)) 
			throw new IllegalArgumentException("Invalid phone number [" + number + "]");
		_number = number;
	}
	
	public void setPhoneNumber(String number, String phoneContext)
	{
		if (number.startsWith("+"))
			throw new IllegalArgumentException("Not a local number: " + number);
		if (!SipGrammar.__phoneDigits.containsAll(number)) 
			throw new IllegalArgumentException("Invalid phone number [" + number + "]");
		_number = number;
		setParameter(PHONE_CONTEXT, phoneContext);
	}
	
	public String getPhoneContext()
	{
		return getParameter(PHONE_CONTEXT);
	}
	
	public String getScheme() 
	{
		return _scheme;
	}
	
	public String getParameter(String name) 
	{
		return (String) _params.get(name.toLowerCase());
	}
	
	public void removeParameter(String name)
	{
		_params.remove(name);	
	}
	
	public void setParameter(String name, String value)
	{
		if (name == null || value == null)
			throw new NullPointerException("Null value or name");
		_params.put(name, value);
	}
	
	public synchronized Iterator<String> getParameterNames() 
	{
		return _params.keySet().iterator();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public TelURL clone() 
	{
		TelURLImpl other;
		try 
		{
			other = (TelURLImpl) super.clone();
		} 
		catch (CloneNotSupportedException _) 
		{
			throw new RuntimeException("!cloneable " + this);
		}
		other._params = (HashMap<String, String>) _params.clone();
		return other;
	}
	
	public String toString() 
	{
		StringBuffer sb = new StringBuffer();
		sb.append(_scheme);
		sb.append(':');
		sb.append(_number);
		
		Iterator<String> it = getParameterNames();
		while (it.hasNext()) 
		{
			String name = (String) it.next();
			String value = getParameter(name);
			sb.append(';');
			sb.append(SipGrammar.escape(name, SipGrammar.__param));
			if (value != null && value.length() > 0) 
			{
				sb.append('=');
				sb.append(SipGrammar.escape(value, SipGrammar.__param));
			}
		}
		return sb.toString();
	}
	
	protected String removeVisualChar(String number)
	{
		return number.replaceAll("[-\\.\\(\\)]", "");
	}
	
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof TelURL)) 
			return false;
		
		TelURL other = (TelURL) o;
		
		if (!_scheme.equals(other.getScheme()))
			return false;
		
		if (!removeVisualChar(getPhoneNumber()).equals(removeVisualChar(other.getPhoneNumber())))
			return false;
		
		if (isGlobal() != other.isGlobal())
			return false;
				
		for (String key : _params.keySet())
		{
			String otherValue = other.getParameter(key); 
			if (otherValue != null && !getParameter(key).equalsIgnoreCase(otherValue))
				return false;
		}
		return true;
	}
}
