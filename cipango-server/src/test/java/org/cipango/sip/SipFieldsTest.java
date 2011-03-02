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
package org.cipango.sip;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.servlet.sip.Address;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.SipURI;

import org.eclipse.jetty.http.HttpFields;
import org.junit.Before;
import org.junit.Test;

public class SipFieldsTest
{
	private SipFields _fields;
	
	@Before
	public void setUp()
	{
		_fields = new SipFields();
	}

	@Test
	public void testString() throws Exception
	{
		_fields.setString("call-id", "foo");
		String callId = _fields.getString(SipHeaders.CALL_ID_BUFFER);
		assertEquals("foo", callId);
		
		_fields.addString("event", "presence");
		_fields.addString("EVENT", "reg");
		
		String event = _fields.getString(SipHeaders.EVENT_BUFFER);
		assertEquals("presence", event);
		
		Iterator<String> it = _fields.getValues(SipHeaders.EVENT_BUFFER);
		assertTrue(it.hasNext());
		assertEquals("presence", it.next());
		assertTrue(it.hasNext());
		assertEquals("reg", it.next());
		assertFalse(it.hasNext());
		
		_fields.addAddress("from", new NameAddr("sip:foo@bar.com"), true);
		assertEquals("<sip:foo@bar.com>", _fields.getString(SipHeaders.FROM_BUFFER));
		
		byte[] b = "foo".getBytes();
		_fields.set(SipHeaders.CALL_ID_BUFFER, new String(b));
		assertEquals("foo", _fields.getString("call-id"));
		
		_fields.addAddress("route", new NameAddr("sip:route2"), true);
		_fields.addAddress("ROUTE", new NameAddr("sip:route1"), true);
		_fields.addAddress("Route", new NameAddr("sip:route3"), false);
		
		it = _fields.getValues("Route");
		assertTrue(it.hasNext());
		assertEquals("<sip:route1>", it.next());
		assertTrue(it.hasNext());
		assertEquals("<sip:route2>", it.next());
		assertTrue(it.hasNext());
		assertEquals("<sip:route3>", it.next());
		
		assertFalse(it.hasNext());
		
		assertEquals("route1", ((SipURI) _fields.getAddress(SipHeaders.ROUTE_BUFFER).getURI()).getHost());
	}

	@Test
	public void testAddress() throws Exception
	{
		_fields.setAddress(SipHeaders.FROM_BUFFER, new NameAddr("sip:foo@bar.com"));
		
		assertEquals("<sip:foo@bar.com>", _fields.getString("from"));
		
		SipURI uri = (SipURI) _fields.getAddress("from").getURI();
		assertEquals("foo", uri.getUser());
		assertEquals("bar.com", uri.getHost());
		
		_fields.addAddress("route", new NameAddr("sip:route1"), false);
		_fields.addAddress(SipHeaders.ROUTE_BUFFER, new NameAddr("sip:route2"), false);
		
		Iterator<Address> it = _fields.getAddressValues("route");
		assertTrue(it.hasNext());
		assertEquals("route1", ((SipURI) it.next().getURI()).getHost());
		assertTrue(it.hasNext());
		assertEquals("route2", ((SipURI) it.next().getURI()).getHost());
		assertFalse(it.hasNext());
		
		_fields.removeFirst(SipHeaders.ROUTE_BUFFER);
		it = _fields.getAddressValues("route");
		assertTrue(it.hasNext());
		assertEquals("route2", ((SipURI) it.next().getURI()).getHost());
		assertFalse(it.hasNext());
		
		_fields.removeFirst(SipHeaders.ROUTE_BUFFER);
		it = _fields.getAddressValues("route");
		assertFalse(it.hasNext());		
	}

	@Test
	public void testLong()
	{
		_fields.setString("expires", "30");
		assertEquals(30, _fields.getLong("expires"));
	}

	@Test
	public void testParameters()
	{
		String s = ";  foo =   bar ; transport =    tcp";
		Map<String, String> parameters = new HashMap<String, String>();
		HttpFields.valueParameters(s, parameters);
		//System.out.println(parameters.get("transport"));
	}

	@Test
	public void testParameterable() throws Exception 
	{
		_fields.addParameterable(
				SipHeaders.CACHE.lookup("Call-Info"), 
				new ParameterableImpl("<sip:foo.org>;appearance-index=1;appearance-state=active"), 
				false);
		Parameterable p = _fields.getParameterable(SipHeaders.CACHE.lookup("Call-Info"));
		
		assertEquals("1", p.getParameter("appearance-index"));
		assertEquals("active", p.getParameter("appearance-state"));
		assertEquals("<sip:foo.org>", p.getValue());
		
		_fields.addAddress("Contact", new NameAddr("<sip:foo.org>;tag=1234"), false);
		p = _fields.getParameterable(SipHeaders.CACHE.lookup("contact"));
		assertEquals("1234", p.getParameter("tag"));
		
		Address address = _fields.getAddress(SipHeaders.CONTACT_BUFFER);
		assertEquals("foo.org", ((SipURI) address.getURI()).getHost());
		p = _fields.getParameterableValues("contact").next();
		assertEquals("1234", p.getParameter("tag"));
	}

	@Test
	public void testUnknown() throws Exception
	{
		_fields.addString("foo", "value;foo=bar");
		Parameterable p = _fields.getParameterable(SipHeaders.CACHE.lookup("foo"));
		assertEquals("bar", p.getParameter("foo"));
	}

