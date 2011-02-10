package org.cipango.client;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.cipango.client.test.SipTester;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class SipTesterTest 
{
	@After
	public void reset() throws Exception
	{
		SipTester.reset(); 
	}
	
	@Test
	public void testTestUA() throws Exception
	{
		SipTester alice = SipTester.create("alice");
		SipTester bob = SipTester.create("bob");
		
		SipServletRequest request = alice.createRequest("OPTIONS", bob);
		request.send();
		
		SipServletResponse response = alice.getResponse(request);
		assertNotNull(response);
		assertEquals(200, response.getStatus());
	}
}
