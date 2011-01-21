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
		SipClient client = new SipClient(5060);
		client.start();
		
		UserAgent alice = client.newUserAgent("alice", "localhost");
		SipServletRequest register = alice.createRequest("REGISTER", "sip:192.168.2.207");
		register.send();
		
		SipServletResponse response = alice.getResponse(register, 10000);
		assertNotNull(response);
		assertEquals(503, response.getStatus());
		
		client.stop();
	}
}
