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

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import junit.framework.TestCase;
import static org.cipango.client.TestUtil.*;

public class SipTestCase extends TestCase
{
		
	private CipangoClient _client;

	@Override
	protected void setUp() throws Exception
	{
		_client = new CipangoClient(5061);
		_client.setProxy("sip:192.168.2.10");
		_client.start();
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception
	{
		_client.stop();
		super.tearDown();
	}
	
	public void testMessage() throws Exception
	{
		SipServletRequest request = _client.createRequest("MESSAGE", "sip:alice@cipango.org", "sip:uas@cipango.org");
		request.send();
		
		SipServletResponse response = waitResponse(request);
		assertEquals(SipServletResponse.SC_OK, response.getStatus());
	}
	
	public void testCall() throws Exception
	{
		SipServletRequest request = _client.createRequest("INVITE", "sip:alice@cipango.org", "sip:uas@cipango.org");
		request.send();
		
		SipServletResponse response = waitResponse(request);
		assertEquals(SipServletResponse.SC_RINGING, response.getStatus());
		response = waitResponse(request);
		assertEquals(SipServletResponse.SC_OK, response.getStatus());
		response.createAck().send();
		Thread.sleep(100);
		
		request = request.getSession().createRequest("BYE");
		request.send();
		assertEquals(SipServletResponse.SC_OK, waitResponse(request).getStatus());
	}
		
}
