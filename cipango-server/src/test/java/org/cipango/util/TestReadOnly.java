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

package org.cipango.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.servlet.sip.Address;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.TelURL;
import javax.servlet.sip.URI;

import junit.framework.TestCase;


import org.cipango.sip.NameAddr;
import org.cipango.sip.ParameterableImpl;
import org.cipango.sip.SipURIImpl;
import org.cipango.sip.TelURLImpl;
import org.cipango.sip.URIImpl;

public class TestReadOnly extends TestCase
{
	public void testAddress() throws Exception 
	{
		Address orig = new NameAddr("\"Hello World\" <sip:foo@bar.com;transport=tcp>;tag=12345");
		Address readOnly = new ReadOnlyAddress((Address) orig.clone());
		
		assertEquals(orig, readOnly);
		try { readOnly.setParameter("foo", "bar"); fail();} catch (IllegalStateException e) {}
		try { readOnly.removeParameter("tag"); fail();} catch (IllegalStateException e) {}
		try { readOnly.setDisplayName(""); fail();} catch (IllegalStateException e) {}
		try { ((SipURI) readOnly.getURI()).removeParameter("transport"); fail();} catch (IllegalStateException e) {}
		try { ((SipURI) readOnly.getURI()).setUser("foo2"); fail();} catch (IllegalStateException e) {}
		
		assertEquals(orig, readOnly);
		assertEquals(readOnly, orig);
		
		try { readOnly.setURI(new TelURLImpl("tel:+3398648;user=phone")); fail();} catch (IllegalStateException e) {}
		
		assertEquals(orig, readOnly);
		assertEquals(readOnly, orig);
		assertEquals(orig.toString(), readOnly.toString());
		Address clone = (Address) readOnly.clone();
		clone.setParameter("a", "b");
		testSerializable(readOnly);
	}
	
	public void testParameterable() throws Exception 
	{
		Parameterable orig = new ParameterableImpl("\"Hello World\" <sip:foo@bar.com;transport=tcp>;tag=12345");
		Parameterable readOnly = new ReadOnlyParameterable((Parameterable) orig.clone());
		
		assertEquals(orig, readOnly);
		try { readOnly.setParameter("foo", "bar"); fail();} catch (IllegalStateException e) {}
		try { readOnly.removeParameter("tag"); fail();} catch (IllegalStateException e) {}
		try { readOnly.setValue(""); fail();} catch (IllegalStateException e) {}
		
		assertEquals(orig, readOnly);
		assertEquals(readOnly, orig);
		assertEquals(orig.toString(), readOnly.toString());
		Parameterable clone = (Parameterable) readOnly.clone();
		clone.setParameter("a", "b");
		testSerializable(readOnly);
	}
	
	public void testSipUri() throws Exception 
	{
		SipURI orig = new SipURIImpl("sip:foo@bar.com;transport=tcp?to=sip:bob%40biloxi.com");
		SipURI readOnly = new ReadOnlySipURI((SipURI) orig.clone());
		
		assertEquals(orig, readOnly);
		try { readOnly.setParameter("foo", "bar"); fail();} catch (IllegalStateException e) {}
		try { readOnly.removeParameter("transport"); fail();} catch (IllegalStateException e) {}	
		try { readOnly.setHeader("subject", "toto"); fail();} catch (IllegalStateException e) {}
		try { readOnly.removeHeader("to"); fail();} catch (IllegalStateException e) {}
		assertEquals(orig, readOnly);
		assertEquals(readOnly, orig);
		assertEquals(orig.toString(), readOnly.toString());
		URI clone = (URI) readOnly.clone();
		clone.setParameter("a", "b");
		testSerializable(readOnly);
	}
	
	public void testTelUrl() throws Exception 
	{
		TelURL orig = new TelURLImpl("tel:+3398648;user=phone");
		TelURL readOnly = new ReadOnlyTelURL((TelURL) orig.clone());
		
		assertEquals(orig, readOnly);
		try { readOnly.setParameter("foo", "bar"); fail();} catch (IllegalStateException e) {}
		try { readOnly.removeParameter("user");	 fail();} catch (IllegalStateException e) {}
		assertEquals(orig, readOnly);
		assertEquals(readOnly, orig);
		assertEquals(orig.toString(), readOnly.toString());
		URI clone = (URI) readOnly.clone();
		clone.setParameter("a", "b");
		testSerializable(readOnly);
	}
	
	public void testURI() throws Exception 
	{
		URI orig = new URIImpl("http://www.nexcom.fr;user=me");
		URI readOnly = new ReadOnlyURI((URI) orig.clone());
		
		assertEquals(orig, readOnly);
		try { readOnly.setParameter("foo", "bar"); fail();} catch (IllegalStateException e) {}
		try { readOnly.removeParameter("user");	 fail();} catch (IllegalStateException e) {}
		assertEquals(orig, readOnly);
		assertEquals(readOnly, orig);
		assertEquals(orig.toString(), readOnly.toString());
		URI clone = (URI) readOnly.clone();
		clone.setParameter("a", "b");
		testSerializable(readOnly);
	}
	
	public void testSerializable(Object o) throws Exception 
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(baos);
		os.writeObject(o);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream is = new ObjectInputStream(bais);
		assertEquals(o, is.readObject());
	}
}
