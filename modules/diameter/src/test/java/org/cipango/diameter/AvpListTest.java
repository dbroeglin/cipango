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

import java.util.Iterator;

import org.cipango.diameter.ims.IMS;

import junit.framework.TestCase;

public class AvpListTest extends TestCase
{

	public void testAvps()
	{
		AVPList avps = new AVPList();
		avps.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.PUBLIC_IDENTITY, "sip:alice@cipango.org"));
		avps.add(AVP.ofString(IMS.IMS_VENDOR_ID, IMS.PUBLIC_IDENTITY, "sip:alice-fixe@cipango.org"));
		
		Iterator<AVP> it = avps.getAVPs(IMS.IMS_VENDOR_ID, IMS.PUBLIC_IDENTITY);
		assertTrue(it.hasNext());
		assertEquals("sip:alice@cipango.org", it.next().getString());
		assertTrue(it.hasNext());
		assertEquals("sip:alice-fixe@cipango.org", it.next().getString());
		assertFalse(it.hasNext());
		assertFalse(avps.getAVPs(IMS.IMS_VENDOR_ID, IMS.REASON_CODE).hasNext());
	}
}
