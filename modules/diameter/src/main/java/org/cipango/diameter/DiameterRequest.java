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

import org.cipango.diameter.base.Base;

public class DiameterRequest extends DiameterMessage
{
	private static int __hopId;
	private static int __endId;
	
	private static synchronized int nextHopId() { return __hopId++; }
	private static synchronized int nextEndId() { return __endId++; }

	public DiameterRequest() {}
	
	public DiameterRequest(Node node, int command, int appId, String sessionId)
	{	
		super(node, appId, command, nextEndId(), nextHopId(), sessionId);
	}
	
	public boolean isRequest()
	{
		return true;
	}
	
	public String getDestinationRealm()
	{
		return _avps.getString(Base.DESTINATION_REALM);
	}
	
	public String getDestinationHost()
	{
		return _avps.getString(Base.DESTINATION_HOST);
	}
	
	public DiameterAnswer createAnswer(int resultCode)
	{
		return createAnswer(Base.IETF_VENDOR_ID, resultCode);
	}
	
	public DiameterAnswer createAnswer(int vendorId, int resultCode)
	{
		return new DiameterAnswer(this, vendorId, resultCode);
	}
	
	public void send() throws IOException
	{
		getNode().send(this);
	}
}