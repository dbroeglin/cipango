// ========================================================================
// Copyright 2010 NEXCOM Systems
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

import java.lang.reflect.Field;

import junit.framework.TestCase;

import org.cipango.diameter.base.Accounting;
import org.cipango.diameter.base.Common;
import org.cipango.diameter.ims.Cx;

public class ApplicationIdTest extends TestCase
{

	public void testOfAvp() throws Exception
	{
		DiameterRequest request = new DiameterRequest();
		request.setApplicationId(Cx.CX_APPLICATION_ID.getId());
		request.setHopByHopId(1);
		request.setEndToEndId(3);
		request.setCommand(Cx.LIR);
		request.setAVPList(new AVPList());
		request.getAVPs().add(Cx.CX_APPLICATION_ID.getAVP());
		System.out.println(request);
		ApplicationId appId = ApplicationId.ofAVP(request);
		System.out.println(appId);
		assertTrue(appId.isVendorSpecific());
		assertTrue(appId.isAuth());
		assertEquals(Cx.CX_APPLICATION_ID, appId);
		assertEquals(Cx.CX_APPLICATION_ID.getId(), request.getApplicationId());
	}
	
	public void test2() throws Exception
	{
		DiameterRequest request = new DiameterRequest();
		request.setApplicationId(Accounting.ACCOUNTING_ID.getId());
		request.setHopByHopId(1);
		request.setEndToEndId(3);
		request.setCommand(Accounting.ACR);
		request.setAVPList(new AVPList());
		request.getAVPs().add(Accounting.ACCOUNTING_ID.getAVP());
		System.out.println(request);
		ApplicationId appId = ApplicationId.ofAVP(request);
		System.out.println(appId);
		assertFalse(appId.isVendorSpecific());
		assertTrue(appId.isAcct());
		assertEquals(Accounting.ACCOUNTING_ID, appId);
		assertEquals(Accounting.ACCOUNTING_ID.getId(), request.getApplicationId());
	}	
}
