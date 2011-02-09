package org.cipango.client;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.cipango.client.test.SipTestClient;
import org.cipango.client.test.SipTester;

import junit.framework.TestCase;

public class TestUserAgentTest extends TestCase
{
	private SipTestClient _client;
		
	@Override
	protected void setUp() throws Exception
	{
		_client = new SipTestClient(5060);
		_client.start();
	}
	
	@Override
	protected void tearDown() throws Exception
	{
		_client.stop();
	}
	
	public void testTestUA() throws Exception
	{
		SipTester test = _client.createTestUserAgent("test", "localhost");
		SipServletRequest request = test.createRequest("OPTIONS", "sip:opensips.org");
		request.send();
		
		SipServletResponse response = test.getResponse(request);
		assertNotNull(response);
		assertEquals(200, response.getStatus());
	}
}
