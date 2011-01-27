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
package org.cipango.plus.sipapp;

import java.util.EventListener;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.cipango.sipapp.SipAppContext;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler.Decorator;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.Log;

public class SipResourceDecorator implements Decorator
{

	public static final String JNDI_SIP_PREFIX = "sip/";
	public static final String JNDI_SIP_FACTORY = "SipFactory";
	public static final String JNDI_TIMER_SERVICE = "TimerService";
	public static final String JNDI_SIP_SESSIONS_UTIL = "SipSessionsUtil";
	public static final String JNDI_SIP_FACTORY_POSTFIX = "/" + JNDI_SIP_FACTORY;
	public static final String JNDI_TIMER_SERVICE_POSTFIX = "/" + JNDI_TIMER_SERVICE;
	public static final String JNDI_SIP_SESSIONS_UTIL_POSTFIX = "/" + JNDI_SIP_SESSIONS_UTIL;
	
	protected String _name;
	protected SipAppContext _context;
	
	public SipResourceDecorator(SipAppContext context)
	{
		_context = context;
	}
	
	public void bindSipResources() throws Exception
	{
		ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(_context.getClassLoader());
		Context context = new InitialContext();
		Context compCtx;

		try
		{
			compCtx = (Context) context.lookup("java:comp/env");
		}
		catch (NameNotFoundException e)
		{
			compCtx = ((Context) context.lookup("java:comp")).createSubcontext("env");
		}

		Context sipCtx;
		try
		{
			sipCtx = (Context) compCtx.lookup("sip");
		}
		catch (NameNotFoundException e)
		{
			sipCtx = (Context) compCtx.createSubcontext("sip");
		}

		sipCtx.createSubcontext(_name);
		compCtx.bind(JNDI_SIP_PREFIX + _name + JNDI_SIP_FACTORY_POSTFIX, _context.getSipFactory());
		compCtx.bind(JNDI_SIP_PREFIX + _name + JNDI_TIMER_SERVICE_POSTFIX, _context.getTimerService());
		compCtx.bind(JNDI_SIP_PREFIX + _name + JNDI_SIP_SESSIONS_UTIL_POSTFIX, _context.getSipSessionsUtil());
		Log.debug("Bind SIP Resources on app " + _name);
		Thread.currentThread().setContextClassLoader(oldLoader);
	}
	
	protected void init()
	{
		try
		{
			String name = _context.getName();
			if (name == null || "".equals(name.trim()))
				name = _context.getDefaultName();
				
			if (_name == null || !_name.endsWith(name))
			{
				_name = name;
				bindSipResources();
			}
		}
		catch (Exception e) 
		{
			Log.warn("Failed to bind SIP resources", e);
		}
	}


	public <T extends Filter> T decorateFilterInstance(T filter) throws ServletException
	{
		return filter;
	}

	public <T extends Servlet> T decorateServletInstance(T servlet) throws ServletException
	{
		init();
		return servlet;
	}

	public <T extends EventListener> T decorateListenerInstance(T listener) throws ServletException
	{
		init();
		return listener;
	}

	public void decorateFilterHolder(FilterHolder filter) throws ServletException
	{		
	}

	public void decorateServletHolder(ServletHolder servlet) throws ServletException
	{
		init();
	}

	public void destroyServletInstance(Servlet s)
	{
	}

	public void destroyFilterInstance(Filter f)
	{		
	}

	public void destroyListenerInstance(EventListener f)
	{		
	}
}