	@Test
	public void testIterator() throws Exception
	{
		_fields.addString("foo", "1");
		_fields.addString("foo", "2");
		_fields.addString("foo", "3");
		
		ListIterator<String> it = _fields.getValues("foo");
		assertTrue(it.hasNext());
		assertEquals(0, it.nextIndex());
		assertEquals("1", it.next());
		assertTrue(it.hasNext());
		assertEquals("2", it.next());
		assertTrue(it.hasNext());
		assertEquals("3", it.next());
		assertEquals(3, it.nextIndex());
		
		try { it.next(); fail("no such element"); } catch (NoSuchElementException _) { }
		
		assertTrue(it.hasPrevious());
		assertEquals("3", it.previous());
		assertTrue(it.hasPrevious());
		assertEquals("2", it.previous());
		assertTrue(it.hasPrevious());
		assertEquals("1", it.previous());
		
		assertFalse(it.hasPrevious());
		assertEquals(0, it.nextIndex());
		assertTrue(it.hasNext());
		assertEquals("1", it.next());

		assertTrue(it.hasPrevious());
		assertEquals("1", it.previous());
		assertTrue(it.hasNext());
		assertEquals("1", it.next());
		
		assertEquals(0, it.previousIndex());
	}

	@Test
	public void testVia() throws Exception
	{
		_fields.addString("via", "SIP/2.0/TCP client.atlanta.example.com:5060;branch=z9hG4bK74bd5");
		assertEquals("z9hG4bK74bd5", _fields.getVia().getBranch());

		_fields.addVia(new Via("SIP/2.0/UDP client.atlanta.example.com:5060;branch=z9hG4bK43fs6"), true);
		assertEquals("z9hG4bK43fs6", _fields.getVia().getBranch());
	}

	@Test
	public void testRemove() throws Exception
	{
		_fields.addString("foo", "bar");
		_fields.addAddress(SipHeaders.FROM_BUFFER, new NameAddr("sip:foo@bar.com"), true);
		assertNotNull(_fields.getString("foo"));
		assertNotNull(_fields.getString("from"));
		_fields.remove("foo");
		_fields.remove("from");
		
		_fields.addAddress("route", new NameAddr("sip:route1@cipango.org"), false);
		_fields.addAddress("route", new NameAddr("sip:route2@cipango.org"), false);
		_fields.addAddress("route", new NameAddr("sip:route3@cipango.org"), false);
		_fields.removeFirst(SipHeaders.ROUTE_BUFFER);
		
		Iterator<Address> it = _fields.getAddressValues("route");
		assertEquals("route2", ((SipURI) _fields.getAddress("route").getURI()).getUser());
		//_fields.removeLast("route");
	}

	@Test
	public void testCopy() throws Exception
	{
		_fields.addAddress("route", new NameAddr("<sip:foo.org>;index=1"), true);
		_fields.addAddress("route", new NameAddr("<sip:foo.org>;index=2"), false);
		
		SipFields fields = new SipFields();
		fields.copy(_fields, SipHeaders.ROUTE_BUFFER);
		
		_fields.getAddress("route").setParameter("index", "0");
		assertEquals("0", _fields.getAddress("route").getParameter("index"));
		assertEquals("1", fields.getAddress("route").getParameter("index"));
	}
	
	/*
	@Test
	public void testClone() throws Exception
	{
		_sipFields.addAddress("route", new NameAddr("sip:route1"), false);
		_sipFields.addAddress(SipHeaders.ROUTE_BUFFER, new NameAddr("sip:route2"), false);
		
		Field route = _sipFields.getField(SipHeaders.ROUTE_BUFFER);
		assertNotNull(route);
		
		Field clone = route.clone();
		assertNotSame(route, clone);
		
		assertNotSame(route.getAddress(), clone.getAddress());
		assertEquals(route.getAddress(), clone.getAddress());
		
		route.set(new AddressValue("sip:route3"));
		assertEquals("sip:route3", route.getAddress().getURI().toString());
		assertEquals("sip:route1", clone.getAddress().getURI().toString());
	
		_sipFields.addString("foo", "bar");
		
		Field foo = _sipFields.getField(SipHeaders.CACHE.lookup("foo"));
		clone = foo.clone();
		
		assertEquals(foo.getString(), clone.getString());
		assertSame(foo.getString(), clone.getString());
		
		assertEquals(foo.getBuffer(), clone.getBuffer());
		assertSame(foo.getBuffer(), clone.getBuffer());
	}*/

	@Test
	public void testClone2() throws Exception
	{
		_fields.addString("foo", "bar");
		_fields.addString("foo", "bar2");
		_fields.addAddress("route", new NameAddr("sip:route"), true);
		_fields.addString("from", "sip:route");
		
		SipFields clone = _fields.clone();
		
		assertNotSame(_fields, clone);
		
		Iterator<String> it = clone.getValues("foo");
		assertTrue(it.hasNext());
		String s = it.next();
		assertEquals("bar", s);
		assertSame(_fields.getString("foo"), s);
		assertEquals("bar2", it.next());
		
		assertNotSame(_fields.getAddress("route"), clone.getAddress("route"));
		assertEquals(_fields.getAddress("route"), clone.getAddress("route"));
		
		assertSame(_fields.getString("from"), clone.getString("from"));
		assertNotSame(_fields.getAddress("from"), clone.getAddress("from"));
	}
}
