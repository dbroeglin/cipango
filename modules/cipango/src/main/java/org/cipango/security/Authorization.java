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

package org.cipango.security;

import java.util.HashMap;
import java.util.StringTokenizer;

import org.cipango.util.DigestAuthenticator;
import org.cipango.util.ID;
import org.mortbay.io.BufferCache;
import org.mortbay.io.BufferCache.CachedBuffer;

public class Authorization
{

	public static BufferCache CACHE = new BufferCache();
	
	public static final String
		USERNAME = "username",
		REALM = "realm",
		NONCE = "nonce",
		DIGEST_URI = "uri",
		RESPONSE = "response",
		ALGORITHM = "algorithm",
		CNONCE = "cnonce",
		OPAQUE = "opaque",
		QOP = "qop",
		NONCE_COUNT = "nc";
	
	public static final int
		USERNAME_ORDINAL = 0,
		REALM_ORDINAL = 1,
		NONCE_ORDINAL = 2,
		DIGEST_URI_ORDINAL = 3,
		RESPONSE_ORDINAL = 4,
		ALGORITHM_ORDINAL = 5,
		CNONCE_ORDINAL = 6,
		OPAQUE_ORDINAL = 7,
		QOP_ORDINAL = 8,
		NONCE_COUNT_ORDINAL = 9;
	
	public static final CachedBuffer
		USERNAME_BUFFER = CACHE.add(USERNAME, USERNAME_ORDINAL),
		REALM_BUFFER = CACHE.add(REALM, REALM_ORDINAL),
		NONCE_BUFFER = CACHE.add(NONCE, NONCE_ORDINAL),
		DIGEST_URI_BUFFER = CACHE.add(DIGEST_URI, DIGEST_URI_ORDINAL),
		RESPONSE_BUFFER = CACHE.add(RESPONSE, RESPONSE_ORDINAL),
		ALGORITHM_BUFFER = CACHE.add(ALGORITHM, ALGORITHM_ORDINAL),
		CNONCE_BUFFER = CACHE.add(CNONCE, CNONCE_ORDINAL),
		OPAQUE_BUFFER = CACHE.add(OPAQUE, OPAQUE_ORDINAL),
		QOP_BUFFER = CACHE.add(QOP, QOP_ORDINAL),
		NONCE_COUNT_BUFFER = CACHE.add(NONCE_COUNT, NONCE_COUNT_ORDINAL);
	

	private String[] _params = new String[10];
	private String _scheme;
	private HashMap<String, String> _unknwonParams;

	public Authorization(Authenticate authenticate, String username, String password, String uri, String method)
	{
		_params[USERNAME_ORDINAL] = username;
		_params[REALM_ORDINAL] = authenticate.getRealm();
		_params[NONCE_ORDINAL] = authenticate.getNonce();
		_params[ALGORITHM_ORDINAL] = authenticate.getAlgorithm();
		_scheme = authenticate.getScheme();
		if (authenticate.getQop() != null)
		{
			_params[CNONCE_ORDINAL] = ID.newCNonce();
			StringTokenizer st = new StringTokenizer(authenticate.getQop(), ",");
			boolean first = true;
			while (st.hasMoreTokens())
			{
				String token = st.nextToken().trim();
				if (first || token.equalsIgnoreCase(DigestAuthenticator.AUTH))
				{
					_params[QOP_ORDINAL] = token;
					first = false;
				}
			}
			_params[NONCE_COUNT_ORDINAL] = "00000001";
		}
		_params[OPAQUE_ORDINAL] = authenticate.getOpaque();
		_params[DIGEST_URI_ORDINAL] = uri;
		_params[RESPONSE_ORDINAL] = getCalculatedResponse(password, method);
	}
	
	public Authorization(String auth)
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
	
	public String getCalculatedResponse(String password, String method)
	{
		return new DigestAuthenticator().calculateResponse(this, password, method);
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
	
	public String getUsername()
	{
		return _params[USERNAME_ORDINAL];
	}
	
	public String getUri()
	{
		return _params[DIGEST_URI_ORDINAL];
	}
	
	public String getNonceCount()
	{
		return _params[NONCE_COUNT_ORDINAL];
	}
		
	public String getRealm()
	{
		return _params[REALM_ORDINAL];
	}
	
	public String getResponse()
	{
		return _params[RESPONSE_ORDINAL];
	}
	public void setRealm(String realm)
	{
		_params[REALM_ORDINAL] = realm;
	}

	public String getNonce()
	{
		return _params[NONCE_ORDINAL];
	}
	
	public String getCNonce()
	{
		return _params[CNONCE_ORDINAL];
	}
	
	public void setNonce(String nonce)
	{
		_params[NONCE_ORDINAL] = nonce;
	}

	public String getOpaque()
	{
		return _params[OPAQUE_ORDINAL];
	}
	
	public void setOpaque(String opaque)
	{
		_params[OPAQUE_ORDINAL] = opaque;
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
		return sb.toString();
	}

}
