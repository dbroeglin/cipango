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
package org.cipango.client;

import javax.servlet.sip.SipSession.State;

import org.cipango.client.labs.SipSession;
import org.cipango.client.labs.UA;

public class SipCall
{
	private Identity _identity;
	private SipSession _session;
	
	public SipCall(Identity identity)
	{
		_identity = identity;
	}
	
	public void makeCall(String to)
	{
		//SipRequest request = _client.createRequest(SipRequest.INVITE, _uri, to);
		//_session = request.getSession();
		//request.send();
	}
	
	public void makeCall(UA to)
	{
		
	}
	
	
	public void reInvite()
	{
	
	}
	
	public void cancel()
	{
	
	}
	
	public void end()
	{
		
	}
	
	public State getState()
	{
		return null;
	}
}
