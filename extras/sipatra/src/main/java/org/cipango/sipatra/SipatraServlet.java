// ========================================================================
// Copyright 2003-2011 the original author or authors.
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
package org.cipango.sipatra;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.cipango.sipatra.properties.Properties;
import org.cipango.sipatra.properties.PropertyUtils;
import org.jruby.embed.ScriptingContainer;
import org.slf4j.Logger;

/**
 * Sipatra Main Servlet
 * 
 * @author Dominique Broeglin <dominique.broeglin@gmail.com>, Jean-Baptiste Morin
 */
public class SipatraServlet extends SipServlet
{
	private static final Logger _log = org.slf4j.LoggerFactory.getLogger(SipatraServlet.class);
	
	private ServletContext _servletContext;

	/**
	 * Initialize the Servlet.
	 * 
	 * @throws ServletException
	 *             if this method encountered difficulties
	 */
	@Override
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);
		_servletContext = config.getServletContext();
		startPool();
	}

	@Override
	public void doRequest(SipServletRequest request) throws IOException
	{
		invokeMethod(request, "do_request");
	}

	@Override
	public void doResponse(SipServletResponse response) throws IOException
	{
		invokeMethod(response, "do_response");
	}	

	private void invokeMethod(SipServletMessage message, String methodName) 
	{
		ScriptingContainer container = null;
		try 
		{
			GenericObjectPool pool = (GenericObjectPool) _servletContext.getAttribute(Attributes.POOL);
			container = (ScriptingContainer) pool.borrowObject();
			try 
			{
				Object app = container.runScriptlet("Sipatra::Application::new");

				container.callMethod(app, "set_bindings", new Object[] { 
						_servletContext,  
						_servletContext.getAttribute(SipServlet.SIP_FACTORY), 
						message.getSession(), 
						message, _log});
				container.callMethod(app, methodName);
			} 
			catch(Exception e) 
			{
				pool.invalidateObject(container);
				container = null;
			} 
			finally 
			{
				if(container != null) 
				{
					pool.returnObject(container);
				}
			}
		} 
		catch(Exception e) 
		{
			_log.error("ERROR >> Failed to borrow a JRuby Runtime ", e);
			// TODO: failed to borrow a runtime... What should we do?
			//throw e?
		}
	}

	@Override
	public void destroy()
	{
		super.destroy();
		stopPool();
	}

	private void startPool() 
	{
		GenericObjectPool pool = (GenericObjectPool) _servletContext.getAttribute(Attributes.POOL);
		int init_pool_size = PropertyUtils.getIntegerProperty(Properties.SIPATRA_POOL_INIT_POOL_SIZE, 0, _servletContext);
		for(int i = 0; i< init_pool_size; i++)
		{
			try 
			{
				pool.addObject();
			} 
			catch (Exception e) 
			{
				_log.error("<<ERROR>>", e);
			}
		}
		_log.info("Pool started with "+init_pool_size+" JRuby Runtimes!");
	}

	private void stopPool() 
	{
		GenericObjectPool pool = (GenericObjectPool) _servletContext.getAttribute(Attributes.POOL);
		pool.clear();
		try 
		{
			pool.close();
		} 
		catch (Exception e) 
		{
			_log.error("ERROR >> Failed to close pool ", e);
		}
	}
}