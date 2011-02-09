package org.cipango.client.test;

import javax.servlet.sip.SipURI;

import org.cipango.client.SipClient;

public class SipTestClient extends SipClient
{
	public SipTestClient(int port) 
	{
		super(port);
	}
	
	public SipTester createTestUserAgent(String user, String host)
	{
		return createTestUserAgent(createSipURI(user, host));
	}
	
	public SipTester createTestUserAgent(SipURI uri)
	{
		SipTester agent = new SipTester(uri);
		addAgent(agent);
		return agent;
	}
}
