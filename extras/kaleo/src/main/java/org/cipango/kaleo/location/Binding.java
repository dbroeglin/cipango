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

package org.cipango.kaleo.location;

import javax.servlet.sip.URI;

public class Binding 
{
	private String _callId;
	private int _cseq;
	private URI _contact;
	private long _expirationTime;
	
	public Binding(URI contact, String callId, int cseq, long expirationTime)
	{
		update(contact, callId, cseq, expirationTime);
	}
	
	public void update(URI contact, String callId, int cseq, long expirationTime)
	{
		_contact = contact;
		_callId = callId;
		_cseq = cseq;
		_expirationTime = expirationTime;
	}
	
	public URI getContact()
	{
		return _contact;
	}
	
	public long getExpirationTime()
	{
		return _expirationTime;
	}
	
	public String getCallId()
	{
		return _callId;
	}
	
	public int getCSeq()
	{
		return _cseq;
	}
	
	public String toString()
	{
		return _contact.toString();
	}
}
