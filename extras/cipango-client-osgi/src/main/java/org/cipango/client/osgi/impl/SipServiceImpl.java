// ========================================================================
// Copyright 2011 NEXCOM Systems
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
package org.cipango.client.osgi.impl;

import java.io.IOException;

import org.cipango.client.UserAgent;
import org.cipango.client.osgi.SipService;

public class SipServiceImpl implements SipService
{
	private UserAgent _userAgent;
	
	public void register(int expires) throws IOException
	{
		if (expires == 0)
			_userAgent.startRegistration();
		else
			_userAgent.startRegistration();
	}

	public UserAgent getUserAgent()
	{
		return _userAgent;
	}

	public void setUserAgent(UserAgent userAgent)
	{
		_userAgent = userAgent;
	}

}
