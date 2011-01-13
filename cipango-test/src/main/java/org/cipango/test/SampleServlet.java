// ========================================================================
// Copyright 2011 NEXCOM Systems
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
package org.cipango.test;

import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.annotation.SipServlet;

import org.cipango.test.common.AbstractServlet;

@SipServlet(name="org.cipango.test.SampleTest")
public class SampleServlet extends AbstractServlet
{

	public void testMessage(SipServletRequest request) throws Exception
	{
		request.createResponse(SipServletResponse.SC_OK).send();
	}
	
	public void testCall(SipServletRequest request) throws Exception
	{
		if ("INVITE".equals(request.getMethod()))
		{
			request.createResponse(SipServletResponse.SC_RINGING).send();
			Thread.sleep(100);
			request.createResponse(SipServletResponse.SC_OK).send();
		}
		else if ("ACK".equals(request.getMethod()))
		{
			Thread.sleep(100);
			request.getSession().createRequest("BYE").send();
		}
	}
	
	public void testCall(SipServletResponse response) throws Exception
	{
		response.getApplicationSession().invalidate();
	}
	
	public void testProxy(SipServletRequest request) throws Exception
	{
		Proxy proxy = request.getProxy();
		proxy.setRecordRoute(true);
		proxy.setSupervised(false);
		proxy.proxyTo(request.getRequestURI());
	}
		
}
