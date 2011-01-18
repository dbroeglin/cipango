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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.sip.SipServletResponse;

import junit.framework.TestCase;

import org.cipango.client.UA;
import org.cipango.client.UaManager;
import org.cipango.client.SipRequest;
import org.cipango.client.SipResponse;
import org.cipango.client.SipSession;
import org.cipango.client.script.UaRunnable;
import org.cipango.client.script.UacScript;
import org.cipango.client.script.UasScript;

public class SampleTest extends TestCase
{
		
	private UaManager _uaManager;
	private UA _alice;
	private UA _bob;
	
	public static final String SERVLET_HEADER = "P-Servlet";
	public static final String METHOD_HEADER = "P-method";
	public static final String APP_NAME = "cipango-servlet-test";

	@Override
	protected void setUp() throws Exception
	{
		_uaManager = new UaManager(5061);
		_alice = new UA(_uaManager, "sip:alice@cipango.org");
		_alice.setProxy("sip:" + InetAddress.getLocalHost().getHostAddress() + ";lr");
		_uaManager.start();
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception
	{
		_uaManager.stop();
		super.tearDown();
	}
	
	public UA getBob() throws Exception
	{
		if (_bob == null)
		{
			_bob = new UA(_uaManager, "sip:bob@cipango.org");
			_bob.setProxy(_alice.getProxy());
		}
		return _bob;
	}
	
	public SipRequest createRequest(String method, String to) throws Exception
	{
		SipRequest request = _alice.createRequest(method, to);
		
		Map<String,List<String>> methodList = createMethodList();		
		request.getSession().setHeaders(methodList);	
		request.getSession().addHeaders(request);
		
		return request;
	}
	
	
	public static Map<String,List<String>> createMethodList()
	{
		Exception e = new Exception();
		StackTraceElement[] stackTrace = e.getStackTrace();
		for (StackTraceElement element: stackTrace)
		{
			if (element.getClassName().endsWith("Test")
					&& element.getMethodName().startsWith("test"))
			{
				Map<String,List<String>> additionalHeaders = new HashMap<String, List<String>>();
				additionalHeaders.put(SERVLET_HEADER, asList(element.getClassName()));
				additionalHeaders.put(METHOD_HEADER, asList(element.getMethodName()));
				return additionalHeaders;
			}
		}
		throw new IllegalStateException("Could not found test method");
	}
	
	private static <T> List<T> asList(T element)
	{
		List<T> l = new ArrayList<T>();
		l.add(element);
		return l;
	}
	
	public void testMessage() throws Exception
	{
		SipRequest request = createRequest("MESSAGE", "sip:bob@cipango.org");
		request.send();
		
		SipResponse response = request.waitResponse();
		assertEquals(SipServletResponse.SC_OK, response.getStatus());
	}
	
	public void testCall() throws Exception
	{
		SipRequest request = createRequest("INVITE", "sip:uas@cipango.org");
		request.send();
		
		SipResponse response = request.waitResponse();
		assertEquals(SipServletResponse.SC_RINGING, response.getStatus());
		response = request.waitResponse();
		assertEquals(SipServletResponse.SC_OK, response.getStatus());
		response.createAck().send();
		Thread.sleep(100);
		
		request = request.getSession().waitRequest();
		request.createResponse(SipServletResponse.SC_OK).send();
	}
	
	public void testProxy() throws Throwable
	{
		SipRequest request = createRequest("INVITE", "sip:bob@cipango.org");
		SipSession sessionBob = getBob().createUasSession();
		sessionBob.setHeaders(createMethodList());
		request.setRequestURI(getBob().getContact().getURI());
		
		UaRunnable bob = new UasScript.RingingOkBye(sessionBob);
		new Thread(bob).start();
		UacScript.ringingOkBye(request);
		bob.assertDone();
		/*
		request.send();
		
		SipRequest requestB = sessionBob.waitRequest();
		requestB.createResponse(SipServletResponse.SC_OK).send();
			
		SipResponse response = request.waitResponse();
		assertEquals(SipServletResponse.SC_OK, response.getStatus());
		response.createAck().send();
		sessionBob.waitRequest();
		
		requestB = sessionBob.createRequest("BYE");
		requestB.send();
		
		request = request.getSession().waitRequest();
		request.createResponse(SipServletResponse.SC_OK).send();
		
		assertEquals(SipServletResponse.SC_OK, requestB.waitResponse().getStatus());
		*/
	}
	
	
		
}
