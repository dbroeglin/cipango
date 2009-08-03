// ========================================================================
// Copyright 2009 NEXCOM Systems
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
package org.cipango.diameter;

import java.net.InetAddress;

import org.cipango.diameter.base.Base;

import junit.framework.TestCase;

public class AvpTest extends TestCase
{

	public void testAvpAddress() throws Exception
	{
		InetAddress address = InetAddress.getByName("[::1]");
		AVP avp = AVP.ofAddress(Base.HOST_IP_ADDRESS, address);
		assertEquals(address, avp.getAddress());
		assertEquals(address, avp.getAddress());
		assertEquals(18, avp.getBytes().length);

		address = InetAddress.getByName("192.168.0.2");
		avp = AVP.ofAddress(Base.HOST_IP_ADDRESS, address);
		assertEquals(address, avp.getAddress());
		assertEquals(6, avp.getBytes().length);

		address = InetAddress
				.getByName("1fff:0000:0a88:85a3:0000:0000:ac1f:8001");
		avp = AVP.ofAddress(Base.HOST_IP_ADDRESS, address);
		assertEquals(address, avp.getAddress());
		assertEquals(18, avp.getBytes().length);
	}
}
