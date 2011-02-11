package org.cipango.client;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.cipango.client.test.SipTest;
import org.cipango.client.test.SipTester;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

public class SipTesterTest 
{
	@After
	public void reset() throws Exception
	{
		SipTest.reset(); 
	}
	
	@Test
	public void testOptions() throws Exception
	{
		SipTester alice = SipTest.create("alice");
		SipTester bob = SipTest.create("bob");
		
		SipServletRequest request = alice.createRequest("OPTIONS", bob);
		request.send();
		
		SipServletResponse response = alice.getResponse(request);
		
		assertNotNull(response);
		assertEquals(200, response.getStatus());
	}
	
	@Test
	public void testCall() throws Exception
	{
		SipTester alice = SipTest.create("alice");
		SipTester bob = SipTest.create("bob");
		
		SipServletRequest invite = alice.createRequest("INVITE", bob);
		invite.send();
		
		SipServletResponse response = alice.getResponse(invite);
		
		assertNotNull(response);
		assertEquals(200, response.getStatus());
		
		response.createAck().send();
		
		SipServletRequest bye = alice.createRequest(invite.getSession(), "BYE");
		bye.send();
		
		response = alice.getResponse(bye);
		
		assertNotNull(response);
		assertEquals(200, response.getStatus());
	}
	
}
