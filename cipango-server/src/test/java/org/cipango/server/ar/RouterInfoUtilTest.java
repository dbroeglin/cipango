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
package org.cipango.server.ar;

import java.io.Serializable;

import javax.servlet.sip.SipURI;
import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.servlet.sip.ar.SipApplicationRoutingRegion;
import javax.servlet.sip.ar.SipApplicationRoutingRegionType;
import javax.servlet.sip.ar.SipRouteModifier;

import junit.framework.TestCase;

import org.cipango.server.ar.RouterInfoUtil;
import org.cipango.sip.SipURIImpl;

public class RouterInfoUtilTest extends TestCase
{
	public void testDecode() throws Exception
	{
		testRouterInfo(new SipApplicationRouterInfo("kaleo", 
				SipApplicationRoutingRegion.NEUTRAL_REGION, 
				"sip:alice@cipango.voip", 
				new String[] { "sip:as1.cipango.voip", "sip:as2.cipango.voip" }, 
				SipRouteModifier.ROUTE, 
				"1"));
	}
	
	protected void testRouterInfo(SipApplicationRouterInfo routerInfo) throws Exception
	{
		SipURI uri = new SipURIImpl(null, "localhost", 5060);
		RouterInfoUtil.encode(uri, routerInfo);
		//System.out.println(uri);
		assertEquals(routerInfo, RouterInfoUtil.decode(new SipURIImpl(uri.toString())));
	}
	
	public void assertEquals(SipApplicationRouterInfo orig, SipApplicationRouterInfo actual)
	{
		assertEquals(orig.getNextApplicationName(), actual.getNextApplicationName());
		if (orig.getRoutingRegion() != null)
		{
			assertEquals(orig.getRoutingRegion().getType(), actual.getRoutingRegion().getType());
			assertEquals(orig.getRoutingRegion().getLabel(), actual.getRoutingRegion().getLabel());
		}
		else
			assertNull(actual.getRoutingRegion());

		assertEquals(orig.getSubscriberURI(), actual.getSubscriberURI());
		assertEquals(orig.getStateInfo(), actual.getStateInfo());
	}
	
	public void testDecodeNull() throws Exception
	{
		testRouterInfo(new SipApplicationRouterInfo("kaleo", 
				null, null, null, null, null));
	}
	
	public void testStrangeAppName() throws Exception
	{
		testRouterInfo(new SipApplicationRouterInfo(":@1=>!;", 
				null, null, null, null, null));
	}
	
	public void testCustomRegion() throws Exception
	{
		testRouterInfo(new SipApplicationRouterInfo("kaleo", 
				new CustomRegion("custom", SipApplicationRoutingRegionType.NEUTRAL),
				"sip:alice@cipango.voip", 
				new String[] { "sip:as1.cipango.voip", "sip:as2.cipango.voip" }, 
				SipRouteModifier.ROUTE, 
				"1"));
	}
	
	static class CustomRegion extends SipApplicationRoutingRegion implements Serializable
	{

		public CustomRegion(String arg0, SipApplicationRoutingRegionType arg1)
		{
			super(arg0, arg1);
		}
		
		@Override
		public String getLabel()
		{
			return super.getLabel() + " Mine";
		}
		
	}
}
