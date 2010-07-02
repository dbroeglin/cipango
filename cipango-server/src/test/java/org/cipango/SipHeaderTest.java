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

import javax.servlet.sip.Address;
import javax.servlet.sip.SipURI;

import org.cipango.util.ReadOnlyAddress;

import junit.framework.TestCase;

public class SipHeaderTest extends TestCase
{
	public void testAddress() throws Exception
	{
		SipRequest request = new SipRequest();
		Address address = new NameAddr("sip:foo");
		try
		{
			request.addAddressHeader("From", address, false);
			fail("system header");
		}
		catch (IllegalArgumentException e)
		{
		}
		
		try
		{
			request.addAddressHeader("Expires", address, false);
			fail("not address header");
		}
		catch (IllegalArgumentException e)
		{
		}
		
		request.addAddressHeader("foo", address, false);
		assertEquals("foo", ((SipURI) request.getAddressHeader("FOO").getURI()).getHost()); 
		
		request.addAddressHeader("bar", new ReadOnlyAddress(address), false);
		assertEquals("foo", ((SipURI) request.getAddressHeader("Bar").getURI()).getHost()); 
		
		request.setAddressHeader("bar", new ReadOnlyAddress(address));
		assertEquals("foo", ((SipURI) request.getAddressHeader("Bar").getURI()).getHost()); 
	}
}
