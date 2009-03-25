// ========================================================================
// Copyright 2007-2008 NEXCOM Systems
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

package org.cipango;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.sip.Address;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.SipURI;

import junit.framework.TestCase;

import org.cipango.sip.SipParser;
import org.cipango.sip.AbstractSipConnector.EventHandler;
import org.mortbay.io.ByteArrayBuffer;

public class SipRequestTest extends TestCase
{

	public void testPushRoute() throws Exception
	{		
		SipRequest request = (SipRequest) getMessage(INVITE);
		assertFalse(request.isNextHopStrictRouting());
		
		request.pushRoute(new NameAddr("<sip:strictRouting@nexcom.fr>"));
		assertEquals("sip:strictRouting@nexcom.fr", request.getRequestURI().toString());
		assertEquals("<sips:ss2.biloxi.example.com>", request.getTopRoute().toString());
		assertTrue(request.isNextHopStrictRouting());
		
		request.pushRoute(new NameAddr("<sip:strictRouting-2@nexcom.fr>"));
		assertEquals("sip:strictRouting-2@nexcom.fr", request.getRequestURI().toString());
		ListIterator<Address> it = request.getAddressHeaders("route");
		List<String> expected = new ArrayList<String>();
		expected.add("<sip:strictRouting@nexcom.fr>");
		expected.add("<sips:ss2.biloxi.example.com>");
		assertAddress(expected, it);
		assertTrue(request.isNextHopStrictRouting());
		
		request.pushRoute(new NameAddr("<sip:looseRouting@nexcom.fr;lr>"));
		assertEquals("sips:ss2.biloxi.example.com", request.getRequestURI().toString());
		it = request.getAddressHeaders("route");
		expected = new ArrayList<String>();
		expected.add("<sip:looseRouting@nexcom.fr;lr>");
		expected.add("<sip:strictRouting-2@nexcom.fr>");
		expected.add("<sip:strictRouting@nexcom.fr>");
		assertAddress(expected, it);
		assertFalse(request.isNextHopStrictRouting());
		
		request.pushRoute(new NameAddr("<sip:looseRouting-2@nexcom.fr;lr>"));
		assertEquals("sips:ss2.biloxi.example.com", request.getRequestURI().toString());
		it = request.getAddressHeaders("route");
		expected = new ArrayList<String>();
		expected.add("<sip:looseRouting-2@nexcom.fr;lr>");
		expected.add("<sip:looseRouting@nexcom.fr;lr>");
		expected.add("<sip:strictRouting-2@nexcom.fr>");
		expected.add("<sip:strictRouting@nexcom.fr>");
		assertAddress(expected, it);
		assertFalse(request.isNextHopStrictRouting());
		
		/*-----------*/
		request = (SipRequest) getMessage(INVITE);
		request.pushRoute(new NameAddr("<sip:looseRouting@nexcom.fr;lr>"));
		assertEquals("sips:ss2.biloxi.example.com", request.getRequestURI().toString());
		assertEquals("<sip:looseRouting@nexcom.fr;lr>", request.getTopRoute().toString());
		assertFalse(request.isNextHopStrictRouting());
		
		request.pushRoute(new NameAddr("<sip:strictRouting@nexcom.fr>"));
		assertEquals("sip:strictRouting@nexcom.fr", request.getRequestURI().toString());
		it = request.getAddressHeaders("route");
		expected = new ArrayList<String>();
		expected.add("<sip:looseRouting@nexcom.fr;lr>");
		expected.add("<sips:ss2.biloxi.example.com>");
		assertAddress(expected, it);
		assertTrue(request.isNextHopStrictRouting());
		
		request.pushRoute(new NameAddr("<sip:strictRouting-2@nexcom.fr>"));
		assertEquals("sip:strictRouting-2@nexcom.fr", request.getRequestURI().toString());
		it = request.getAddressHeaders("route");
		expected = new ArrayList<String>();
		expected.add("<sip:strictRouting@nexcom.fr>");
		expected.add("<sip:looseRouting@nexcom.fr;lr>");
		expected.add("<sips:ss2.biloxi.example.com>");
		assertAddress(expected, it);
		assertTrue(request.isNextHopStrictRouting());
	
	}
	
	public void testContact() throws Exception
	{
		SipRequest request = (SipRequest) getMessage(INVITE);
		Address contact = request.getAddressHeader("Contact");
		assertEquals("<sip:127.0.0.1:5060;transport=TCP>", contact.toString());
		contact.setDisplayName("Bob");
		assertEquals("Bob", contact.getDisplayName());
		SipURI uri = (SipURI) contact.getURI();
		uri.setUser("bob");
		assertEquals("bob", uri.getUser());
		try { uri.setHost("bad"); fail(); } catch (IllegalStateException e) {}
		try { uri.setLrParam(true); fail(); } catch (IllegalStateException e) {}
		try { uri.setMAddrParam("bad"); fail(); } catch (IllegalStateException e) {}
		try { uri.setMethodParam("Bad"); fail(); } catch (IllegalStateException e) {}
		try { uri.setTTLParam(2); fail(); } catch (IllegalStateException e) {}
		try { uri.setParameter("lr", ""); fail(); } catch (IllegalStateException e) {}
		try { uri.removeParameter("Maddr"); fail(); } catch (IllegalStateException e) {}
		uri.setParameter("transport", "UDP");
		assertEquals("UDP", uri.getParameter("transport"));
		assertEquals("Bob <sip:bob@127.0.0.1:5060;transport=UDP>", contact.toString());
		
		// Full read-only on committed
		request.setCommitted(true);
		contact = request.getAddressHeader("Contact");
		uri = (SipURI) contact.getURI();
		try { contact.setDisplayName("bad"); fail(); } catch (IllegalStateException e) {}
		try { uri.setUser("bad"); fail(); } catch (IllegalStateException e) {}
		
		// Full writable on REGISTER
		request = (SipRequest) getMessage(REGISTER);
		contact = request.getAddressHeader("Contact");
		uri = (SipURI) contact.getURI();
		contact.setDisplayName("Bob");
		uri.setHost("nexcom.fr");
		uri.setPort(5062);
		uri.removeParameter("transport");
		uri.setUser("bob");
		assertEquals("Bob <sip:bob@nexcom.fr:5062>", contact.toString());		
	}
	
	
	protected void assertAddress(List<String> expected, ListIterator<Address> it)
	{
		while (it.hasNext())
		{
			int index = it.nextIndex();
			Address address = (Address) it.next();
			assertEquals(expected.get(index), address.toString());
		}
		assertEquals("Not same number of address", expected.size(), it.nextIndex());
	}
	
