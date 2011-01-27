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

public class UserAgentTest extends TestCase
{
	private SipClient _client;
	
	@Override
	protected void setUp() throws Exception
	{
		_client = new SipClient(5060);
		_client.start();
	}
	
	@Override
	protected void tearDown() throws Exception
	{
		_client.stop();
	}
	
	public void testUA() throws Exception
	{
		UserAgent thomas = _client.createUserAgent("thomas22", "opensips.org");
		thomas.setCredentials("thomas22", "thomas22300");
		
		thomas.startRegistration();
		
		Thread.sleep(5000);
	}
	
	public void testTestUA() throws Exception
	{
		TestUserAgent test = _client.createTestUserAgent("test", "localhost");
		SipServletRequest request = test.createRequest("OPTIONS", "sip:opensips.org");
		request.send();
		
		SipServletResponse response = test.getResponse(request);
		assertNotNull(response);
		assertEquals(200, response.getStatus());
	}
}
