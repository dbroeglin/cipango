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

import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.URI;

import org.cipango.sip.SipHeaders.HeaderInfo;

public class NameAddr implements Address, Serializable
{
    static final long serialVersionUID = -6854832441383110449L;
    
	private String _nameAddr;
	private String _displayName; 
	private URI _uri;
	private HashMap<String, String> _params = new HashMap<String, String>();
	private boolean _wildcard;

	public NameAddr(String address) throws ServletParseException 
	{
		_nameAddr = address;
		parse(_nameAddr, true); 
	}
	
	public NameAddr(URI uri) 
	{
		this(uri, null);
	}
	
	public NameAddr(URI uri, String displayName) 
	{
		_uri = uri;
		_displayName = displayName;
	}
	
	public int getType()
	{
		return HeaderInfo.ADDRESS;
	}
	
	private void parse(String address, boolean parseParam) throws ServletParseException 
	{
		String sURI = null;
		String sParams = null;
		address = address.trim();
		if ("*".equals(address)) 
		{
			_wildcard = true;
			return;
		}
		
		if (address.startsWith("\"")) 
		{
			int rQuote = indexRQuote(address, 1);
			if (rQuote == -1) 
				throw new ServletParseException("Missing terminating \" in [" + address + "]");
			
			String s = address.substring(0, rQuote + 1);
			_displayName = SipGrammar.unquote(s);
			int laqIndex = address.indexOf('<', rQuote + 1);
			if (laqIndex == -1) 
				throw new ServletParseException("Missing < in address [" + address + "]");
			
			int raqIndex = address.indexOf('>');
			if (raqIndex == -1) 
				throw new ServletParseException("Missing > in address [" + address + "]");
			
			sURI = address.substring(laqIndex + 1, raqIndex);
			int indexParams = address.indexOf(';', raqIndex + 1);
			if (indexParams > -1) 
				sParams = address.substring(indexParams + 1);
			
		} 
		else 
		{
			int indexLAQuote = address.indexOf('<');
			if (indexLAQuote > -1)
			{
				if (indexLAQuote > 0) 
				{
					String s = address.substring(0, indexLAQuote).trim();
					if (s.length() > 0) 
					{
						if (!SipGrammar.isTokens(s)) 
							throw new ServletParseException("Not token or LWS char in [" 
									+ s + "] in address [" + address + "]");
						_displayName = s;
					}
				}
				int indexRAQuote = address.indexOf('>', indexLAQuote);
				if (indexRAQuote == -1) 
					throw new ServletParseException("Missing > in address [" + address + "]");
				
				sURI = address.substring(indexLAQuote + 1, indexRAQuote);
				int indexParams = address.indexOf(';', indexRAQuote + 1);
				if (indexParams > -1) 
					sParams = address.substring(indexParams + 1);
				
			}
			else
			{
				int indexParams = address.indexOf(';');
				if (indexParams > -1) 
				{
					sURI = address.substring(0, indexParams);
					sParams = address.substring(indexParams + 1);
				} 
				else 
					sURI = address;
			}
		}
		_uri = URIFactory.parseURI(sURI);
		if (sParams != null && parseParam) 
			parseParams(sParams);
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
						+ name + "] in [" + _uri + "]");
			_params.put(name.toLowerCase(), value);
		}
	}
	
	private int indexRQuote(String s, int start) 
	{	
		for (int index = start;; index++) 
		{
			if (index >= s.length()) 
				return -1;
		
			if (s.charAt(index) == '"' && s.charAt(index - 1) != '\\') 
				return index;
		}
	}
	
	public void setDisplayName(String displayName) 
	{
		_displayName = displayName;
	}
	
	public String getDisplayName() 
	{
		return _displayName;
	}
	
	public Iterator<String> getParameterNames() 
	{
		return _params.keySet().iterator();
	}
	
	public URI getURI() 
	{
		return _uri;
	}
	
	public void setURI(URI uri) 
	{
		if (uri == null)
			throw new NullPointerException("null uri");
		_uri = uri;
	}
	
	public String getParameter(String name) 
	{
		return (String) _params.get(name.toLowerCase());
	}
	
	public void setParameter(String name, String value) 
	{
		if (value == null)
			removeParameter(name);
		else
			_params.put(name.toLowerCase(), value);
	}
	
	public void removeParameter(String name) 
	{
		_params.remove(name.toLowerCase());
	}
	
	public boolean isWildcard() {
		
		return _wildcard;
	}
	
	public float getQ() 
	{
		String q = getParameter("q");
		if (q != null) 
		{
			try 
			{
				return Float.parseFloat(q);
			} 
			catch (NumberFormatException _) 
			{
			}
		}
		return -1.0f;
	}
	
	public void setQ(float q) 
	{
		if (q == -1.0f) 
			_params.remove("q");
		else 
		{
			if (q < 0 || q > 1.0f) 
				throw new IllegalArgumentException("Invalid q value:" + q);
			setParameter("q", String.valueOf(q));
		}
	}
	
	public int getExpires() 
	{
		String expires = getParameter("expires");
		if (expires != null) 
		{
			try 
			{
				return Integer.parseInt(expires);
			} 
			catch (NumberFormatException _) { }
		}
		return -1;
	}
	
	public void setExpires(int seconds) 
	{
		if (seconds < 0) 
			removeParameter("expires");
		else 
			setParameter("expires", Integer.toString(seconds));
	}
	
	public String toString() 
	{
		StringBuffer sb = getValueBuffer();
		if (isWildcard())
			return sb.toString();
		
		Iterator<String> it = getParameterNames();
		while (it.hasNext()) 
		{
			String name = it.next();
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

	@Override
	public Object clone() 
	{
		NameAddr clone;
		try 
		{
			clone = (NameAddr) super.clone();
		} 
		catch (CloneNotSupportedException _) 
		{
			throw new RuntimeException("!cloneable " + this);
		}
		if (_params != null) 
			clone._params = (HashMap<String, String>) _params.clone();
		
		if (_uri != null) 
			clone._uri = (URI) _uri.clone();
		
		return clone;
	}
	
	@Override
	public boolean equals(Object o) 
	{
		if (o == null || !(o instanceof Address)) 
			return false;
		
		Address other = (Address) o;
		
		if (!_uri.equals(other.getURI()))
			return false;
				
		for (String key : _params.keySet())
		{
			String otherValue = other.getParameter(key); 
			if (otherValue != null && !getParameter(key).equals(otherValue))
				return false;
		}
		return true;
	}

	public Set<Entry<String, String>> getParameters()
	{
		return _params.entrySet();
	}

	public String getValue()
	{
		return getValueBuffer().toString();
	}
		
	public StringBuffer getValueBuffer()
	{
		StringBuffer sb = new StringBuffer(64);
		if (isWildcard()) 
			return sb.append("*");
		
		if (_displayName != null) 
		{
			if (SipGrammar.isTokens(_displayName)) 
			{
				sb.append(_displayName);
			} 
			else 
			{
				sb.append('"');
				sb.append(SipGrammar.escapeQuoted(_displayName));
				sb.append('"');
			}
			sb.append(' ');
		}
		sb.append('<');
		sb.append(_uri.toString());
		sb.append('>');
		return sb;
	}

	public void setValue(String value)
	{
		if (value == null)
			throw new NullPointerException("Null value");
		
		try
		{
			_displayName = null;
			_wildcard = false;
			_nameAddr = null;
			parse(value, false);
		}
		catch (ServletParseException e)
		{
			throw new LazyParsingException(e);
		}
	}
}