	public SipMessage getMessage(String msg) throws Exception
	{
		EventHandler handler = new EventHandler();
		SipParser parser = new SipParser(new ByteArrayBuffer(msg.getBytes()), handler);
		parser.parse();
		return handler.getMessage();
	}
	
	public void testGetParameterable() throws Exception
	{
		SipRequest request = (SipRequest) getMessage(INVITE);
		Parameterable p = request.getParameterableHeader("from");
		assertEquals("Bob <sips:bob@biloxi.example.com>", p.getValue());
		assertEquals("a73kszlfl", p.getParameter("tag"));
		
		// FIXME p = request.getParameterableHeader("Via");
		
		p = request.getParameterableHeader("Accept");
		assertEquals("application/sdp", p.getValue());
		assertEquals("1", p.getParameter("level"));
		
		ListIterator<? extends Parameterable> it = request.getParameterableHeaders("Accept");
		while (it.hasNext())
		{
			int index = it.nextIndex();
			p = (Parameterable) it.next();
			switch (index)
			{
			case 0:
				assertEquals("application/sdp", p.getValue());
				assertEquals("1", p.getParameter("level"));
				break;
			case 1:
				assertEquals("application/x-private", p.getValue());
				assertFalse(p.getParameterNames().hasNext());
				break;
			case 2:
				assertEquals("text/html", p.getValue());
				assertFalse(p.getParameterNames().hasNext());
				break;
			default:
				fail("Too much parameterable");
				break;
			}
		}
		assertEquals(3, it.nextIndex());
	}
	
	public void testMultipleLineHeaders() throws Exception
	{
		EventHandler handler = new EventHandler();
		InputStream is = getClass().getResourceAsStream("/org/cipango/MultipleLineRequest.txt");
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int read;
		while ((read = is.read(buffer)) != -1)
		{
			os.write(buffer, 0, read);
		}
		SipParser parser = new SipParser(new ByteArrayBuffer(os.toByteArray()), handler);
		parser.parse();
		
		SipMessage message = handler.getMessage();
		String toString = message.toString();
		System.out.println(message);
		
		assertEquals(1, count(toString, "Accept:"));
		assertEquals(3, count(toString, "Via:"));
		assertEquals(2, count(toString, "UnknownHeader:"));
		assertEquals(2, count(toString, "Proxy-Authenticate:"));
		
		ListIterator<String> it = message.getHeaders("UnknownHeader");
		while (it.hasNext())
		{
			int index = it.nextIndex();
			String value = (String) it.next();
			if (index == 0)
				assertEquals("valWith,Comma", value);
			else
				assertEquals("val2", value);
		}
	}
	
	private int count(String string, String token)
	{
		int i = 0;
		int index = -1;
		while ((index = string.indexOf(token, index + 1)) != -1)
			i++;
		return i;
	}
	
	private static final String INVITE = "INVITE sips:ss2.biloxi.example.com SIP/2.0\r\n"
		+ "Via: SIP/2.0/TLS client.biloxi.example.com:5061;branch=z9hG4bKnashds7\r\n"
		+ "Max-Forwards: 70\r\n"
		+ "From: Bob <sips:bob@biloxi.example.com>;tag=a73kszlfl\r\n"
		+ "To: Alice <sips:alice@biloxi.example.com>\r\n"
		+ "Call-ID: 1j9FpLxk3uxtm8tn@biloxi.example.com\r\n"
		+ "CSeq: 1 INVITE\r\n"
		+ "Expires: 0\r\n"
		+ "Contact: <sip:127.0.0.1:5060;transport=TCP>\r\n"
		+ "Accept: application/sdp;level=1, application/x-private, text/html\r\n"
		+ "Content-Length: 0\r\n\r\n";
	
	private static final String REGISTER = "REGISTER sip:nexcom.fr SIP/2.0\r\n"
		+ "Via: SIP/2.0/UDP 192.168.1.2:5061;branch=z9hG4bKnashds7\r\n"
		+ "Max-Forwards: 70\r\n"
		+ "From: Bob <sips:bob@nexcom.fr>;tag=a73kszlfl\r\n"
		+ "To: Bob <sips:bob@nexcom.fr>\r\n"
		+ "Call-ID: 1j9FpLxk3uxtm8tn@192.168.1.2\r\n"
		+ "CSeq: 1 REGISTER\r\n"
		+ "Expires: 0\r\n"
		+ "Contact: <sip:127.0.0.1:5060;transport=TCP>\r\n"
		+ "Content-Length: 0\r\n\r\n";
}
