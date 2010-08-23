package org.cipango.server;

import javax.servlet.sip.Address;
import javax.servlet.sip.SipURI;

import org.cipango.server.SipRequest;
import org.cipango.sip.NameAddr;
import org.cipango.util.ReadOnlyAddress;

import junit.framework.TestCase;

public class SipMessageTest extends TestCase
{
	public void testProxyAddress() throws Exception
	{
		SipRequest request = new SipRequest();
		NameAddr address = new NameAddr("sip:alice@atlanta.com");
		Address readOnlyAddress = new ReadOnlyAddress(address);
		
		request.addAddressHeader("p-asserted-identity", readOnlyAddress, true);
		
		Address addr = request.getAddressHeader("p-asserted-identity");
		assertEquals("alice", ((SipURI) addr.getURI()).getUser());
	}
}
