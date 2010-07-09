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

package org.cipango.sip.security;

import java.util.HashMap;
import java.util.Iterator;

import org.cipango.sip.LazyParsingException;

import org.eclipse.jetty.io.BufferCache;
import org.eclipse.jetty.io.BufferCache.CachedBuffer;

/**
 * Parser for WWW-Authenticate or Proxy-Authenticate headers
 * @author nicolas
 *
 */
public class Authenticate
{

	public static BufferCache CACHE = new BufferCache();
	
	public static final String
		REALM = "realm",
		DOMAIN = "domain",
		NONCE = "nonce",
		OPAQUE = "opaque",
		STALE = "stale",
		ALGORITHM = "algorithm",
		QOP = "qop";
	
	public static final int
		REALM_ORDINAL = 0,
		DOMAIN_ORDINAL = 1,
		NONCE_ORDINAL = 2,
		OPAQUE_ORDINAL = 3,
		STALE_ORDINAL = 4,
		ALGORITHM_ORDINAL = 5,
		QOP_ORDINAL = 6;
	
	public static final CachedBuffer
		REALM_BUFFER = CACHE.add(REALM, REALM_ORDINAL),
		DOMAIN_BUFFER = CACHE.add(DOMAIN, DOMAIN_ORDINAL),
		NONCE_BUFFER = CACHE.add(NONCE, NONCE_ORDINAL),
		OPAQUE_BUFFER = CACHE.add(OPAQUE, OPAQUE_ORDINAL),
		STALE_BUFFER = CACHE.add(STALE, STALE_ORDINAL),
		ALGORITHM_BUFFER = CACHE.add(ALGORITHM, ALGORITHM_ORDINAL),
		QOP_BUFFER = CACHE.add(QOP, QOP_ORDINAL);
	

	private String[] _params = new String[7];
	private String _scheme;
	private HashMap<String, String> _unknwonParams;

	public Authenticate(String auth) throws LazyParsingException
	{
		int beginIndex = auth.indexOf(' ');
		int endIndex;
		_scheme = auth.substring(0, beginIndex).trim();
		while (beginIndex > 0)
		{
			endIndex = auth.indexOf('=', beginIndex);
			String name = auth.substring(beginIndex, endIndex).trim();
			if (auth.charAt(endIndex + 1) == '"')
			{
				beginIndex = endIndex + 2;
				endIndex = auth.indexOf('"', beginIndex);
			}
			else
			{
				beginIndex = endIndex + 1;
				endIndex = auth.indexOf(',', beginIndex);
				if (endIndex == -1)
					endIndex = auth.length(); 
			}

			String value = auth.substring(beginIndex, endIndex);	
			setParameter(name, value);
			beginIndex = auth.indexOf(',', endIndex) + 1;
		}
	}
	
	public String getParameter(String name)
	{
		CachedBuffer buffer = CACHE.get(name);
		if (buffer == null)
		{
			if (_unknwonParams == null)
				return null;
			return _unknwonParams.get(name);
		}
		return _params[buffer.getOrdinal()];
	}
	
	public void setParameter(String name, String value)
	{	
		CachedBuffer buffer = CACHE.get(name);
		if (buffer == null)
		{
			if (_unknwonParams == null)
				_unknwonParams = new HashMap<String, String>();
			_unknwonParams.put(name, value);
		}
		else
			_params[buffer.getOrdinal()] = value;
	}
	
	public String getScheme()
	{
		return _scheme;
	}
	
	public String getParameter(CachedBuffer buffer)
	{
		return _params[CACHE.getOrdinal(buffer)];
	}
	
	public String getRealm()
	{
		return _params[REALM_ORDINAL];
	}

	public String getDomain()
	{
		return _params[DOMAIN_ORDINAL];
	}

	public String getNonce()
	{
		return _params[NONCE_ORDINAL];
	}

	public String getOpaque()
	{
		return _params[OPAQUE_ORDINAL];
	}
	
	public boolean isStale()
	{
		return "true".equalsIgnoreCase(_params[STALE_ORDINAL]);
	}

	public String getAlgorithm()
	{
		return _params[ALGORITHM_ORDINAL];
	}

	public String getQop()
	{
		return _params[QOP_ORDINAL];
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(getScheme()).append(' ');
		boolean first = true;
		for (int i = 0; i < _params.length; i++)
		{
			if (_params[i] != null)
			{
				if (!first)
					sb.append(',');
				else
					first = false;
				sb.append(CACHE.get(i).buffer().toString());
				sb.append("=\"").append(_params[i]).append('"');
			}
		}
		if (_unknwonParams != null)
		{
			Iterator<String> it = _unknwonParams.keySet().iterator();
			while (it.hasNext())
			{
				String key = it.next();
				sb.append(',');
				sb.append(key).append("=\"").append(_unknwonParams.get(key)).append('"');
			}
		}
		return sb.toString();
	}

}
