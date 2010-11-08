// ========================================================================
// Copyright 2007-2010 NEXCOM Systems
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
package org.cipango.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.sip.Address;

import org.cipango.server.B2bHelper;
import org.cipango.sip.NameAddr;

import junit.framework.TestCase;

public class B2bHelperTest extends TestCase
{
	public void testMergeContact() throws Exception
	{ 
		Address destination = new NameAddr("<sip:127.0.0.1:5060>");
		B2bHelper.mergeContact("Bob <sip:bob@127.0.0.22:5070;transport=UDP;ttl=1>;p=2", destination);
		assertEquals("Bob <sip:bob@127.0.0.1:5060;transport=UDP>;p=2", destination.toString());
	}
	
	public void testAddMapHeaders() throws Exception
	{ 
		SipRequest request = (SipRequest) SipRequestTest.getMessage(SipRequestTest.INVITE);
		request.setInitial(true);
		String tag = request.getFrom().getParameter("tag");
		Map<String, List<String>> headerMap = new HashMap<String, List<String>>();
		headerMap.put("X-test", Arrays.asList("val1", "val2"));
		headerMap.put("From", Arrays.asList("\"Alice\" <sip:alice@cipango.voip>;tag=123"));
		headerMap.put("To", Arrays.asList("\"Bob\" <sip:bob@cipango.voip>;tag=123"));
		headerMap.put("Route", Arrays.asList("<sip:cipango.voip;lr>"));
		headerMap.put("Accept", Arrays.asList("text/plain"));
		headerMap.put("m", Arrays.asList("Alice <sip:alice@127.0.0.22:5070;transport=UDP>;p=2")); // Contact compact header
		new B2bHelper().addHeaders(request, headerMap);
		
		System.out.println(request);
		
		Iterator<String> it = request.getHeaders("X-test");
		assertEquals("val1", it.next());
		assertEquals("val2", it.next());
		assertFalse(it.hasNext());
		
		assertEquals("Alice <sip:alice@cipango.voip>;tag=" + tag, request.getFrom().toString());
		assertEquals("Bob <sip:bob@cipango.voip>", request.getTo().toString());
		
		it = request.getHeaders("Accept");
		assertEquals("text/plain", it.next());
		assertFalse(it.hasNext());
		
		it = request.getHeaders("Route");
		assertEquals("<sip:cipango.voip;lr>", it.next());
		assertFalse(it.hasNext());
		
		assertEquals("Alice <sip:alice@127.0.0.1:5060;transport=UDP>;p=2", request.getHeader("Contact"));
		
		headerMap.clear();
		headerMap.put("Via", Arrays.asList("SIP/2.0/UDP invalid:5061;branch=z9hG4bdes"));
		try
		{
			new B2bHelper().addHeaders(request, headerMap);
			fail("System header added");
		}
		catch (IllegalArgumentException e) {}
	}
}
