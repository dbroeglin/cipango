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
package org.cipango.groovy;

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.TooManyHopsException;
import javax.servlet.sip.URI;

import org.codehaus.groovy.runtime.MethodClosure;

/**
 * SIP-Servlet-request-specific binding extension.
 * <p/>
 * <p>
 * <h3>variables</h3>
 * <ul>
 *   <li><tt>"request"</tt> : the <code>SipServletRequest</code> object</li>
 *   <li><tt>"params"</tt> : map of all request parameters - can be empty</li>
 * </ul>
 * <p/>
 * <p>
 * <h3>Methods</h3>
 * <ul>
 *   <li><tt>"proxy()"</tt> : request.getProxy().proxyTo(request.getRequestURI();</li>
 *   <li><tt>"proxy(String uri)"</tt> : proxy(sipFactory.createURI(uri));</li>
 *   <li><tt>"proxy(URI uri)"</tt> : request.getProxy().proxyTo(uri);</li>
 *   <li><tt>"sendResponse(int status)"</tt> : request.createResponse(status).send();</li>
 *   <li><tt>"sendResponse(int status, String reason)"</tt> : request.createResponse(status, reason).send();</li>
 *   <li><tt>"pushRoute(String route)"</tt> : request.pushRoute(sipFactory.createAddress(route));</li>
 * </ul>
 * </p>
 *
 */
public class SipServletRequestBinding extends SipServletBinding
{

	public static final String 
		REQUEST = "request",
		PARAMS = "params";
	
	@SuppressWarnings("unchecked")
	public SipServletRequestBinding(SipServletRequest request, ServletContext context)
	{
		super(request, context);

		setVariable(REQUEST, request);

		/*
		 * Bind form parameter key-value hash map.
		 * 
		 * If there are multiple, they are passed as an array.
		 */
		Map<String, Object> params = new LinkedHashMap<String, Object>();
		for (Enumeration names = request.getParameterNames(); names.hasMoreElements();)
		{
			String name = (String) names.nextElement();
			if (!super.getVariables().containsKey(name))
			{
				String[] values = request.getParameterValues(name);
				if (values.length == 1)
				{
					params.put(name, values[0]);
				}
				else
				{
					params.put(name, values);
				}
			}
		}
		super.setVariable(PARAMS, params);

		addMethod("proxy");
		addMethod("sendResponse");
		addMethod("pushRoute");
	}
	
	private void addMethod(String name)
	{
		super.setVariable(name, new MethodClosure(this, name));
		_reservedNames.add(name);
	}
	
	private SipServletRequest getRequest()
	{
		return (SipServletRequest) getVariable(REQUEST);
	}
	
	private SipFactory getSipFactory()
	{
		return (SipFactory) getVariable(SIP_FACTORY);
	}

	public void proxy() throws TooManyHopsException
	{
		proxy(getRequest().getRequestURI());
	}
		
	public void proxy(String uri) throws TooManyHopsException, ServletParseException
	{
		proxy(getSipFactory().createURI(uri));
	}
	
	public void proxy(URI uri) throws TooManyHopsException
	{
		SipServletRequest request = getRequest();
		Proxy proxy = request.getProxy();
		proxy.setSupervised(true);
		proxy.proxyTo(uri);
	}

	public void sendResponse(int code) throws IOException
	{
		getRequest().createResponse(code).send();
	}
	
	public void sendResponse(int code, String reason) throws IOException
	{
		getRequest().createResponse(code, reason).send();
	}

	public void pushRoute(String route) throws ServletParseException
	{
		getRequest().pushRoute(getSipFactory().createAddress(route));
	}
}
